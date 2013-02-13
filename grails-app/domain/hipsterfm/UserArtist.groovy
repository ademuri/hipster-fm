package hipsterfm

class UserArtist {
	
	public String toString() {
		return artist.name
	}
	
	public String encodeAsHTML = {
		return toString()
	}
	
	String getName() {
		return artist.name
	}
	String getLastId() {
		return artist.lastId
	}
	
	String name
	String lastId
	long numScrobbles = 0
	
	Date lastSynced
	
	static transients = ["name", "lastId"]
	
	static belongsTo = [user: User, artist: Artist]
	static hasMany = [tracks: Track, albums: UserAlbum]

    static constraints = {
		lastSynced nullable: true
    }
}
