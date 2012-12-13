package firstfm

import hipsterfm.Artist
import hipsterfm.UserArtist
import hipsterfm.Track
import java.text.SimpleDateFormat

class LastFmService {
	
	public static String api = '9cc72e8864ba4ec102ee347ec557f262'
	public static String userAgent = 'Grails Last.Fm Notifier Service'

	//http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=rj&api_key=b25b959554ed76058ac220b7b2e0a026&format=json
	
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
	
	def getArtistTracks(user, rawArtistName) {
		withRest(uri:"http://ws.audioscrobbler.com/") {
			def query = [
				method: 'user.getartisttracks',
				user: user.username,
				artist: rawArtistName,
				api_key: api,
				format: 'json'
				]
			
			def resp = get(path: '/2.0/', query: query)
			
			def data = resp.getData()
			log.info "user ${user}, artist ${rawArtistName}, data: ${data}"
			if (data.artisttracks?.items && data.artisttracks?.items.toInteger() == 0) {
				log.warn "Found no results for user ${user}, artist ${rawArtistName}"
				return
			}
			
			def tracks = data.artisttracks.track
			
			
			// grab the earliest scrobbles
			def paging = data.artisttracks."@attr"
			log.info "Got ${paging.totalPages} pages for search ${rawArtistName}"
			log.info "total pages is integer: ${paging.totalPages.isInteger()}"
			if (paging.totalPages.toInteger() > 1) {
				query["page"] = paging.totalPages
				
				resp = get(path: '/2.0/', query: query)
				data = resp.getData()
				
				if (paging.totalPages == 2)
				{
					data.artisttracks.track.each {
						tracks.add(it)
					}
				} else {
					query["page"] -= 1
					
					resp = get(path: '/2.0/', query: query)
					data = resp.getData()
					data.artisttracks.track.each {
						tracks.add(it)
					}
				}
			}
			
			log.info "Found ${tracks.size()} tracks."
			
			println "print"
			println data.artisttracks.track[0]
			
			def artistName = tracks[0]?.artist?."#text"
			
			
			def artistId = tracks[0]?.artist?.mbid
			if (!artistId) {
				log.warn "Search for ${rawArtistName} returned no tracks"
				return
			}
			println "artist name: ${artistName}"
			def artist = Artist.findByLastId(artistId) ?: new Artist(name: artistName, lastId: artistId).save(flush: true, failOnError: true)
			def userArtist = UserArtist.findByUserAndArtist(user, artist) ?: new UserArtist(user: user, artist: artist).save(flush: true, failOnError: true)
			artist.addToUserArtists(userArtist)
			
			// example: 19 Jun 2012, 21:16
			def dateFormat = new SimpleDateFormat("dd MMM yyyy, kk:mm")
			
			tracks.each {
				def trackId = it.mbid
				def date = dateFormat.parse(it.date."#text")
				def track = Track.findByLastIdAndDate(trackId, date) ?: new Track(name: it.name, date: date, artist: userArtist, lastId: trackId).save(flush: true, failOnError: true)
				userArtist.addToTracks(track)
			}
			
			userArtist.lastSynced = new Date()
		}
	}
}
