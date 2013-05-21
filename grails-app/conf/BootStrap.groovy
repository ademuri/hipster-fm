import com.ademuri.hipster.Artist
import com.ademuri.hipster.GraphDataCache
import com.ademuri.hipster.Track
import com.ademuri.hipster.User
import com.ademuri.hipster.UserArtist
import grails.converters.JSON
import grails.util.Environment

class BootStrap {

    def init = { servletContext ->
		if (Environment.getCurrent() == Environment.DEVELOPMENT) {
			User adam = new User(username: "Adamsmasher", email: "adam@ademuri.com").save(flush: true, failOnError: true)
			User doug = new User(username: "Warmsounds", email: "").save(flush: true, failOnError: true)
			
			User gary = new User(username: "DoktorKen", email: "").save(failOnError: true)
			User dan = new User(username: "drhjort", email: "").save(failOnError: true)
			User blake = new User(username: "johnsonblake1", email: "").save(failOnError: true)
			User hershey = new User(username: "kwhershey", email: "").save(failOnError: true)
			User viraj = new User(username: "Sinhahaha", email: "").save(failOnError: true)
			User ian = new User(username: "unknownmosquito", email: "").save(failOnError: true)
			
			def edit = new Artist(name: "Edit", lastId: "1234").save(failOnError: true)
			def editUser = new UserArtist(artist: edit, user: adam, lastSynced: new Date()-20).save(failOnError: true)
			edit.addToUserArtists(editUser)
			def track = new Track(artist: editUser, date: new Date()-10, name: "The Herp", lastId: "5678").save(failOnError: true)
			editUser.addToTracks(track)
			def cache = new GraphDataCache(tickSize: 20, intervalSize: 20, groupBy: 0, userArtists: ([1] as JSON).toString(),
				chartdataJSON: '{"series":[{"label":"Adamsmasher"}],"data":[[["2010-10-30",67],["2010-12-14",59],["2011-01-28",5],["2011-03-14",0],["2011-04-28",23],["2011-06-12",0],["2011-07-27",0],["2011-09-10",5],["2011-10-25",0],["2011-12-09",7],["2012-01-23",6],["2012-03-08",10],["2012-04-22",39]]],"maxY":0}',
				hitsSinceSync: 5, dateCreated: new Date()-7, lastUpdated: new Date()-7).save(failOnError: true)
			
//			def em = new Artist(name: "Emancipator", lastId: "aa1d4315-5246-42b2-b62b-f997d046d8b2").save(flush: true, failOnError: true)
//			def rand = new Random()
//			
//			def emAdam = new UserArtist(artist: em, user: adam).save(flush: true, failOnError: true)
//			adam.addToArtists(emAdam)
//			(0..10).each {
//				(0..rand.nextInt(10)).each {
//					emAdam.addToTracks(new Track(date: new Date()-(15+it), name: "First Snow", artist: emAdam, lastId: "herp derp").save(flush: true, failOnError: true))
//				}
//			}
//			
//			def emDoug = new UserArtist(artist: em, user: doug).save(flush: true, failOnError: true)
//			doug.addToArtists(emDoug)
//			emDoug.addToTracks(new Track(date: new Date()-10, name: "Wolf Drawn", artist: emDoug, lastId: "derp herp").save(flush: true, failOnError: true))
		}
    }
    def destroy = {
    }
}
