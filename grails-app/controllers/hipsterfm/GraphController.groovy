package hipsterfm

import grails.converters.JSON

class GraphController {

    def index() { }
	
	def show() {
		def artist = Artist.findByName("Emancipator")
//		def user = User.findByUsername("Adamsmasher")
//		def userArtist = UserArtist.findByUserAndArtist(user, artist)
		
		def data = []
		def users = []
		
		def intervalSize = 14 	// about a month
		
		artist.userArtists.each { userArtist ->
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
			
			
			for (int i=0; i<(end-start); i+=1) {
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
		[chartdata:chartdata as JSON]
		//data as JSON
	}
}
