package com.ademuri.hipster

import java.text.DateFormat;

import grails.converters.JSON
import groovyx.gpars.GParsPool
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.Criteria;
import org.hibernate.StaleObjectStateException
import org.javasimon.SimonManager;

import com.ademuri.hipster.Album;
import com.ademuri.hipster.Artist;
import com.ademuri.hipster.User;
import com.ademuri.hipster.UserArtist;
import org.springframework.transaction.annotation.Transactional

class GraphController {

	def lastFmService
	def graphDataService
	def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
	
	def  heatmapTypes = ['DayAndHour', 'Day', 'Hour']
	
	def index() {
		redirect(action: 'setup')
	}
	
	@Transactional
    def setup(String user) {
		def friends
		def topArtists
		
		def interval = params.interval ?: "3month"
		
		if (user && user != "") {
			def userInstance = User.findByUsername(user)
			
			if (userInstance) {
//				log.info "Found user ${user}"
				lastFmService.getFriends(userInstance.id)
				friends = userInstance.friends
				
				lastFmService.getUserTopArtists(userInstance.id, interval)
				if (userInstance.artists.size() > 0) {
//					topArtists = userInstance.artists.sort { it.numScrobbles }.reverse().getAt(0..(Math.min(userInstance.artists.size()-1, 30)))
					topArtists = UserArtist.withCriteria {
						eq("user", userInstance)
						eq("isTop${interval}", true)
						between("top${interval}Rank", 1, 30)
						order("top${interval}Rank", "asc")
					}
				}
			}
		}
		
		def newParams = params
		newParams.putAll([friends: friends, user: user, topArtists: topArtists, interval: interval])
		if (params.artist) {
			newParams.artistName = params.artist
		}
		
		
		return newParams
	}
	
	@Transactional
	def search(String user, String artist) {
		def usernameList = user.tokenize(',')
		Set userList = []
		
		for(String username : usernameList) {
			def userInstance = User.findByUsername(username)
			if (!userInstance) {
				log.info "Didn't find user with username ${username}"
				userInstance = new User(username: username).save(flush: true, failOnError: true)
			}
			userList.add(userInstance)
		}
		
		// grab users from the checkboxes
		params.each {
			if (it.key.contains("u_")) {
				if (it.value.contains("on")) {
					def userInstance = User.get((it.key - "u_") as Long)
					
					// note: this shouldn't happen unless people are monkeying around with the page
					if (!userInstance) {
						return	//only skips this checkbox
					}
					userList.add(userInstance)
				}
			}
		}
		
		// if 'Add all friends with artist' is checked, and there is only one user from ^^^,
		// 		fetch the friends from last.fm and add them all
		if (userList.size() == 1 && params.addAllFriends) {
			def userInstance = userList.toList()[0]
			log.info "Adding all friends of user ${userInstance}"
			lastFmService.getFriends(userInstance.id)
			userList.addAll(userInstance.friends)
		}
		
		if (userList.size() == 0) {
			flash.message = "Please choose at least one user"
			redirect(action: "setup")
			return 
		}
		
		def artistInstance = lastFmService.getArtist(artist)
		if (!artistInstance) {
			log.warn "Didn't find artist ${artist}"
			flash.message = "Didn't find artist ${artist}"
			redirect(action: "setup")
			return
		}
		
//		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
//		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
//		log.info "start date: ${startDate}, end date: ${endDate}"
		
		def newParams = [a_0: artistInstance.id, removeOutliers: params.removeOutliers?.contains("on") ? true : false,
			tickSize: params.tickSize, intervalSize: params.intervalSize, addAllFriends: params.addAllFriends]

		def album = params.album ? Album.findWhere(artist: artistInstance, name: params.album).id : ""
//		log.info "search album: ${album}"	
		
		if (album) {
			newParams.albumId = album
		}
		if (params.startDate) {
			newParams.startDate = params.startDate
		}
		if (params.endDate) {
			newParams.endDate = params.endDate
		}
		if(params.userMaxY) {
			newParams.userMaxY = params.userMaxY
		}
		
		userList.eachWithIndex { it, i ->
			newParams["u_${i}"] = it.id
		}
		
		if (userList.size() == 1) {
			newParams.userName = userList.iterator()[0].toString()
		}
		
		redirect(action: "show", params: newParams)
	}
	
