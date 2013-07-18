package com.ademuri.hipster

import com.ademuri.hipster.Album

import com.ademuri.hipster.UserAlbum
import com.ademuri.hipster.Artist
import com.ademuri.hipster.UserArtist
import com.ademuri.hipster.Track
import com.ademuri.hipster.User
import com.mysql.jdbc.log.Log;

import java.text.SimpleDateFormat
import java.util.concurrent.locks.ReentrantLock

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import groovyx.gpars.GParsPool
import org.springframework.transaction.annotation.Transactional

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;

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
						if (data.error == '' || data.error.toInteger() == 8) {
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
	
	@Transactional
	def User getUser(name) {
		def user = User.findByUsername(name)
		if (user) {
			return user
		}
		
		if (checkIfUserExists(name)) {
			user = new User(username: name).save(flush: true, failOnError: true)
			return user
		} else {
			return null
		}
	}
	
	@Transactional
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
	
	@Transactional
	def getFriends(Long id) {
		def origUser = User.lock(id)
		def today = new Date()
		if (origUser.friendsLastSynced && origUser.friendsLastSynced > (today-7)) {
			log.info "Not syncing friends for ${origUser}, synced recently"
			return
		}
		
		log.info "Getting friends for ${origUser}"
		
		// also update the user's information (for now, just realname)
		def query = [:]
		query.user = origUser.username
		query.method = "user.getinfo"
		def data = queryApi(query)
		def realname = data.user.realname
		if (origUser.name != realname) {
			origUser.name = realname
		}
		
		origUser.friendsLastSynced = new Date()
		def username = origUser.username
		
		query = [:]
		query.user = username
		query.method = "user.getfriends"
		data = queryApi(query)
		
		def users = [data.friends.user].flatten()	// the two calls to flatten are for if there is only 1 user on a page
		
		def paging = data.friends."@attr"
		for (int i=2; i<=paging.totalPages.toInteger(); i++) {	// pages start at 1
			log.trace "Fetching page ${i} of users"
			query.page = i
			data = queryApi(query)
			def newData = [data.friends.user].flatten()
			newData.each {
				users.add(it)
			}
		}

		log.info "Found ${users.size()} friend for user ${username}"
		
		users.each {
			if (!it.name) {
				log.warn "Got user with null username from last.fm: ${it}"
				return	// closure, so just skip this one	
			}
			
			def user = User.findByUsername(it.name)
			if (!user) {
//				log.info "Creating user ${it.name} (${it.realname})"
				user = new User(username: it.name, name: it.realname).save(flush: true, failOnError: true)
			}
			// note: grails docs suggest this should be a set (ie no duplicates), but I'm still seeing duplicates in the DB
			// this is a hack & may cause performance issues if there are many friends
			if (user.friends.find { it == origUser } == null) {
				user.addToFriends(origUser)
			}
			
			if (origUser.friends.find { it == user } == null) {
				origUser.addToFriends(user)
			}
		}
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
	
	def volatile syncing = [:]
	def volatile syncLock = new ReentrantLock()	// lock for changing the data structure
	/**
	 * Get scrobbles for a user and artist. Assumes artist already exists.
	 * @param userId
	 * @param artistId
	 * @return
	 */
	def getArtistTracks(userId, artistId, force = false, priority = 1) {
		
		// make sure no-one else is already syncing
		syncLock.lock()
		if (syncing[userId] && syncing[userId][artistId] && syncing[userId][artistId].isLocked()) {
			log.info "Another thread is already syncing this artist"
			syncLock.unlock()
			syncing[userId][artistId].lock()
			syncing[userId][artistId].unlock()
//			Thread.sleep(1000)
			log.info "Other thread has been synced, returning"
			return 1
		} else {
			// create the entry
			if (!syncing[userId]) {
				syncing[userId] = [:]
			}
			syncing[userId][artistId] = new ReentrantLock()
			syncing[userId][artistId].lock()
			syncLock.unlock()
		}
		
		def split = SimonManager.getStopwatch("getArtistTracks").start()

		
		def error
	
		try {
			def tracks
			def userArtist
			def albumMap
			def dateFormat = new SimpleDateFormat("dd MMM yyyy, kk:mm")
			def syncFromDate
			def lastExtDate
			def existingTracks
			def splitInsert
			
			def success = true			// if we fail but can get most data, throw an exception after we commit the transaction so we save partial data
			def failMessage = ""
			
			Artist.withTransaction { status ->
				def splitDownload = SimonManager.getStopwatch("download").start()
				
				def user = User.get(userId)
				def existingArtist = Artist.get(artistId)
				if (!existingArtist) {
					log.error "Artist does not exist with id: ${artistId}"
					throw new IllegalArgumentException("Artist does not exist with id: ${artistId}")	
				}
				
				def cutoffDate = (new Date())-7
				def cutoffTS = cutoffDate.toTimestamp()
				
				def lastSynced = existingArtist ? UserArtist.findByUserAndArtist(user, existingArtist)?.lastSynced : null
				
				if (!force && (existingArtist && UserArtist.findByUserAndArtist(user, existingArtist)?.lastSynced > cutoffDate)
					|| (user.notFoundLastSynced[existingArtist.name] && user.notFoundLastSynced[existingArtist.name].after(cutoffTS))) {
					return 0
				}
				log.info "Getting artist tracks for ${user}, ${existingArtist.name}"
					
				syncFromDate = lastSynced ? lastSynced - 16 : null		// if we've synced before, sync for 15 days from the last day
				// last.fm lets you sync back up to 2 weeks, so this should give us 2 days of margin	
				
				def query = [
					method: 'user.getartisttracks',
					user: user.username,
					artist: existingArtist.name,
					]
				
//				def downloadTime = new Date()
				
				def data = queryApi(query, -1, priority)
				
				if (data?.error) {
					log.warn "Got error ${data.error}, message '${data?.message}' for query ${query}"
					throw new LastFmException("Got error ${data.error}, message '${data?.message}' for query ${query}")
					return 0
				}
				
				if (data.artisttracks?.items && data.artisttracks?.items.toInteger() == 0) {
					log.info "Found no results for user ${user}, artist ${existingArtist.name}"
					user.notFoundLastSynced[existingArtist.name] = new Date()
					user.save(failOnError: true, flush: true)
					return 0
				}
				
				tracks = data.artisttracks.track
				// if there's only 1 track, make it into a list
				if (!tracks[0]?.artist) {
					log.info "Making a list"
					tracks = [tracks]
				}
				
				// grab the earliest scrobbles
				def paging = data.artisttracks."@attr"
				if (paging?.totalPages?.toInteger() > 1) {		
					GParsPool.withPool(2) {
						(2..paging.totalPages.toInteger()).eachParallel { i ->
							def newquery =  [
								method: 'user.getartisttracks',
								user: user.username,
								artist: existingArtist.name,
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
								trackList = [trackList]
							}
							trackList.each {
								tracks.push(it)
							}
						}
					}
				}
				
				splitDownload.stop()
				log.info "Found ${tracks.size()} tracks."
				
				
				splitInsert = SimonManager.getStopwatch("insert").start()
				def artistName = tracks[0]?.artist?."#text"
				def insertTime = new Date()
				
				def artistLastId = tracks[0]?.artist?.mbid
				if (!artistLastId) {
					log.warn "Search for ${existingArtist.name} returned no artist id"
//					return 0
				}
				
				def theArtist = Artist.get(artistId) ?: new Artist(name: artistName, lastId: artistLastId).save(failOnError: true)
				userArtist = UserArtist.findByUserAndArtist(user, theArtist) ?: new UserArtist(user: user, artist: theArtist).save(failOnError: true)
//				theArtist.addToUserArtists(userArtist)
				
				// example: 19 Jun 2012, 21:16
				
				
				existingTracks = Track.countByArtist(userArtist) > 0	// don't check for duplicate tracks if none exist
				def albums
				
				// do albums stuff efficiently - create them all here, then add tracks to them as needed
				albums = userArtist.albums ?: []
				def rawAlbums = tracks.collect { it?.album } as Set
				albumMap = [:]
//				log.info "Got ${rawAlbums.size()} albums"
				
				rawAlbums.each { rawAlbum ->
					if (!rawAlbum || rawAlbum.mbid == "") {
						albumMap[""] = null
					}
					else if (albums.find { it.lastId == rawAlbum.mbid } == null) {
						// create the album
						def album = Album.findByLastId(rawAlbum.mbid) ?: new Album(lastId: rawAlbum.mbid, name: rawAlbum."#text", artist: theArtist).save(flush: true, failOnError: true)
						def userAlbum = new UserAlbum(lastId: rawAlbum.mbid, name: rawAlbum."#text", artist: userArtist, album: album).save(flush: true, failOnError: true)
						albums.add(userAlbum)
					}
				}
				
				
				albums.each {
					albumMap[it.lastId] = it
				}
		
				def count = 0	// clear the session every so often
		
				def lastTrack = Track.withCriteria {
					maxResults(1)
					order('date', 'desc')
					artist {
						eq("id", userArtist.id)
					}
				}
			
				if (lastTrack.size() == 0) {
//					log.info "No previous tracks found"
				} else {
					lastExtDate = lastTrack.get(0).date	
				}
			}// end Artist.withTransaction
			
			if (!tracks) {
				return 0
			}
			
			int numWorkUnits = 2
			int trackSize = tracks.size()
			int workSize = Math.floor(trackSize / numWorkUnits)
			def workUnits = []
			def work = []
			def actors = []
			
			if (tracks.size() < 100) {
				workUnits = [tracks]
			} else {
				for (int i=0; i<numWorkUnits; i++) {
					def end
					if (i < numWorkUnits-1) {
						end = ((i+1)*workSize)-1
					} else {
						end = trackSize-1
					}
//					log.info "from ${i*workSize} to ${end}"
					workUnits.push tracks[i*workSize .. end]
				}
			}
//			log.info "workunits: ${workUnits.size()}"
//			workUnits.each {
//				log.info "   size: ${it.size()}"
//			}
			
			def add = { tracksToAdd ->
//				def dateFormatter = new SimpleDateFormat("dd MMM yyyy, kk:mm")		// not *actually* in GMT, but we don't want it to be adjusted
//				dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"))
				def timeOffset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings()
//				def timeOffset = 60 * 60 * 1000		// one hour
//				def timeOffset = 0	
				tracksToAdd.each {
					if (!it?.date) {
						log.warn "Invalid date!: ${it}"
						success = false
						failMessage += "Invalid date!: ${it}\n"
						return	//skip this track
					}
					
					def trackId = it.mbid
//					def date = dateFormatter.parse(it.date."#text")
					def date = new Date(Long.parseLong(it.date."uts") * 1000 + timeOffset)
//					log.info "${it.name}, date: ${date.toGMTString()}, raw date: ${it.date.'#text'}"
					if (syncFromDate && date < syncFromDate) {
						return
					}
					def track
					
					if (existingTracks || (date && date < lastExtDate)) {
						track = Track.findByLastIdAndDate(trackId, date) ?: new Track(name: it.name, date: date, artist: userArtist, lastId: trackId, album: albumMap[it.album.mbid]).save(failOnError: true)
					} else {
						track = new Track(name: it.name, date: date, artist: userArtist, lastId: trackId, album: albumMap[it.album.mbid]).save(failOnError: true)
					}
//					log.info "date after save: ${track.getDateString()}"
//					track.save(flush: true)
//					log.info "date after flushing: ${track.getDateString()}"
					// this may be helpful: http://burtbeckwith.com/blog/?p=73
					//track.errors = null
				}
			}
			
			workUnits.each {
				work.push add.curry(it)	
			}
				
			work.each {
				def thing = callAsync(it)
				actors.push thing
//				thing.get()
			}
			
			actors.each {
				it.get()
			}
				
			splitInsert.stop()
				
			log.info "Done creating tracks"
			
			
			if (!success) {
				// we should have committed all changes we've made, so throw an exception to tell our caller something went wrong
				// 	additionally, don't update lastSynced (for now)
				//		at some point, we should add an errorLastSynced (or similar) since last.fm caches the replies for a little while
				error =  new LastFmException(failMessage)
			} else {
				userArtist.lastSynced = new Date()
				userArtist.merge()
				userArtist.save(flush: true)
			}
		} finally {
			split.stop()
//			log.info "Unlocking for ${userId}, ${artistId}"
			synchronized(syncing[userId][artistId]) {
				syncing[userId][artistId].unlock()
			}
		}
		
		if (error) {
			throw error
		}
		
		return 0
	}
	
	def getUserAllTopArtists(user, priority) {
		def artists = []
		UserArtist.rankNames.each {
			artists.addAll(getUserTopArtists(user.id, it, priority))
		}
		
		return artists
	}
	
	@Transactional
	def getUserTopArtists(userId, interval = "3month", priority = 1) {
		def user = User.get(userId)
		
		if (!user) {
			log.error "getUserTopArtists called with null user"
			return
		}
		
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
		log.info "Getting top artists for user ${user}, interval ${interval}"
		
		def query = [
			method: 'user.getTopArtists',
			user: user.username,
			period: interval,
			]
		
		def data = queryApi(query, -1, priority)
		
		def topArtists = []
		
		if ((data?.topartists?."@attr"?.total) && (data.topartists."@attr".total as int) > 0) {
			def artists = [data.topartists.artist].flatten()
			
			artists.each {
				if (!it.containsKey("mbid")) {
					log.warn "Invalid artist: ${it}"
					log.info "raw data: ${data}"
					return
				}
				
				def artist = Artist.findByName(it.name)
				if (!artist) {
					artist = new Artist(name: it.name, lastId: it.mbid).save(flush: true, failOnError: true)
				}
				
				def userArtist = UserArtist.findByUserAndArtist(user, artist)
				
				if (!userArtist) {
	//				log.info "Top user artist not present: ${artist.name}"
					userArtist = new UserArtist(artist: artist, user: user).save(flush: true, failOnError: true)
//					artist.addToUserArtists(userArtist)
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
