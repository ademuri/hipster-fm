package com.ademuri.hipster

import org.springframework.dao.DataIntegrityViolationException

import com.ademuri.hipster.UserAlbum;

class UserAlbumController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
		def list
        params.max = Math.min(max ?: 10, 100)
		
		if (params?.sort == 'lastId') {
			// lastId is a transient property, so Hibernate freaks out if we try to sort by it
			log.info "sort by lastId"
			def newParams = params.findAll {
				!(it.key == 'sort')
			}
			log.info "params: ${newParams}"
			list = UserAlbum.list(newParams)
			list.sort {
				it.lastId
			}
			if (params.order == 'desc') {
				list.reverse(true)
			}
			
			
		} else {
			log.info "normal sort"
			list = UserAlbum.list(params)
			log.info "list: ${list}"
		}
		
        [albumInstanceList: list, albumInstanceTotal: UserAlbum.count()]
    }

    def create() {
        [albumInstance: new UserAlbum(params)]
    }

    def save() {
        def albumInstance = new UserAlbum(params)
        if (!albumInstance.save(flush: true)) {
            render(view: "create", model: [albumInstance: albumInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'album.label', default: 'Album'), albumInstance.id])
        redirect(action: "show", id: albumInstance.id)
    }

    def show(Long id) {
        def albumInstance = UserAlbum.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        [albumInstance: albumInstance]
    }

    def edit(Long id) {
        def albumInstance = UserAlbum.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        [albumInstance: albumInstance]
    }

    def update(Long id, Long version) {
        def albumInstance = UserAlbum.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (albumInstance.version > version) {
                albumInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'album.label', default: 'Album')] as Object[],
                          "Another user has updated this Album while you were editing")
                render(view: "edit", model: [albumInstance: albumInstance])
                return
            }
        }

        albumInstance.properties = params

        if (!albumInstance.save(flush: true)) {
            render(view: "edit", model: [albumInstance: albumInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'album.label', default: 'Album'), albumInstance.id])
        redirect(action: "show", id: albumInstance.id)
    }

    def delete(Long id) {
        def albumInstance = UserAlbum.get(id)
        if (!albumInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
            return
        }

        try {
            albumInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'album.label', default: 'Album'), id])
            redirect(action: "show", id: id)
        }
    }
}
