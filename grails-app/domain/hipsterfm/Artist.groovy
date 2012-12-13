package hipsterfm

class Artist {
	
	public String toString() {
		return name
	}
	
	String name
	String lastId

    static constraints = {
		name nullable: false
		lastId nullable: false
    }
	
	static hasMany = [userArtists: UserArtist]
}
