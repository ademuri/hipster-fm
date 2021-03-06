package com.ademuri.hipster

import java.text.SimpleDateFormat;

class Track {
	
	public String toString() {
		return "${artist.name} - ${name} (${getDateString()})"
	}
	
	static belongsTo = [artist: UserArtist, album: UserAlbum]
	
	String name
	String lastId
	Date date
	Integer dayOfWeek
	Integer hourOfDay
	
	Date dateCreated
	
	
	def tz = TimeZone.getTimeZone("GMT")
//	def tz = TimeZone.getDefault()
	
	String getDateString() {
		return date.format("dd MMM yyyy, kk:mm", tz)
	}
	
	static mapping = {
		sort "date"
		autoTimestamp true
		dayOfWeek formula: 'DAYOFWEEK(date)'
		hourOfDay formula: 'HOUR(date)'
		
		// once created, a track should never change
		version false
		dynamicUpdate true
		
		// avoid concurrency issues
		id generator: 'hilo', params: [sequence: 'hi_value', max_lo: 32767]
	}
	
    static constraints = {
		dateCreated(nullable: true)
		date nullable: false
		artist nullable: false
		name nullable: false, maxSize: 1000
		album nullable: true
    }
}
