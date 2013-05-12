package hipsterfm

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
	}
	
    static constraints = {
		dateCreated()
		date nullable: false
		artist nullable: false
		name nullable: false
		album nullable: true
    }
}
