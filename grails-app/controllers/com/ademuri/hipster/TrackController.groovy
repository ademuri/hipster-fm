package com.ademuri.hipster

import org.springframework.dao.DataIntegrityViolationException

import com.ademuri.hipster.Track;

class TrackController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [trackInstanceList: Track.list(params), trackInstanceTotal: Track.count()]
    }

    def create() {
        [trackInstance: new Track(params)]
    }

    def save() {
        def trackInstance = new Track(params)
        if (!trackInstance.save(flush: true)) {
            render(view: "create", model: [trackInstance: trackInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'track.label', default: 'Track'), trackInstance.id])
        redirect(action: "show", id: trackInstance.id)
    }

    def show(Long id) {
        def trackInstance = Track.get(id)
        if (!trackInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'track.label', default: 'Track'), id])
            redirect(action: "list")
            return
        }

        [trackInstance: trackInstance]
    }

    def edit(Long id) {
        def trackInstance = Track.get(id)
        if (!trackInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'track.label', default: 'Track'), id])
            redirect(action: "list")
            return
        }

        [trackInstance: trackInstance]
    }

    def update(Long id, Long version) {
        def trackInstance = Track.get(id)
        if (!trackInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'track.label', default: 'Track'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (trackInstance.version > version) {
                trackInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'track.label', default: 'Track')] as Object[],
                          "Another user has updated this Track while you were editing")
                render(view: "edit", model: [trackInstance: trackInstance])
                return
            }
        }

        trackInstance.properties = params

        if (!trackInstance.save(flush: true)) {
            render(view: "edit", model: [trackInstance: trackInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'track.label', default: 'Track'), trackInstance.id])
        redirect(action: "show", id: trackInstance.id)
    }

    def delete(Long id) {
        def trackInstance = Track.get(id)
        if (!trackInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'track.label', default: 'Track'), id])
            redirect(action: "list")
            return
        }

        try {
            trackInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'track.label', default: 'Track'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'track.label', default: 'Track'), id])
            redirect(action: "show", id: id)
        }
    }
}
