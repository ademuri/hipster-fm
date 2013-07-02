package com.ademuri.hipster

import grails.converters.JSON
import org.springframework.dao.DataIntegrityViolationException

import com.ademuri.hipster.User;
import com.ademuri.hipster.UserArtist;

class UserController {
	
	def lastFmService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

//    def list(Integer max) {
//        params.max = Math.min(max ?: 10, 100)
//        [userInstanceList: User.list(params), userInstanceTotal: User.count()]
//    }

    def create() {
        [userInstance: new User(params)]
    }

    def save() {
        def userInstance = new User(params)
        if (!userInstance.save(flush: true)) {
            render(view: "create", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect(action: "show", id: userInstance.id)
    }

    def show(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }
		
        [userInstance: userInstance]
    }

    def edit(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        [userInstance: userInstance]
    }

    def update(Long id, Long version) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (userInstance.version > version) {
                userInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'user.label', default: 'User')] as Object[],
                          "Another user has updated this User while you were editing")
                render(view: "edit", model: [userInstance: userInstance])
                return
            }
        }

        userInstance.properties = params

        if (!userInstance.save(flush: true)) {
            render(view: "edit", model: [userInstance: userInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect(action: "show", id: userInstance.id)
    }

    def delete(Long id) {
        def userInstance = User.get(id)
        if (!userInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
            return
        }

        try {
            userInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), id])
            redirect(action: "show", id: id)
        }
    }
	
	def getFriends(Long id) {
		def userInstance = User.get(id)
		if (!userInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
			redirect(action: "list")
			return
		}
		
		lastFmService.getFriends(userInstance)
		redirect(action: "list")
	}
	
	def find() {
		if (params.username) {
			def user = User.findByUsername(params.username)
			if (!user) {
				if (!lastFmService.checkIfUserExists(params.username)) {
					flash.message = "User ${params.username} doesn't exist"
					return
				}
				
				// user exists in last.fm, create user
				user = new User(username: params.username).save(failOnError: true)
			}
			
			redirect(action: "show", id: user.id)
			return
		}
	}
	
	def ajaxGetTopArtists(Long id) {
		def interval = "3month"
		if (params?.interval) {
			interval = UserArtist.rankNames[params.interval as int]
		}
		
		def topArtists = lastFmService.getUserTopArtists(id, interval)
		
		def userInstance = User.get(id)
		if (!userInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), id])
			redirect(action: "list")
			return
		}
		
		render (template: 'updateArtists', model: ["artistList": topArtists, "user": userInstance.username])
	}
	
	def ajaxGetUserList() {
		log.info "get user list"
		def users = User.list()
		def names = users.username
		
		render names as JSON
	}
}
