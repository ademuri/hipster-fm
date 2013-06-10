package com.ademuri.hipster

class Track {
	
	public String toString() {
		return "${artist.name} - ${name} (${date})"
	}
	
	static belongsTo = [artist: UserArtist, album: UserAlbum]
	
	String name
	String lastId
	Date date
	
	Date dateCreated
	
	static mapping = {
		sort "date"
		autoTimestamp true
	}
	
    static constraints = {
		dateCreated(nullable: true)
		date nullable: false
		artist nullable: false
		name nullable: false
		album nullable: true
    }
}
