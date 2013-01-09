package hipsterfm

import grails.converters.JSON

class GraphController {

	def lastFmService
	
	def index() {
		redirect(action: 'setup')
	}
	
    def setup() {
		
	}
	
	def search(String user, String artist) {
		def usernameList = user.tokenize()
		def userList = []
		
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
		log.info "Syncing friends for user ${user}"
//		lastFmService.getFriends(userInstance)
//		def users = [userInstance]
//		users.addAll(userInstance.friends)
//		log.info "friends: "
//		log.info userInstance.friends
	 
		def userArtistIds = []
		
		userList.each {
			log.info "Syncing for user ${it}"
			lastFmService.getArtistTracks(it, artistInstance.name)
//			userArtistIds.add(it.userArtist.id)
		}
		
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
		
		def intervalSize = 100 	// about a month
		def tickSize = 100
		
		//artist.userArtists.each { userArtist ->
//		def userArtistList = chainModel.userArtistList
		def userArtistIdList = chainModel.userArtistIdList
		userArtistIdList.each { id ->
			def userArtist = UserArtist.get(id)
			users.add(userArtist.user.toString())
			def dates = userArtist.tracks.collect { 
				it.date
			} as Set
			dates = dates as List	// only grab unique elements, but should be sorted
			
			dates.sort()
			
			log.info "Found dates from ${dates.first()} to ${dates.last()} for artist ${artist}, user ${userArtist.user}; ${dates.size()} total scrobbles"
			log.info dates
			def counts = []
			
			def start = dates.first()
			start.hours = 0
			start.minutes = 0
			start.seconds = 0
			
			def end = dates.last()
			end.hours = 0
			end.minutes = 0
			end.seconds = 0
			
			
			for (int i=0; i<(end-start); i+=tickSize) {
				def c = Track.createCriteria()
				def count = c.count{
					eq("artist.id", userArtist.id)
					between('date', start+i, start+i+intervalSize)
				}
				//def count = Track.countByArtistAndDate(userArtist, it)
//				log.info "Found ${count} for ${start+i}"
				counts.add([String.format('%tY-%<tm-%<td', start+i), count])
				//data.add([it, count])
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
