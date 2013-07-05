package com.ademuri.hipster

class Track {
	
	public String toString() {
		return "${artist.name} - ${name} (${date})"
	}
	
	static belongsTo = [artist: UserArtist, album: UserAlbum]
	
	String name
	String lastId
	Date date
	Integer dayOfWeek
	Integer hourOfDay
	
	Date dateCreated
	
	static mapping = {
		sort "date"
		autoTimestamp true
		dayOfWeek formula: 'DAYOFWEEK(date)'
		hourOfDay formula: 'HOUR(date)'
		
		// once created, a track should never change
		version false
		dynamicUpdate true
		
		// avoid concurrency issues
		id generator: 'hilo', params: [sequence: 'hi_value', max_lo: 1000]
	}
	
    static constraints = {
		dateCreated(nullable: true)
		date nullable: false
		artist nullable: false
		name nullable: false
		album nullable: true
		dayOfWeek nullable: true
		hourOfDay nullable: true
    }
}
