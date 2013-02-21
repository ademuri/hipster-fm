package hipsterfm

import java.text.DateFormat;

import grails.converters.JSON
import groovyx.gpars.GParsPool
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class GraphController {

	def lastFmService
	
	def index() {
		redirect(action: 'setup')
	}
	
    def setup(String user) {
//		log.info "params: ${params}"
		def friends
		def topArtists
		
		if (user && user != "") {
			def userInstance = User.findByUsername(user)
			
			if (userInstance) {
				log.info "Found user ${user}"
				lastFmService.getFriends(userInstance)
				friends = userInstance.friends
				
				lastFmService.getUserTopArtists(userInstance)
				if (userInstance.artists.size() > 0) {
					topArtists = userInstance.artists.sort { it.numScrobbles }.reverse().getAt(0..(Math.min(userInstance.artists.size()-1, 30)))
				}
			}
		}
		
		[friends: friends, user: user, topArtists: topArtists]
	}
	
	def search(String user, String artist) {
//		log.info "search params: ${params}"
		def usernameList = user.tokenize()
		Set userList = []
		
		for(String username : usernameList) {
//		usernameList.each {
			def userInstance = User.findByUsername(username)
			if (!userInstance) {
				log.info "Didn't find user with username ${user}"
				userInstance = new User(username: username).save(flush: true, failOnError: true)
//				flash.message = "Didn't find user with username ${user}"
//				redirect(action: "setup")
//				return
			}
			userList.add(userInstance)
		}
		
		// grab users from the checkboxes
		params.each {
			if (it.key.contains("user_")) {
				if (it.value.contains("on")) {
					def userInstance = User.get((it.key - "user_") as Long)
					
					// note: this shouldn't happen unless people are monkeying around with the page
					if (!userInstance) {
						return	//only skips this checkbox
					}
					userList.add(userInstance)
				}
			}
		}
		
//		GParsPool.withPool {
			userList.each {
				log.info "Syncing for user ${it}"
				lastFmService.getArtistTracks(it, artist)
	//			userArtistIds.add(it.userArtist.id)
			}
//		}
		
		def artistInstance = Artist.findByName(artist)
		if (!artistInstance) {
			log.warn "Didn't find artist ${artist}"
			flash.message = "Didn't find artist ${artist}"
			redirect(action: "setup")
			return
		}

		
		def userArtistList = []
		
		userList.each { userInstance ->
			log.info "generating user list, ${userInstance}"
			def userArtistInstance = UserArtist.findByUserAndArtist(userInstance, artistInstance)
			if (!userArtistInstance) {
				log.info "User ${userInstance} has no scrobbles for artist ${artist}"
			} else {
				userArtistList.add(userArtistInstance)
			}
		}
		
//		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
//		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
//		log.info "start date: ${startDate}, end date: ${endDate}"

		def album = params.album ? Album.findWhere(artist: artistInstance, name: params.album).id : ""
		log.info "search album: ${album}"	
		def newParams = [artistId: artistInstance.id, removeOutliers: params.removeOutliers?.contains("on") ? true : false, 
			tickSize: params.tickSize, intervalSize: params.intervalSize]
		
		if (album) {
			newParams.albumId = album
		}
		if (params.startDate) {
			newParams.startDate = params.startDate
		}
		if (params.endDate) {
			newParams.endDate = params.endDate
		}
		
		userArtistList.eachWithIndex { it, i ->
			newParams["user${i}"] = it.id
		}
		
		redirect(action: "show", params: newParams)
	}
	
	def show(Long artistId, Boolean removeOutliers) {
		def artist = Artist.get(artistId)
		if (!artist) {
			log.warn "Couldn't find artist with id ${artistId}"
			flash.message = "Couldn't find artist with id ${artistId}"
			redirect(action: "setup")
			return
		}
		
		def userArtistIdList = []
		params.each {
			if (it.key.startsWith("user")) {
				userArtistIdList.push(it.value)
			}
		}
		
		def albumId
		if (params["albumId"]) {
			albumId = params["albumId"] as Long
		}
		def albumName = albumId ? Album.get(albumId).name : ""

		def data = []
		def users = []
		
		def intervalSize = 25.0 	// about a month
		def tickSize = 25.0
		if (params.tickSize) {
			tickSize = params.tickSize as long
		}
		if (params.intervalSize) {
			intervalSize = params.intervalSize as long
		}
		
//		log.info "tickSize: ${tickSize}, intervalSize: ${intervalSize}"
		log.info "params: ${params}"
		
		// parameters for setting ymax based removing outliers
		def kOutlierMin = 30	
		def kNumOutliers = 10
		def kOutlierRatioUpper = 1.5	// threshold for max being an outlier
		def kOutlierMax = 200
		
		def globalFirst, globalLast
		
		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
		if (startDate && endDate) {
			globalFirst = startDate
			globalLast = endDate
		}
		
//		def userArtistIdList = chainModel.userArtistIdList
		def userArtistList = []
		
		log.info "Getting date stuff"
		
		userArtistIdList.each { id ->
			def userArtist = UserArtist.get(id)
			userArtistList.add(userArtist)
			
			users.add(userArtist.user.toString())
			
			if (!(globalFirst && globalLast)) {
				def dates
				if (albumId) {
					log.info "album id: ${albumId}"
					log.info "user artist albums: ${userArtist.albums}"
					dates = userArtist.albums.find { it.album.id == albumId }?.tracks.collect { it.date } as Set
				} else { 
					dates = userArtist.tracks.collect { it.date } as Set
				}
			
				dates = dates as List	// only grab unique elements, but should be sorted
				
				if (dates.size() == 0) {
					return 	// closure, so only skip this user artist id
				}
				
				dates.sort()
				
				log.info "Found dates from ${dates.first()} to ${dates.last()} for artist ${artist}, user ${userArtist.user}; ${dates.size()} total days"
		//			log.info dates
				
				
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
		
		tickSize = (globalLast - globalFirst) / tickSize as int
		intervalSize = (globalLast - globalFirst) / intervalSize as int
		
		if (tickSize < 1) {
			tickSize = 1
		}
		if (intervalSize < 1) {
			intervalSize = 1
		}
		
		
		def outliers = new PriorityQueue<Integer>()
		
		userArtistList.each { userArtist ->
			log.info "getting for user artist: ${userArtist.user}"
			def counts = []
			def found = false	// only start adding when we've found some tracks
			def userAlbumId
			
			if (albumId) {
				userAlbumId = userArtist.albums.find { it.album.id == albumId }.id
			}
			
			for (int i=0; i<(globalLast-globalFirst); i+=tickSize) {
				def c = Track.createCriteria()
				def count = c.count{
					eq("artist.id", userArtist.id)
					between('date', globalFirst+i, globalFirst+i+intervalSize)
					if (albumId) {
						eq("album.id", userAlbumId)
					}
				}
				
				if (removeOutliers) {
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
		
		if (removeOutliers) {
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
				for (i=4; i<outlierList.size(); i++) {
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
		
		def chartdata = [:]
		chartdata.data = data
		chartdata.series = []
		users.each {
			chartdata.series.add(["label": it])
		}
		
		log.info "rendering page"
		
		//def json = data as JSON
		[chartdata:chartdata as JSON, artistName: artist.name, maxY: maxY, albumName: albumName]
		//data as JSON
	}
}
