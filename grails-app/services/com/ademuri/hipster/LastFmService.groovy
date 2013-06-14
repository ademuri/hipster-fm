package com.ademuri.hipster

import com.ademuri.hipster.Album
import com.ademuri.hipster.UserAlbum
import com.ademuri.hipster.Artist
import com.ademuri.hipster.UserArtist
import com.ademuri.hipster.Track
import com.ademuri.hipster.User
import java.text.SimpleDateFormat

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import groovyx.gpars.GParsPool

class LastFmService {
	
	public static String api = '9cc72e8864ba4ec102ee347ec557f262'
	public static String userAgent = 'Grails Last.Fm Notifier Service'
	def url = 'http://ws.audioscrobbler.com/'
	def path = '/2.0/'
	
//	def url = 'http://localhost:8180/'
//	def path = 'LastFmProxy/2.0/'

	//http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=rj&api_key=b25b959554ed76058ac220b7b2e0a026&format=json
	
	static int queriesRunning = 0
	static Object queryLock = new Object()
	static final int maxQueries = 5
	
	def sessionFactory
	
	def timeSinceLastQuery
	
	def volatile priority1 = 0
	
	// allowError: if we're expecting an error, don't keep trying if we get that one
	// priority: higher = higher priority
	def queryApi(query, allowError = -1, priority = 1) {
//		log.info "Enter query ${query}"
		synchronized(queryLock) {
			if (priority == 1) {
				priority1++
			}
			while ((timeSinceLastQuery && (System.currentTimeMillis() - timeSinceLastQuery) < 190) 
				|| (priority == 0 && priority1 > 0)) {
				Thread.sleep(25)
//				log.info "sleeping"
			}
			if (priority == 1) {
				priority1--
			}
			timeSinceLastQuery = System.currentTimeMillis()
		}
//		log.info "Running ${query}"
		
		query.api_key = api
		query.format = 'json'
		
		def data
		
		try {
			withRest(uri:url) {
				for (int i=0; i<5; i++) {
					def resp = get(path: path, query: query)
					data = resp.getData()
					if (data?.error && data.error != allowError) {
						log.warn "Got error ${data.error}, message '${data?.message}' for query ${query}"
						if (data.error.toInteger() == 8) {
							Thread.sleep(5000)
							log.warn "Trying again"
						} else {
							break
						}
					} else {
						break
					}
				}
				
				if (data?.error) {
					log.error "Unable to get data"
				}
			}
		} finally {
		}
		
		return data
	}
	
	def checkIfUserExists(username) {
		def query = [
			method: 'user.getinfo',
			user: username,
			]
		def data = queryApi(query, 6)
		
		if (data?.error == 6) {
			log.warn "User ${username} doesn't exist!"
			return false
		} else {
			return true
		}
	}
	
	def Artist getArtist(name) {
		def artist = Artist.findByName(name) 
		if (artist) {
			return artist
		}
		
		log.info "Querying last.fm API for ${name}"
		def query = [
			method: "artist.getinfo",
			artist: name,
			]
		
		def data = queryApi(query)
		
		if (!data || !data?.artist) {
			log.info "Didn't find artist ${name}"
			return
		}
		
		artist = new Artist(name: data.artist.name, lastId: data.artist.mbid).save(failOnError: true)
		
		return artist
	}
	
