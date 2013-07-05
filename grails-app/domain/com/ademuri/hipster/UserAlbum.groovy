package com.ademuri.hipster

class UserAlbum {
	
	public String toString() {
		return "${album.name} (parent id: ${album.id})"
	}
	
	public String encodeAsHTML = {
		return toString()
	}
	
	String getName() {
		return album.name
	}
	String getLastId() {
		return album.lastId
	}
	
	String name
	String lastId
	
	static belongsTo = [artist: UserArtist, album: Album]
	static hasMany = [tracks: Track]
	static transients = ["name", "lastId"]
	
	static mapping = {
		version false
		dynamicUpdate true
		id generator: 'hilo', params: [table: 'hilo', column: 'next_value', max_lo: 100]
	}

    static constraints = {
		lastId(nullable: false, blank: false)
		name(blank: true)
		artist(nullable: false)
		album(nullable: false)
    }
}
