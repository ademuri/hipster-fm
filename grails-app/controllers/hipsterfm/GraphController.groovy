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
		log.info "params: ${params}"
		def friends
		def topArtists
		
		def interval = params.interval ?: "3month"
		
		if (user && user != "") {
			def userInstance = User.findByUsername(user)
			
			if (userInstance) {
				log.info "Found user ${user}"
				lastFmService.getFriends(userInstance)
				friends = userInstance.friends
				
				lastFmService.getUserTopArtists(userInstance, interval)
				if (userInstance.artists.size() > 0) {
//					topArtists = userInstance.artists.sort { it.numScrobbles }.reverse().getAt(0..(Math.min(userInstance.artists.size()-1, 30)))
					topArtists = UserArtist.withCriteria {
						eq("user", userInstance)
						eq("isTop${interval}", true)
						between("top${interval}Rank", 1, 30)
						order("top${interval}Rank", "asc")
					}
				}
			}
		}
		
		[friends: friends, user: user, topArtists: topArtists, interval: interval]
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
		
		// if 'Add all friends with artist' is checked, and there is only one user from ^^^,
		// 		fetch the friends from last.fm and add them all
		if (userList.size() == 1 && params.addAllFriends) {
			def userInstance = userList.toList()[0]
			log.info "Adding all friends of user ${userInstance}"
			lastFmService.getFriends(userInstance)
			userList.addAll(userInstance.friends)
		}
		
		if (userList.size() == 0) {
			flash.message = "Please choose at least one user"
			redirect(action: "setup")
			return 
		}
		
		def artistInstance = lastFmService.getArtist(artist)
		if (!artistInstance) {
			log.warn "Didn't find artist ${artist}"
			flash.message = "Didn't find artist ${artist}"
			redirect(action: "setup")
			return
		}
		
		
		
//		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
//		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
//		log.info "start date: ${startDate}, end date: ${endDate}"
		
		def newParams = [artistId: artistInstance.id, removeOutliers: params.removeOutliers?.contains("on") ? true : false,
			tickSize: params.tickSize, intervalSize: params.intervalSize, addAllFriends: params.addAllFriends]

		def album = params.album ? Album.findWhere(artist: artistInstance, name: params.album).id : ""
//		log.info "search album: ${album}"	
		
		if (album) {
			newParams.albumId = album
		}
		if (params.startDate) {
			newParams.startDate = params.startDate
		}
		if (params.endDate) {
			newParams.endDate = params.endDate
		}
		if(params.userMaxY) {
			newParams.userMaxY = params.userMaxY
		}
		
		userList.eachWithIndex { it, i ->
			newParams["user_${i}"] = it.id
		}
		
		redirect(action: "show", params: newParams)
	}
	
	def show() {
		if (params["_action_show"]) {
			params.remove("_action_show")
		}
		
		def newParams = params
		newParams.artistName = Artist.get(params.artistId) ?: null 
		
		params
	}
	
	def ajaxGraphData = {
		Long artistId = params.artistId as Long
		Boolean removeOutliers = params.removeOutliers == "true"
		log.info "params.removeOutliers: ${params.removeOutliers}, removeOutliers: ${removeOutliers}"
		def userIdList = []
		def userList = []
		def userArtistList = []
		
		def artistInstance = Artist.get(artistId)
		if (!artistInstance) {
			log.warn "Couldn't find artist with id ${artistId}"
			flash.message = "Couldn't find artist with id ${artistId}"
			redirect(action: "setup")
			return
		}
		
		params.each {
			if (it.key.startsWith("user_")) {
//				log.info "Got user ${it.value}"
				userIdList.push(it.value)
			}
		}
		
		userIdList = userIdList.sort()
		log.info "user list: ${userIdList}"
		
		userIdList.each {
			def userInstance = User.get(it)
//			log.info "userInstance: ${userInstance}"
			userList.push(userInstance)
		}
		
		userList.each {
//			log.info "Syncing for user ${it}"
			lastFmService.getArtistTracks(it, artistInstance.name) // TODO: probably shouldn't pass name
		}
		
		userList.each { userInstance ->
//			log.info "generating user list, ${userInstance}"
			def userArtistInstance = UserArtist.findByUserAndArtist(userInstance, artistInstance)
			if (!userArtistInstance) {
				log.info "User ${userInstance} has no scrobbles for artist ${artistInstance}"
			} else {
				userArtistList.add(userArtistInstance)
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
		
//		log.info "params: ${params}"
//		log.info "tickSize: ${tickSize}, intervalSize: ${intervalSize}"
		
		// parameters for setting ymax based removing outliers
		def kOutlierMin = 30	
		def kNumOutliers = 10
		def kOutlierRatioUpper = 1.5	// threshold for max being an outlier
		def kOutlierMax = 200
		
		def userMaxY
		if (params.userMaxY) {
			userMaxY = params.userMaxY as Integer
		}		
		
		def globalFirst, globalLast
		
		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
		if (startDate && endDate) {
			globalFirst = startDate
			globalLast = endDate
		}
		
		log.info "Getting date stuff"
		
		userArtistList.each { userArtist ->
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
				
				log.info "Found dates from ${dates.first()} to ${dates.last()} for artist ${artistInstance}, user ${userArtist.user}; ${dates.size()} total days"
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
		users.each {
			chartdata.series.add(["label": it])
		}
		
		log.info "rendering page"
		
		def theData = [chartdata:chartdata, maxY: maxY]
		render theData as JSON
	}
}