	def getFriends(User origUser) {
		def today = new Date()
		if (origUser.friendsLastSynced && origUser.friendsLastSynced > (today-7)) {
			log.info "Not syncing friends for ${origUser}, synced recently"
			return
		}
		
		log.info "Getting friends for ${origUser}"
		
		origUser.friendsLastSynced = new Date()
		def username = origUser.username
		
		def query = [:]
		query.user = username
		query.method = "user.getfriends"
		def data = queryApi(query)
		
//		log.info "Last fm returned ${data}"
		
//		log.info "data: ${data}"
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
			// note: grails docs suggest this should be a set (ie no duplicates), but I'm still seeing duplicates in the DB
			// this is a hack & may cause performance issues if there are many friends
			if (user.friends.find { it == origUser } == null) {
				user.addToFriends(origUser)
//				user.save(flush: true)
			}
			
			if (origUser.friends.find { it == user } == null) {
				origUser.addToFriends(user)
//				origUser.save(flush: true)
			}
		}
	}
	
	// thread safe: pass IDs, not instances
	int getArtistTracksSafe(userId, artistName, force = false) {
		def user = User.get(userId)
		return getArtistTracks(user, artistName, force)
	}
	
	def static cumInsertTime = new TimeDuration(0, 0, 0, 0)
	def static cumDownloadTime =  new TimeDuration(0, 0, 0, 0)
	def cumTracks = 0
	def cumUserArtists = 0
	
	def printStats( ){
		log.info ""
		log.info "Cumulative stats:"
		log.info "Insert time: ${cumInsertTime}"
		log.info "Download time: ${cumDownloadTime}"
		log.info "Tracks: ${cumTracks}"
		log.info "User artists: ${cumUserArtists}"
	}
	
	int getArtistTracks(user, rawArtistName, force = false, priority = 1) {
		
		def existingArtist = Artist.findByName(rawArtistName)
		def cutoffDate = (new Date())-7
		def cutoffTS = cutoffDate.toTimestamp()
		
		def lastSynced = existingArtist ? UserArtist.findByUserAndArtist(user, existingArtist)?.lastSynced : null
		
		if (!force && (existingArtist && UserArtist.findByUserAndArtist(user, existingArtist)?.lastSynced > cutoffDate)
			|| (user.notFoundLastSynced[rawArtistName] && user.notFoundLastSynced[rawArtistName].after(cutoffTS))) {
//			log.info "Not syncing ${rawArtistName} for ${user}, synced recently"
			return 0
		}
			
		def syncFromDate = lastSynced ? lastSynced - 16 : null		// if we've synced before, sync for 15 days from the last day
		// last.fm lets you sync back up to 2 weeks, so this should give us 2 days of margin	
		
		def query = [
			method: 'user.getartisttracks',
			user: user.username,
			artist: rawArtistName,
			]
		
		def downloadTime = new Date()
		
		def data = queryApi(query, -1, priority)
		def success = true			// if we fail but can get most data, throw an exception after we commit the transaction so we save partial data
		def failMessage = ""
		
		if (data?.error) {
			log.warn "Got error ${data.error}, message '${data?.message}' for query ${query}"
			throw new LastFmException("Got error ${data.error}, message '${data?.message}' for query ${query}")
			return 0
		}
		
		if (data.artisttracks?.items && data.artisttracks?.items.toInteger() == 0) {
			log.warn "Found no results for user ${user}, artist ${rawArtistName}"
			user.notFoundLastSynced[rawArtistName] = new Date()
			user.save(failOnError: true, flush: true)
//			user.merge(failOnError: true, flush: true)
			return 0
		}
		
		def tracks = data.artisttracks.track
		// if there's only 1 track, make it into a list
		if (!tracks[0]?.artist) {
			log.info "Making a list"
			tracks = [tracks]
		}
		
		// grab the earliest scrobbles
		def paging = data.artisttracks."@attr"
//		log.info "Got ${paging.totalPages} pages for search ${rawArtistName}"

		if (paging?.totalPages?.toInteger() > 1) {		
			GParsPool.withPool {
				(2..paging.totalPages.toInteger()).eachParallel { i ->
					def newquery =  [
						method: 'user.getartisttracks',
						user: user.username,
						artist: rawArtistName,
						]
					newquery["page"] = i
					data = queryApi(newquery, -1, priority)
					if (!data?.artisttracks) {
						log.error "Didn't get track data for page ${i}"
						log.error "data: ${data}"
						success = false
						failMessage += "Didn't get track data for page ${i}, data: ${data}; "
						return 	// closure, so skip this page
					}
					def trackList = data.artisttracks.track
					if (!trackList[0]?.artist) {
						log.info "Making a list"
						trackList = [trackList]
					}
					trackList.each {
						tracks.push(it)
					}
				}
			}
		}
		
		log.info "Found ${tracks.size()} tracks."
//		log.info tracks
		
		def duration = TimeCategory.minus(new Date(), downloadTime)
		log.warn "Download time: ${duration}"
		cumDownloadTime += duration
		log.warn "Cumulative download time: ${cumDownloadTime}"
		
		
		def artistName = tracks[0]?.artist?."#text"
		
		def insertTime = new Date()
		
		def artistId = tracks[0]?.artist?.mbid
		if (!artistId) {
			log.warn "Search for ${rawArtistName} returned no artist id"
			return 0
		}
		
//		println "artist name: ${artistName}"
		def theArtist = Artist.findByLastId(artistId) ?: new Artist(name: artistName, lastId: artistId).save(flush: true, failOnError: true)
		def userArtist = UserArtist.findByUserAndArtist(user, theArtist) ?: new UserArtist(user: user, artist: theArtist).save(flush: true, failOnError: true)
		theArtist.addToUserArtists(userArtist)
		
		// example: 19 Jun 2012, 21:16
		def dateFormat = new SimpleDateFormat("dd MMM yyyy, kk:mm")
		
		def existingTracks = Track.countByArtist(userArtist) > 0	// don't check for duplicate tracks if none exist
		def albums
		def albumMap
		
		
		Album.withTransaction {
			// do albums stuff efficiently - create them all here, then add tracks to them as needed
			albums = userArtist.albums ?: []
			def rawAlbums = tracks.collect { it.album } as Set
			albumMap = [:]
			log.info "Got ${rawAlbums.size()} albums"
			
			rawAlbums.each { rawAlbum ->
				if (!rawAlbum || rawAlbum.mbid == "") {
					albumMap[""] = null
				}
				else if (albums.find { it.lastId == rawAlbum.mbid } == null) {
	//				log.info "album ${rawAlbum}"
					// create the album
	//				log.info "Id ${rawAlbum.mbid}, name: ${rawAlbum.'#text'}"
					def album = Album.findByLastId(rawAlbum.mbid) ?: new Album(lastId: rawAlbum.mbid, name: rawAlbum."#text", artist: theArtist).save(failOnError: true, flush: true)
					def userAlbum = new UserAlbum(lastId: rawAlbum.mbid, name: rawAlbum."#text", artist: userArtist, album: album).save(failOnError: true, flush: true)
					album.addToUserAlbums(userAlbum).save(failOnError: true, flush: true)
	//				log.info "album: ${album}, userAlbums: ${album.userAlbums}"
					albums.add(userAlbum)
				}
			}
			
			
			albums.each {
				albumMap[it.lastId] = it
			}
		}
//		log.info "Done with albums"

		def count = 0	// clear the session every so often
//		Track.withTransaction {		

		Track.withTransaction {
			def lastExtDate
			def lastTrack = Track.withCriteria {
				maxResults(1)
				order('date', 'desc')
				artist {
					eq("id", userArtist.id)
				}
			}
		
			if (lastTrack.size() == 0) {
				log.info "No previous tracks found"
			} else {
	//			log.info "Last track: ${lastTrack.get(0).date}"
				lastExtDate = lastTrack.get(0).date	
			}
			
			tracks.each {
				if (!it?.date) {
					log.warn "Invalid date!: ${it}"
					success = false
					failMessage += "Invalid date!: ${it}\n"
					return	//skip this track
				}
				
				cumTracks++
				
				def trackId = it.mbid
				def date = dateFormat.parse(it.date."#text")
				if (syncFromDate && date < syncFromDate) {
	//				log.info "Ignoring track with date ${date}, before cutoff ${syncFromDate}"
					return
				}
				def track
				
				if (existingTracks || date > lastExtDate) {
	//				log.info "Searching for existing tracks name: ${it.name}, date: ${date}"
					track = Track.findByLastIdAndDate(trackId, date) ?: new Track(name: it.name, date: date, artist: userArtist, lastId: trackId, album: albumMap[it.album.mbid]).save(failOnError: true)
				} else {
	//				log.info "name: ${it.name}, date: ${date}"
					track = new Track(name: it.name, date: date, artist: userArtist, lastId: trackId, album: albumMap[it.album.mbid]).save(failOnError: true)
	//				log.info track	
				}
				userArtist.addToTracks(track)
			}
		}
			
		duration = TimeCategory.minus(new Date(), insertTime)
		log.warn "Insert time: ${duration}"
		cumInsertTime += duration
		log.warn "Cumulative insert time: ${cumInsertTime}"
		cumUserArtists++
		log.warn "Cum user artists: ${cumUserArtists}, tracks: ${cumTracks}"
		
		log.info "Done creating tracks"
		
		if (!success) {
			// we should have committed all changes we've made, so throw an exception to tell our caller something went wrong
			// 	additionally, don't update lastSynced (for now)
			//		at some point, we should add an errorLastSynced (or similar) since last.fm caches the replies for a little while
			throw new LastFmException(failMessage)
		}

		userArtist.lastSynced = new Date()
		
		return tracks.size()
	}
	
	def getUserAllTopArtists(user, priority) {
		def artists = []
		UserArtist.rankNames.each {
			artists.addAll(getUserTopArtists(user.id, it, priority))
		}
		
		return artists
	}
	
	def getUserTopArtists(userId, interval = "3month", priority = 1) {
		def user = User.lock(userId)
		
		if (!user) {
			log.warn "getUserTopArtists called with null user"
			return
		}
		
		log.info "Getting top artists for user ${user}, interval ${interval}"
		
		
		// if we've synced this recently, don't do it again
		if (user?.topArtistsLastSynced[interval] && user?.topArtistsLastSynced[interval] > (new Date()-7)) {
//			log.info "Synced top artists for ${user.username}, interval ${interval} recently, not syncing"
			return UserArtist.withCriteria {
				eq('user', user)
				eq("isTop${interval}", true)
				order("top${interval}Rank", 'asc')
				maxResults(20)
			}
		}
		
		def query = [
			method: 'user.getTopArtists',
			user: user.username,
			period: interval,
			]
		
		def data = queryApi(query, -1, priority)
//		log.info "data.class: ${data.@class}"
		
		def topArtists = []
		
		if ((data?.topartists?."@attr"?.total) && (data.topartists."@attr".total as int) > 0) {
			def artists = data.topartists.artist
			
			artists.each {
				if (!it.has("mbid")) {
					log.warn "Invalid artist: ${it}"
					log.info "raw data: ${data}"
					return
				}
				
				def artist = Artist.findByLastId(it.mbid)
				if (!artist) {
	//				log.info "Top artist not present: ${it.name}"
					artist = new Artist(name: it.name, lastId: it.mbid).save()
				}
				
				def userArtist = artist.userArtists.find { it.user == user}
				
				if (!userArtist) {
	//				log.info "Top user artist not present: ${artist.name}"
					userArtist = new UserArtist(artist: artist, user: user).save()
					artist.addToUserArtists(userArtist)
				} else {
	//				log.info "Found top user artist: ${artist.name}"
				}
				userArtist."top${interval}Rank" = it."@attr".rank as int
				userArtist."isTop${interval}" = true
				topArtists.push(userArtist)
			}
		}
		
		user.topArtistsLastSynced[interval] = new Date()
		topArtists = topArtists.sort {
			it."top${interval}Rank"
		}
		
		if (topArtists.size() > 20) {
			topArtists = topArtists.subList(0, 20)
		}
		
		return topArtists
	}
}
