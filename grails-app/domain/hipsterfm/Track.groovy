package hipsterfm

class Track {
	
	public String toString() {
		return "${artist.name} - ${name} (${date})"
	}
	
	static belongsTo = [artist: UserArtist]
	
	String name
	String lastId
	def albumId
	Date date
	
	static mapping = {
		sort "date"
	}
	
    static constraints = {
		albumId nullable: true
		date nullable: false
		artist nullable: false
		name nullable: false
    }
}
