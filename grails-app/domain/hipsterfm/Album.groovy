package hipsterfm

class Album {
	
	public String toString() {
		return name
	}
	
	public String encodeAsHTML = {
		return toString()
	}
	
	String name
	String lastId
	
	static belongsTo = [artist: UserArtist]
	static hasMany = [tracks: Track]

    static constraints = {
		lastId(nullable: false, blank: false, unique: true)
    }
}
