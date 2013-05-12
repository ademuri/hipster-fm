package hipsterfm

import java.text.DateFormat;

import grails.converters.JSON
import groovyx.gpars.GParsPool
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class GraphController {

	def lastFmService
	def graphDataService
	
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
//				log.info "Found user ${user}"
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
		
		def newParams = params
		newParams.putAll([friends: friends, user: user, topArtists: topArtists, interval: interval])
		if (params.artist) {
			newParams.artistName = params.artist
		}
		
		
		return newParams
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
			if (it.key.contains("u_")) {
				if (it.value.contains("on")) {
					def userInstance = User.get((it.key - "u_") as Long)
					
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
		
		def newParams = [a_0: artistInstance.id, removeOutliers: params.removeOutliers?.contains("on") ? true : false,
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
			newParams["u_${i}"] = it.id
		}
		
		if (userList.size() == 1) {
			newParams.userName = userList.iterator()[0].toString()
		}
		
		redirect(action: "show", params: newParams)
	}
	
	def show() {
		if (params["_action_show"]) {
			params.remove("_action_show")
		}
		
		def userId
		
		def newParams = params
		
		def artists = []
		def artistName = ""
		params.each {
			if (it.key.startsWith("a_")) {
				artists.push(it.value)
			}
		}
		for(int i=0; i<artists.size()-1; i++) {
			artistName += (Artist.get(artists.get(i)).toString() + ", ") ?: null
		}
		if (artists.size() > 0) {
			artistName += Artist.get(artists.last()).toString() ?: null
		} 
		newParams.artistName = artistName
		
		def userIdList = []
		params.each {
			if (it.key.contains("u_")) {
				userIdList.push(it.value)
			}
		}
		
		if (userIdList.size() == 1) {
			newParams.userName = User.get(userIdList[0])
		} 
		
		params
	}
	
	def ajaxGraphData = {
		//Long artistId	// if there's only 1 artist
		Boolean removeOutliers = params.removeOutliers == "true"
		def userIdList = []
		def userList = []
		def artistUnsortedList = []	// grab key-value pairs, then sort them
		def artistIdList = []
		def artistList = []
		def userArtistList = []
		
		def by = params.by ? params.by as int : graphDataService.kByUser
//		log.info "params.by: ${params.by}, by: ${by}"

		// users		
		params.each {
			if (it.key.startsWith("u_")) {
				userIdList.push(it.value)
			}
		}
		
		userIdList = userIdList.sort()
		
		userIdList.each {
			def userInstance = User.get(it)
			userList.push(userInstance)
		}
		
		// artists
		params.each {
			if (it.key.startsWith("a_")) {
				artistUnsortedList.push(it)
			}
		}
		
		artistUnsortedList.sort { a, b ->
			if (!(a && b)) {
				return a <=> b
			}
			
//			log.info "sub: ${a.key.substring(2, a.key.size())}"
			return (a.key.substring(2, a.key.size()) as long) <=> (b.key.substring(2, b.key.size()) as long)
		}
		
		artistUnsortedList.each {
			artistIdList.push(it.value)
		}
		
		artistIdList.each {
			def artistInstance = Artist.get(it)
			if (!artistInstance) {
				log.warn "Couldn't find artist with id ${it}"
			} else {
				artistList.push(artistInstance)
			}
		}
		
		// if we don't have any artists, quit
		if (artistList.size() == 0) {
			return [error: "No artists found"]
		} 
		
		log.info "Getting tracks..."
		GParsPool.withPool {
			userList.eachParallel { user ->
				GParsPool.withPool {
					artistList.eachParallel { artist ->
						lastFmService.getArtistTracksSafe(user.id, artist.name) // TODO: probably shouldn't pass name
					}
				}
			}
		}
		
		userList.each { user ->
			artistList.each { artist ->
				def userArtist = UserArtist.findByUserAndArtist(user, artist)
				if (!userArtist) {
					log.info "User ${user} has no scrobbles for artist ${artist}"
				} else {
					userArtistList.add(userArtist)
					userArtist.lastGraphed = new Date()
				}
			}
		}
		
		def albumId
		if (params["albumId"]) {
			albumId = params["albumId"] as Long
		}
		def albumName = albumId ? Album.get(albumId).name : ""

		def users = []
		
		def intervalSize = 25.0 	// about a month
		def tickSize = 25.0
		if (params.tickSize) {
			tickSize = params.tickSize as long
		}
		if (params.intervalSize) {
			intervalSize = params.intervalSize as long
		}
		
		def userMaxY
		if (params.userMaxY) {
			userMaxY = params.userMaxY as Integer
		}		
		
		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
		log.info "Getting graph data"
		def chartdata = graphDataService.getGraphData(userArtistList, startDate, endDate, tickSize, intervalSize, removeOutliers, userMaxY, by, albumId)
		
		log.info "rendering page"
		
		def theData = [chartdata:chartdata]
		render theData as JSON
	}
	
	def fetchTopArtists() {
		graphDataService.autoUpdateUsers()
		return
	}
	
	def updateCache() {
		graphDataService.autoUpdateGraphDataCache()
		return
	}
}