	@Transactional
	def show() {
		if (params["_action_show"]) {
			params.remove("_action_show")
		}
		
		def userId
		
		def newParams = params
		
		def artists = []
		def artistName = ""
		params.each {
			if (it.key.startsWith("a_")) {
				artists.push(it.value)
			}
		}
		for(int i=0; i<artists.size()-1; i++) {
			artistName += (Artist.get(artists.get(i)).toString() + ", ") ?: null
		}
		if (artists.size() > 0) {
			artistName += Artist.get(artists.last()).toString() ?: null
		} 
		newParams.artistName = artistName
		
		def userIdList = []
		params.each {
			if (it.key.contains("u_")) {
				userIdList.push(it.value)
			}
		}
		
		if (userIdList.size() == 1) {
			newParams.userName = User.get(userIdList[0])
		} 
		
		params
	}
	
	def setupHeatmap() {
		[heatmapTypes: heatmapTypes]
	}
	
	def heatmapSearch() {
		def graphs = []
		params.each {
			if (it.key.startsWith("artist")) {
				def graph = [:]
				def theId = it.key.substring(6, it.key.length())
				
				graph.artist = it.value
				graph.user = params["user${theId}"]
				graph.type = params["type${theId}"]
				graph.index = theId as Long
				
				graphs.push(graph)
			}
		}
		
		def newParams = [:]
		for(def i=0; i<graphs.size(); i++) {
			def graph = graphs[i]
			def userName = graph.user
			def artistName = graph.artist
			
			if (!userName || !artistName) {
				flash.message = "Please specify a user and artist"
				redirect(action: "setupHeatmap")
				return
			}
			if (!graph.type) {
				flash.message = "Please specify a type"
				redirect(action: "setupHeatmap")
				return
			}
			
			def user = lastFmService.getUser(userName)
			def artist = lastFmService.getArtist(artistName)
			
			if (!artist) {
				flash.message = "Artist not found: ${artistName}"
			}
			if (!user) {
				flash.message = "User not found: ${userName}"
			}
			if (!user || !artist) {
				redirect(action: "setupHeatmap")
				return
			}
			
			newParams["u_${graph.index}"] = user.id
			newParams["a_${graph.index}"] = artist.id
			newParams["t_${graph.index}"] = graph.type
		}
		
		redirect(action: "heatmap", params: newParams)
	}
	
	@Transactional
	def heatmap() {
		def graphs = []
		params.each {
			if (it.key.startsWith("a_")) {
				def graph = [:]
				def theId = it.key.substring(2, it.key.length())
				
				graph.artist = it.value as Long
				graph.user = params["u_${theId}"] as Long
				graph.type = params["t_${theId}"]
				graph.index = theId as Long
				
				def user = User.get(graph.user)
				def artist = Artist.get(graph.artist)
				if (user && artist) {
					graph.userName = user.toString()
					graph.artistName = artist.name
				}
				
				graphs.push(graph)
			}
		}
		
		[graphs: graphs]
	}
	
	def ajaxHeatmapData() {
		def userId = params?.u as Long
		def artistId = params?.a as Long
		
		lastFmService.getArtistTracks(userId, artistId)
		
		if (!userId || !artistId) {
			flash.message = "Please specify a user and artist"
			redirect(action: "setupHeatmap")
			return
		}
		
		def c = UserArtist.createCriteria()
		def userArtist = c.get {
			projections {
				createAlias('user', '_user')
				createAlias('artist', '_artist')
				eq('_user.id', userId)
				eq('_artist.id', artistId)
			}
		}
		
		
		def data = []
		if (params.t == 'DayAndHour') {
			(1..7).each { day ->
				def criteria = Track.createCriteria()
				def dataList = criteria.list {
					eq('dayOfWeek', day)
					projections {
						createAlias('artist', '_artist')
						eq('artist.id', userArtist.id)
						groupProperty('hourOfDay')
						rowCount()
					}
				}
				
				def toAdd = []
				dataList.each {
					def point = [:]
					point.day = day
					point.hour = it[0]
					point.count = it[1]
					toAdd << point
				}
				(0..23).each { hour ->
					if (!toAdd.find { it.hour == hour }) {
						toAdd << [day: day, hour: hour, count: 0]
					}
				}
				data.addAll(toAdd)
				
			}
		}
		else if (params.t == 'Day') {
			(1..7).each { day ->
				def criteria = Track.createCriteria()
				def dataList = criteria.list {
					eq('dayOfWeek', day)
					projections {
						createAlias('artist', '_artist')
						eq('artist.id', userArtist.id)
						rowCount()
					}
				}
				
				dataList.each {
					def point = [:]
					point.day = day
					point.count = it
					data << point
				}
			}
			(1..7).each { day ->
				if (!data.find { it.day == day }) {
					data << [day: day, count: 0]
				}
			}
		} else if (params.t == 'Hour') {
			(0..23).each { hour ->
				def criteria = Track.createCriteria()
				def dataList = criteria.list {
					eq('hourOfDay', hour)
					projections {
						createAlias('artist', '_artist')
						eq('artist.id', userArtist.id)
						rowCount()
					}
				}
				
				dataList.each {
					def point = [:]
					point.hour = hour
					point.count = it
					data << point
				}
			}
			(0..23).each { hour ->
				if (!data.find { it.hour == hour }) {
					data << [hour: hour, count: 0]
				}
			}
		}
		
		render data as JSON
	}
	
