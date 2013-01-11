package firstfm

import hipsterfm.Artist
import hipsterfm.UserArtist
import hipsterfm.Track
import hipsterfm.User
import java.text.SimpleDateFormat
import groovyx.gpars.GParsPool

class LastFmService {
	
	public static String api = '9cc72e8864ba4ec102ee347ec557f262'
	public static String userAgent = 'Grails Last.Fm Notifier Service'

	//http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=rj&api_key=b25b959554ed76058ac220b7b2e0a026&format=json
	
	static int queriesRunning = 0
	static Object queryLock = new Object()
	static final int maxQueries = 5
	
	def sessionFactory
	
    def checkForUser(user) {
		withRest(uri:"http://ws.audioscrobbler.com/") {
//			def resp = get(path: '/?method=user.getrecenttracks?user=${user.username}&api_key=${api}&format=json') {
			
			def query = [
				method: 'user.getrecenttracks',
				user: user.username,
				api_key: api,
				format: 'json'
				] 
			def resp = get(path: '/2.0/', query: query)
			
			def tracks = resp.getData()
			println tracks
		}
    }
	
	def queryApi(query) {
//		log.info "Enter query ${query}"
		synchronized(queryLock) {
//			log.info "Increment queries ${query}"
			while (queriesRunning >= maxQueries) {
//				log.info "Queries already running, waiting ${query}"
				queryLock.wait()
			}
			queriesRunning++
		}
//		log.info "Running ${query}"
		
		query.api_key = api
		query.format = 'json'
		
		def data
		
		try {
			withRest(uri:"http://ws.audioscrobbler.com/") {
				for (int i=0; i<5; i++) {
					def resp = get(path: '/2.0/', query: query)
					data = resp.getData()
					if (data?.error) {
						log.warn "Got error ${data.error}, message '${data?.message}' for query ${query}"
						if (data.error.toInteger() == 8) {
							Thread.sleep(5000)
							log.info "Trying again"
						} else {
							break
						}
					} else {
						break
					}
				}
			}
		} finally {
			synchronized(queryLock) {
//				log.info "Decrement queries ${query}"
				queriesRunning--
				queryLock.notifyAll()
			}
		}
		
		return data
	}
	
	def getFriends(User origUser) {
		def today = new Date()
		if (origUser.friendsLastSynced && origUser.friendsLastSynced > (today-7)) {
			log.info "Not syncing friends for ${origUser}, synced recently"
			return
		}
		
		origUser.friendsLastSynced = new Date()
		def username = origUser.username
		
		def query = [:]
		query.user = username
		query.method = "user.getfriends"
		def data = queryApi(query)
		
		log.info "data: ${data}"
		def users = data.friends.user
		
		def paging = data.friends."@attr"
		for (int i=2; i<=paging.totalPages.toInteger(); i++) {	// pages start at 1
			log.info "Fetching page ${i} of users"
			query.page = i
			data = queryApi(query)
			data.friends.user.each {
				users.add(it)
			}
		}
		
		log.info "Found ${users.size()} friend for user ${username}"
		
		users.each {
			def user = User.findByUsername(it.name)
			if (!user) {
				log.info "Creating user ${it.name} (${it.realname})"
				user = new User(username: it.name, name: it.realname).save(failOnError: true)
			}
			user.addToFriends(origUser)
			origUser.addToFriends(user)
		}
	}
	
	int getArtistTracks(user, rawArtistName) {
		
		def existingArtist = Artist.findByName(rawArtistName)
		def cutoffDate = (new Date())-7
		def cutoffTS = cutoffDate.toTimestamp()
		
		if ((existingArtist && UserArtist.findByUserAndArtist(user, existingArtist)?.lastSynced > cutoffDate)
			|| (user.notFoundLastSynced[rawArtistName] && user.notFoundLastSynced[rawArtistName].after(cutoffTS))) {
			log.info "Not syncing ${rawArtistName} for ${user}, synced recently"
			return 0
		}
			
		
		def query = [
			method: 'user.getartisttracks',
			user: user.username,
			artist: rawArtistName,
			]
		
		def data = queryApi(query)
//		log.info "user ${user}, artist ${rawArtistName}, data: ${data}"
		
		if (data?.error) {
			log.warn "Got error ${data.error}, message '${data?.message}' for query ${query}"
			return 0
		}
		
		if (data.artisttracks?.items && data.artisttracks?.items.toInteger() == 0) {
			log.warn "Found no results for user ${user}, artist ${rawArtistName}"
			user.notFoundLastSynced[rawArtistName] = new Date()
			log.info "in artist tracks, not found: ${user.notFoundLastSynced}"
			user.save(failOnError: true, flush: true)
//			user.merge(failOnError: true, flush: true)
			return 0
		}
		
		def tracks = data.artisttracks.track
		
		
		// grab the earliest scrobbles
		def paging = data.artisttracks."@attr"
		log.info "Got ${paging.totalPages} pages for search ${rawArtistName}"
		
		GParsPool.withPool {
			//for(int i=2; i<=paging.totalPages.toInteger(); i++) {
			(2..paging.totalPages.toInteger()).eachParallel { i ->
//				log.info "Getting page ${i}"
				query["page"] = i
				data = queryApi(query)
				if (!data?.artisttracks) {
					log.error "Didn't get track data for page ${i}"
				}
				data.artisttracks.track.each {
					tracks.add(it)
				}
			}
		}
//		if (paging.totalPages.toInteger() > 1) {
//			query["page"] = paging.totalPages
//			data = queryApi(query)
//			
//			if (paging.totalPages == 2)
//			{
//				data.artisttracks.track.each {
//					tracks.add(it)
//				}
//			} else {
//				query["page"] = query["page"].toInteger() - 1
//				data = queryApi(query)
//				data.artisttracks.track.each {
//					tracks.add(it)
//				}
//			}
//		}
		
		log.info "Found ${tracks.size()} tracks."
		
		//println "print"
		//println data.artisttracks.track[0]
		
		def artistName = tracks[0]?.artist?."#text"
		
		
		def artistId = tracks[0]?.artist?.mbid
		if (!artistId) {
			log.warn "Search for ${rawArtistName} returned no tracks"
			return
		}
		//println "artist name: ${artistName}"
		def artist = Artist.findByLastId(artistId) ?: new Artist(name: artistName, lastId: artistId).save(flush: true, failOnError: true)
		def userArtist = UserArtist.findByUserAndArtist(user, artist) ?: new UserArtist(user: user, artist: artist).save(flush: true, failOnError: true)
		artist.addToUserArtists(userArtist)
		
		// example: 19 Jun 2012, 21:16
		def dateFormat = new SimpleDateFormat("dd MMM yyyy, kk:mm")
		
		def existingTracks = Track.countByArtist(userArtist) > 0	// don't check for duplicate tracks if none exist
		
		tracks.each {
			def trackId = it.mbid
			def date = dateFormat.parse(it.date."#text")
			def track
			
			if (existingTracks) {
				track = Track.findByLastIdAndDate(trackId, date) ?: new Track(name: it.name, date: date, artist: userArtist, lastId: trackId).save(failOnError: true)
			} else {
				track = new Track(name: it.name, date: date, artist: userArtist, lastId: trackId).save(failOnError: true)	
			}
			userArtist.addToTracks(track)
		}
		
		log.info "Done creating tracks"
		
		userArtist.lastSynced = new Date()
		
		
		return tracks.size()
	}
}
