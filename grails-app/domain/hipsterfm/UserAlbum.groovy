package hipsterfm

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

    static constraints = {
		lastId(nullable: false, blank: false)
		name(blank: true)
		artist(nullable: false)
    }
}