	def sessionFactory
	
	
	def ajaxGraphData = {
		Boolean removeOutliers = params.removeOutliers == "true"
		def userIdList = []
		def artistUnsortedList = []	// grab key-value pairs, then sort them
		def artistIdList = []
		def userArtistIdList = []
		def flush = {
			def session = sessionFactory.currentSession
			session.flush()
			session.clear()
			propertyInstanceMap.get().clear()
		}
		
		def stopwatch = SimonManager.getStopwatch("getArtistTracks")
		def stopDownload = SimonManager.getStopwatch("download")
		def insertDownload = SimonManager.getStopwatch("insert")
		
		def by = params.by ? params.by as int : graphDataService.kByUser

		// users		
		params.each {
			if (it.key.startsWith("u_")) {
				userIdList.push(it.value)
			}
		}
		
		userIdList = userIdList.sort()
		
		// artists
		params.each {
			if (it.key.startsWith("a_")) {
				artistUnsortedList.push(it)
			}
		}
		
		artistUnsortedList.sort { a, b ->
			if (!(a && b)) {
				return a <=> b
			}
			
			return (a.key.substring(2, a.key.size()) as long) <=> (b.key.substring(2, b.key.size()) as long)
		}
		
		artistUnsortedList.each {
			artistIdList.push(it.value)
		}
		
		def out
		log.trace "Getting tracks..."
		GParsPool.withPool(3) {
			userIdList.eachParallel { userId ->
				GParsPool.withPool(3) {
					artistIdList.eachParallel { artistId ->
						Track.withNewSession {
							out = lastFmService.getArtistTracks(userId, artistId)
						}
					}
				}
			}
		}
		// this is a hack, but we want the request actually fetching data to flush first (and give it time to complete)
		if (out == 1) {
			Thread.sleep(1000)
		}
		
		flush()
		
		def albumId
		if (params["albumId"]) {
			albumId = params["albumId"] as Long
		}
		def albumName = albumId ? Album.get(albumId).name : ""

		def users = []
		
		def intervalSize = 25.0 	// about a month
		def tickSize = 25.0
		if (params.tickSize) {
			tickSize = params.tickSize as long
		}
		if (params.intervalSize) {
			intervalSize = params.intervalSize as long
		}
		
		def userMaxY
		if (params.userMaxY) {
			userMaxY = params.userMaxY as Integer
		}		
		
		def startDate = params.startDate ? params.date('startDate', "MM/dd/yyyy") : null
		def endDate = params.endDate ? params.date('endDate', "MM/dd/yyyy") : null
		
		log.trace "Getting graph data"
		def getData = {
			graphDataService.getGraphData(userIdList, artistIdList, startDate, endDate, tickSize, intervalSize, removeOutliers, userMaxY, by, albumId)
		}
		
		def chartdata
		try {
			chartdata = getData()
		} 
		catch(StaleObjectStateException e) {
			log.warn "Got stale object exception, trying again"
			flush()
			Thread.sleep(5000)
			flush()
			try {
				chartdata = getData()
			}
			catch(StaleObjectStateException f) {
					flush()
					Thread.sleep(5000)
					flush()
				try {
					chartdata = getData()
				}
				catch(StaleObjectStateException g) {
					chartdata = null
				}
			}
		}
		
		if (!chartdata) {
			log.error "getGraphData returned no data"
			def error = [error: "No scrobbles found"]
			render error as JSON
			return
		}
		
		log.info stopwatch
		log.info stopDownload
		log.info insertDownload
		stopwatch.reset()
		stopDownload.reset()
		insertDownload.reset()
		
		def theData = [chartdata:chartdata]
		render theData as JSON
	}
	
	
	def fetchTopArtists() {
		graphDataService.autoUpdateUsers()
		return
	}
	
	def updateCache() {
		graphDataService.autoUpdateGraphDataCache()
		return
	}
}
