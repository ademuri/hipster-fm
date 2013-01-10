package hipsterfm

import grails.converters.JSON

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
			}
		}
		
		[friends: friends, user: user]
	}
	
	def search(String user, String artist) {
//		log.info "search params: ${params}"
		def usernameList = user.tokenize()
		Set userList = []
		
		for(String username : usernameList) {
//		usernameList.each {
			def userInstance = User.findByUsername(username)
			if (!userInstance) {
				log.warn "Didn't find user with username ${user}"
				flash.message = "Didn't find user with username ${user}"
				redirect(action: "setup")
				return
			}
			userList.add(userInstance)
		}
		
		// grab users from the checkboxes
		params.each {
			if (it.key.contains("user_")) {
				if (it.value.contains("on")) {
					def userInstance = User.get((it.key - "user_") as Long)
					userList.add(userInstance)
				}
			}
		}
		
		userList.each {
			log.info "Syncing for user ${it}"
			lastFmService.getArtistTracks(it, artist)
//			userArtistIds.add(it.userArtist.id)
		}
		
		def artistInstance = Artist.findByName(artist)
		if (!artistInstance) {
			log.warn "Didn't find artist ${artist}"
			flash.message = "Didn't find artist ${artist}"
			redirect(action: "setup")
			return
		}

		
		def userArtistList = []
		userList.each { userInstance ->
			def userArtistInstance = UserArtist.findByUserAndArtist(userInstance, artistInstance)
			if (!userArtistInstance) {
				log.warn "User ${user} has no scrobbles for artist ${artist}!"
				flash.message = "User ${user} has no scrobbles for artist ${artist}!"
				redirect(action: "setup")
				return
			}
			userArtistList.add(userArtistInstance)
		}
		
		// sync necessary data
//		log.info "Syncing friends for user ${user}"
//		lastFmService.getFriends(userInstance)
//		def users = [userInstance]
//		users.addAll(userInstance.friends)
//		log.info "friends: "
//		log.info userInstance.friends
	 
		def userArtistIds = []
		
		
		
		chain(action: "show", model: [artistId: artistInstance.id, userArtistIdList: userArtistList.id])
	}
	
	def show() {
		if (!chainModel || !chainModel?.artistId || !chainModel?.userArtistIdList) {
			log.warn "Graph-Show called without previous model"
			flash.message = "Please setup a graph first"
			redirect(action: "setup")
			return
		}
		def artist = Artist.get(chainModel.artistId)
		if (!artist) {
			log.warn "Couldn't find artist with id ${id}"
			flash.message = "Couldn't find artist with id ${id}"
			redirect(action: "setup")
			return
		}

		def data = []
		def users = []
		
		def intervalSize = 30 	// about a month
		def tickSize = 30
		
		def globalFirst, globalLast
		
		def userArtistIdList = chainModel.userArtistIdList
		def userArtistList = []
		
		userArtistIdList.each { id ->
			def userArtist = UserArtist.get(id)
			userArtistList.add(userArtist)
			
			users.add(userArtist.user.toString())
			def dates = userArtist.tracks.collect { 
				it.date
			} as Set
			dates = dates as List	// only grab unique elements, but should be sorted
			
			dates.sort()
			
			log.info "Found dates from ${dates.first()} to ${dates.last()} for artist ${artist}, user ${userArtist.user}; ${dates.size()} total scrobbles"
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
		
		
		userArtistList.each { userArtist ->
			def counts = []
			def found = false	// only start adding when we've found some tracks
			
			for (int i=0; i<(globalLast-globalFirst); i+=tickSize) {
				def c = Track.createCriteria()
				def count = c.count{
					eq("artist.id", userArtist.id)
					between('date', globalFirst+i, globalFirst+i+intervalSize)
				}
				if (found || count > 0) {
					found = true
					counts.add([String.format('%tY-%<tm-%<td', globalFirst+i), count])
				}
			}
			data.add(counts)
		}
		
		def chartdata = [:]
		chartdata.data = data
		chartdata.series = []
		users.each {
			chartdata.series.add(["label": it])
		}
		
		//def json = data as JSON
		[chartdata:chartdata as JSON, artistName: artist.name]
		//data as JSON
	}
}
