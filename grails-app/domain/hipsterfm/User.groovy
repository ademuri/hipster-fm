package hipsterfm

class User {
	
	String username
	String email
	
	static hasMany = [artists: UserArtist]
	
    static constraints = {
		username(blank: false, nullable: false)
		email()
    }
	
	public String toString() {
		return username
	}
}
