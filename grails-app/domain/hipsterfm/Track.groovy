package hipsterfm

class Track {
	
	public String toString() {
		return "${artist.name} - ${name} (${date})"
	}
	
	static belongsTo = [artist: UserArtist, album: Album]
	
	String name
	String lastId
	Date date
	
	static mapping = {
		sort "date"
	}
	
    static constraints = {
		date nullable: false
		artist nullable: false
		name nullable: false
		album nullable: true
    }
}
