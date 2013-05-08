package firstfm

import grails.converters.JSON;
import hipsterfm.GraphDataCache;
import hipsterfm.Track;
import hipsterfm.User;
import org.springframework.transaction.annotation.Transactional;

class GraphDataService {
	
	// parameters for setting ymax based removing outliers
	def kOutlierMin = 30
	def kNumOutliers = 10
	def kOutlierRatioUpper = 1.5	// threshold for max being an outlier
	def kOutlierMax = 200
	
	def kByUser = 0
	def kByArtist = 1
	
	def lastFmService

	@Transactional
    def getGraphData(userArtistList, startDate, endDate, tickSize, intervalSize,
			removeOutliers = false, userMaxY, by = kByUser, albumId = null) {
		def data = []
		def users = []
		def theUserArtists = []
		def globalFirst, globalLast
		def newTickSize, newIntervalSize
		
		def allCache = GraphDataCache.all
//		log.info "cache: "
//		allCache.each {
//			log.info it.dump()
//		}
//		log.info "startDate: ${startDate}"
//		log.info "user artist size: ${userArtistList.size()}"
		def idList = userArtistList.id.sort()
		
		def prevCache = GraphDataCache.withCriteria {
			def compare = { name, val ->
				if (val == null) {
					isNull(name)
				} else {
					eq(name, val)
				}
			}
			
			and {
				isNotNull("chartdataJSON")
				compare("startDate", startDate)
				compare("endDate", endDate)
				compare("tickSize", tickSize as Long)
				compare("intervalSize", intervalSize as Long)
				compare("userMaxY", userMaxY as Long)
				compare("groupBy", by as Long)
				compare("albumId", albumId as Long)
				compare("removeOutliers", removeOutliers)
				compare("userArtists", (idList as JSON).toString())
			}
		}
		
		if (prevCache.size() > 0) {
			log.info "Found previous cache!"
			if (prevCache.size() > 1) {
				log.info "cache: "
				prevCache.each {
					log.info it.dump()
				}
			}
			
			def cachedEntry = prevCache.get(0)
			
			if ((new Date() - cachedEntry.dateCreated) > 2) {
				log.info "Cache too old - deleting"
				cachedEntry.delete()
			} else {
			
//			log.info "data: ${prevCache.get(0).chartdata}"
//			log.info "user artist size: ${prevCache.get(0).userArtists.size()}"
				return cachedEntry.chartdata
			}
		}
		
		
		if (startDate && endDate) {
			globalFirst = startDate
			globalLast = endDate
		}
		
		
		userArtistList.each { userArtist ->
			users.add(userArtist.user.toString())
			theUserArtists.add(userArtist.toString())
			
			if (!(startDate && endDate)) {
				def dates
				if (albumId) {
//					log.info "album id: ${albumId}"
//					log.info "user artist albums: ${userArtist.albums}"
					dates = userArtist.albums.find { it.album.id == albumId }?.tracks.collect { it.date } as Set
				} else {
					dates = userArtist.tracks.collect { it.date } as Set
				}
			
				dates = dates as List	// only grab unique elements, but should be sorted
				
				if (dates.size() == 0) {
					return 	// closure, so only skip this user artist id
				}
				
				dates.sort()

				def start = dates.first()
				start.hours = 0
				start.minutes = 0
				start.seconds = 0
				
				def end = dates.last()
				end.hours = 0
				end.minutes = 0
				end.seconds = 0
				
				if (!globalFirst) {
					globalFirst = start
				} else {
					if (globalFirst > start) {
						globalFirst = start
					}
				}
				
				if (!globalLast) {
					globalLast = end
				} else {
					if (globalLast < end) {
						globalLast = end
					}
				}
			}
		}
		
		if (startDate) {
			globalFirst = startDate
		}
		
		if (endDate) {
			globalLast = endDate
		}
		
		log.info "Done getting date stuff"
		
		newTickSize = (globalLast - globalFirst) / tickSize as int
		newIntervalSize = (globalLast - globalFirst) / intervalSize as int
		
		if (newTickSize < 1) {
			newTickSize = 1
		}
		if (newIntervalSize < 1) {
			newIntervalSize = 1
		}
		
		def outliers = new PriorityQueue<Integer>()
		
		userArtistList.each { userArtist ->
//			log.info "getting for user artist: ${userArtist.user}"
			def counts = []
			def userAlbumId
			
			if (albumId) {
				userAlbumId = userArtist.albums.find { it.album.id == albumId }.id
			}
			
			def found = Track.createCriteria().count {
				eq("artist.id", userArtist.id)
				lt('date', globalFirst)
				if (albumId) {
					eq("album.id", userAlbumId)
				}
			}
			
			for (int i=0; i<(globalLast-globalFirst); i+=newTickSize) {
				def c = Track.createCriteria()
				def count = c.count{
					eq("artist.id", userArtist.id)
					between('date', globalFirst+i, globalFirst+i+newIntervalSize)
					if (albumId) {
						eq("album.id", userAlbumId)
					}
				}
				
				if (removeOutliers && !userMaxY) {
					if (outliers.size() < kNumOutliers || count >= outliers.min()) {
						outliers.add(count)
						if (outliers.size() > kNumOutliers) {
							outliers.remove()	//remove smallest element
						}
					}
				}
				
				if (found || count > 0) {
					found = true
					counts.add([String.format('%tY-%<tm-%<td', globalFirst+i), count])
				}
			}
			data.add(counts)
		}
		
		log.info "Done getting counts"
		
		def maxY
		
		if (removeOutliers && !userMaxY) {
			def outlierList = outliers as List
			outlierList.sort()
			log.info "Outliers: ${outlierList}"
			def outlierMin = outlierList.first()
			def outlierMax = outlierList.last()
			
			if (outlierMin > kOutlierMax) {	// chop off at a maximum value
				maxY = kOutlierMax
			}
			else if (outlierMax < kOutlierMin) {
				maxY = null 	// don't set a max, let jqplot autoscale
			}
			else if (outlierMax > (outlierMin * kOutlierRatioUpper)) {
				int i;
				for (i=0; i<outlierList.size(); i++) {
					if (outlierList[i] > outlierList[i-1] * kOutlierRatioUpper) {
						break
					}
				}
				
				maxY = Math.max((int)(outlierList[i-1]*1.1), kOutlierMin)	// give us a little of breathing room above the curve
			}
			
			if (maxY) {
				log.info "Selected maxY as ${maxY}"
			}
		}
		
		if(userMaxY) {
			log.info "Setting userMaxY to ${userMaxY}"
			maxY = userMaxY
		} else if (!removeOutliers) {
			maxY = 0
		}
		
		log.info "maxY: ${maxY}"
		
		def chartdata = [:]
		chartdata.data = data
		chartdata.series = []
		chartdata.maxY = maxY
		
		def byList
		if (by == kByUser) {
			byList = users
		} else if (by == kByArtist) {
			byList = theUserArtists
		} else {
			log.error "No category for by in getGraphData"
		}
		
		byList.each {
			chartdata.series.add(["label": it])
		}
		
		
		
		def cache = new GraphDataCache(startDate: startDate, endDate: endDate, tickSize: tickSize, intervalSize: intervalSize, 
				userMaxY: userMaxY, groupBy: by, albumId: albumId, removeOutliers: removeOutliers, chartdataJSON: "",
				userArtists: (idList as JSON).toString())
		cache.chartdata = chartdata
		cache.save(failOnError: true, flush: true)
//		log.info "Save to cache ${cache}"
//		log.info "user artist size: ${cache.userArtists.size()}"
		
		return chartdata
	}
			
	def autoUpdateUsers() {
		def now = new Date()
		
		def users = User.withCriteria {
			or {
				lt("allTopArtistsLastSynced", now-7)
				isNull("allTopArtistsLastSynced")
			}
			artists {
				between("lastGraphed", (now-7), now)
			}
		}
		
		log.info "Users to update: ${users}"
		
		users.each { user ->
			log.info "Fetching top artists for user ${user.toString()}"
			def artists = lastFmService.getUserAllTopArtists(user, 0)
			
			artists.each { artist ->
				log.info "Fetching tracks for ${user}: ${artist}"
				lastFmService.getArtistTracks(user, artist.name, false, 0)
			}
			user.allTopArtistsLastSynced = new Date()
		}
	}			
}
