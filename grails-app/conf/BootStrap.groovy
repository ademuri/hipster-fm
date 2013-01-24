import hipsterfm.Artist
import hipsterfm.Track
import hipsterfm.User
import hipsterfm.UserArtist
import grails.util.Environment

class BootStrap {

    def init = { servletContext ->
		if (Environment.getCurrent() != Environment.PRODUCTION) {
			User adam = new User(username: "Adamsmasher", email: "adam.demuri@gmail.com").save(flush: true, failOnError: true)
			User doug = new User(username: "Warmsounds", email: "").save(flush: true, failOnError: true)
			
			User gary = new User(username: "DoktorKen", email: "").save(failOnError: true)
			User dan = new User(username: "drhjort", email: "").save(failOnError: true)
			User blake = new User(username: "johnsonblake1", email: "").save(failOnError: true)
			User hershey = new User(username: "kwhershey", email: "").save(failOnError: true)
			User viraj = new User(username: "Sinhahaha", email: "").save(failOnError: true)
			User ian = new User(username: "unknownmosquito", email: "").save(failOnError: true)
			
			def em = new Artist(name: "Emancipator", lastId: "aa1d4315-5246-42b2-b62b-f997d046d8b2").save(flush: true, failOnError: true)
			def rand = new Random()
			
			def emAdam = new UserArtist(artist: em, user: adam).save(flush: true, failOnError: true)
			adam.addToArtists(emAdam)
			(0..10).each {
				(0..rand.nextInt(10)).each {
					emAdam.addToTracks(new Track(date: new Date()-(15+it), name: "First Snow", artist: emAdam, lastId: "herp derp").save(flush: true, failOnError: true))
				}
			}
			
			def emDoug = new UserArtist(artist: em, user: doug).save(flush: true, failOnError: true)
			doug.addToArtists(emDoug)
			emDoug.addToTracks(new Track(date: new Date()-10, name: "Wolf Drawn", artist: emDoug, lastId: "derp herp").save(flush: true, failOnError: true))
		}
    }
    def destroy = {
    }
}
