package hipsterfm

class User implements Comparable {
	
	String username
	String email
	String name
	
	
	Map<String, Date> notFoundLastSynced = [:]		// artists that had no tracks -> when it was checked
	Date friendsLastSynced
	
	static hasMany = [artists: UserArtist, friends: User, topArtists: UserArtist]
	
//	Set friends
	
    static constraints = {
		username(blank: false, nullable: false, unique: true)
		email(blank: true, nullable: true)
		name(blank: true, nullable: true)
		friendsLastSynced(nullable: true)
    }
	
	static mapping = {
		notFoundLastSynced type: Date
	}
	
	public String toString() {
		return username
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof User) {
//			log.warn "compare ${this.username}, ${o.username}"
			return this.username <=> o.username
		}
		
		return this.hashCode() <=> o.hashCode()
	}
	
	@Override
	public boolean equals(User u) {
//		log.warn "equals ${this.username}, ${u.username}"
		return u.username == username
	}
	
	@Override public int hashCode() {
		return username.hashCode()
	}
	
	
}
