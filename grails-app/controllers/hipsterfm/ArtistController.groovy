package hipsterfm

import com.eaio.util.text.HumanTime
import org.springframework.dao.DataIntegrityViolationException


class ArtistController {
	
	def lastFmService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [artistInstanceList: Artist.list(params), artistInstanceTotal: Artist.count()]
    }

    def create() {
        [artistInstance: new Artist(params)]
    }

    def save() {
        def artistInstance = new Artist(params)
        if (!artistInstance.save(flush: true)) {
            render(view: "create", model: [artistInstance: artistInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'artist.label', default: 'Artist'), artistInstance.id])
        redirect(action: "show", id: artistInstance.id)
    }

    def show(Long id) {
        def artistInstance = Artist.get(id)
        if (!artistInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
            redirect(action: "list")
            return
        }

        [artistInstance: artistInstance]
    }

    def edit(Long id) {
        def artistInstance = Artist.get(id)
        if (!artistInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
            redirect(action: "list")
            return
        }

        [artistInstance: artistInstance]
    }

    def update(Long id, Long version) {
        def artistInstance = Artist.get(id)
        if (!artistInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (artistInstance.version > version) {
                artistInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'artist.label', default: 'Artist')] as Object[],
                          "Another user has updated this Artist while you were editing")
                render(view: "edit", model: [artistInstance: artistInstance])
                return
            }
        }

        artistInstance.properties = params

        if (!artistInstance.save(flush: true)) {
            render(view: "edit", model: [artistInstance: artistInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'artist.label', default: 'Artist'), artistInstance.id])
        redirect(action: "show", id: artistInstance.id)
    }

    def delete(Long id) {
        def artistInstance = Artist.get(id)
        if (!artistInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
            redirect(action: "list")
            return
        }

        try {
            artistInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'artist.label', default: 'Artist'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'artist.label', default: 'Artist'), id])
            redirect(action: "show", id: id)
        }
    }
	
	def sync(Long id) {
		def artistInstance = Artist.get(id)
		if (!artistInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
			redirect(action: "list")
			return
		}
		log.info "Sync for artist ${artistInstance.name}"
		
		def cutoffDate = (new Date())-7
		def cutoffTS = cutoffDate.toTimestamp()
		def found
		
		def users = User.findAll()
		users.each {
			log.info "user: ${it}, not found last synced: ${it.notFoundLastSynced}, cutoff: ${cutoffDate}, ts: ${cutoffTS}"
			if (it.notFoundLastSynced[artistInstance.name]) {
				log.info "type: ${it.notFoundLastSynced[artistInstance.name].class}"
				//def notFoundDate = new Date((long)it.notFoundLastSynced[artistInstance.name]  * 1000L)
				//log.info "not found date: ${notFoundDate}"
			}
			
			if (UserArtist.findByUserAndArtist(it, artistInstance)?.lastSynced < cutoffDate
				|| (it.notFoundLastSynced[artistInstance.name] && it.notFoundLastSynced[artistInstance.name].before(cutoffTS))) {
				def success = false
				def count = 0
				while (success == false && count < 2) {
					//try {
						log.info "Syncing ${artistInstance.name} for ${it.username}"
						found = lastFmService.getArtistTracks(it, artistInstance.name)
						if (found == 0) {
							it.notFoundLastSynced[artistInstance.name] = new Date() 
						}
						success = true
					/*} catch(Exception e) {
						log.warn "Encountered ${e.message} while syncing ${artistInstance.name} for ${it.username}"
						e.printStackTrace()
						Thread.sleep(10000)
					}*/
					count++
				}
			}
		}
		
		redirect(action: "show", id: id)
	}
	
	def first(Long id) {
		def artistInstance = Artist.get(id)
		if (!artistInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'artist.label', default: 'Artist'), id])
			redirect(action: "list")
			return
		}
		log.info "First for artist ${artistInstance.name}"
		
		def users = []
		
		artistInstance.userArtists.user.each { it ->
			def user = [:]
			user.id = it.id
			user.name = it.username
			def artist = it.artists.find{it.lastId == artistInstance.lastId}
			user.artist_id = artist.id
			def track = Track.withCriteria(uniqueResult: true) {
				eq("artist", artist)
				maxResults(1)
				order("date", "asc")
			}
			
			user.date = track.date
			user.track_id = track.id
			users.add(user)
		}
		users.sort {
			it.date
		}
		
		// pretty dates
		users[0].pretty_date = ""
		for (int i=1; i<users.size(); i++) {
			users[i].pretty_date = HumanTime.approximately((users[i].date.time - users[i-1].date.time))
		}
		
		[artist: artistInstance, users: users]
	}
}
