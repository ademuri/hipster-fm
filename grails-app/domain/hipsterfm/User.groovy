package hipsterfm

class User {
	
	String username
	String email
	String name
	
	
	Map<String, Date> notFoundLastSynced = [:]		// artists that had no tracks -> when it was checked
	
	static hasMany = [artists: UserArtist, friends: User]
	
    static constraints = {
		username(blank: false, nullable: false)
		email(blank: true, nullable: true)
		name(blank: true, nullable: true)
    }
	
	static mapping = {
		notFoundLastSynced type: Date
	}
	
	public String toString() {
		return username
	}
}
