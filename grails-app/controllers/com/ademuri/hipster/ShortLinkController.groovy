package com.ademuri.hipster

import grails.converters.JSON
import grails.plugin.cache.CacheEvict;
import grails.plugin.cache.Cacheable;

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.dao.DataIntegrityViolationException

class ShortLinkController {
	
	LinkGenerator grailsLinkGenerator

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [shortLinkInstanceList: ShortLink.list(params), shortLinkInstanceTotal: ShortLink.count()]
    }

    def create() {
        [shortLinkInstance: new ShortLink(params)]
    }

    def save() {
        def shortLinkInstance = new ShortLink(params)
		log.info "about to save"
        if (!shortLinkInstance.save(flush: true)) {
            render(view: "create", model: [shortLinkInstance: shortLinkInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), shortLinkInstance.id])
        redirect(action: "show", id: shortLinkInstance.id)
    }

    def show(Long id) {
        def shortLinkInstance = ShortLink.get(id)
        if (!shortLinkInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), id])
            redirect(action: "list")
            return
        }

        [shortLinkInstance: shortLinkInstance]
    }

    def edit(Long id) {
        def shortLinkInstance = ShortLink.get(id)
        if (!shortLinkInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), id])
            redirect(action: "list")
            return
        }

        [shortLinkInstance: shortLinkInstance]
    }

    def update(Long id, Long version) {
        def shortLinkInstance = ShortLink.get(id)
        if (!shortLinkInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (shortLinkInstance.version > version) {
                shortLinkInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'shortLink.label', default: 'ShortLink')] as Object[],
                          "Another user has updated this ShortLink while you were editing")
                render(view: "edit", model: [shortLinkInstance: shortLinkInstance])
                return
            }
        }

        shortLinkInstance.properties = params

        if (!shortLinkInstance.save(flush: true)) {
            render(view: "edit", model: [shortLinkInstance: shortLinkInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), shortLinkInstance.id])
        redirect(action: "show", id: shortLinkInstance.id)
    }

    def delete(Long id) {
        def shortLinkInstance = ShortLink.get(id)
        if (!shortLinkInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), id])
            redirect(action: "list")
            return
        }

        try {
            shortLinkInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'shortLink.label', default: 'ShortLink'), id])
            redirect(action: "show", id: id)
        }
    }
	
	def shortToFull(String shortUrl) {
		def theId = ShortLink.findId(shortUrl)
		log.info "short to full id: ${theId}, ${shortUrl}"
		def shortLinkInstance = ShortLink.get(theId)
		if (!shortLinkInstance) {
			flash.message = "Error: short URL ${shortUrl} does not exist"
			redirect(action: "list")
			return
		}
		
		redirect(uri: shortLinkInstance.fullUrl)
	}
	
	def ajaxShortenUrl(String fullUrl) {
		log.info "shorten: ${fullUrl}"
		fullUrl = fullUrl.substring(1, fullUrl.length())
		fullUrl = fullUrl.substring(fullUrl.indexOf('/'), fullUrl.length())
		
		def shortLinkInstance = ShortLink.findByFullUrl(fullUrl)
		if (!shortLinkInstance) {
			shortLinkInstance = new ShortLink(fullUrl: fullUrl).save(failOnError: true)
		}
		log.info "shortUrl: ${shortLinkInstance.shortUrl}"
		
		def url = grailsLinkGenerator.serverBaseURL + "/s/" + shortLinkInstance.shortUrl
		
		def data = [url: url]
		render data as JSON
	}
}
