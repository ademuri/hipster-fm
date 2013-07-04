package com.ademuri.hipster

class Album {
	
	public String toString() {
		return name
	}
	
	public String encodeAsHTML = {
		return toString()
	}
	
	String name
	String lastId
	
	static belongsTo = [artist: Artist]
	static hasMany = [userAlbums: UserAlbum]
	
	static mapping = {
		// once created, an album should never change
		version: false
		dynamicUpdate: true
	}

    static constraints = {
		lastId(nullable: false)
    }
}
