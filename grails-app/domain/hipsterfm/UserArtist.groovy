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
	
	boolean isTop7day = false,
			isTop1month = false,
			isTop3month = false,
			isTop6month = false,
			isTop12month = false,
			isTopoverall = false
			
	int top7dayRank,
		top1monthRank,
		top3monthRank,
		top6monthRank,
		top12monthRank,
		topoverallRank
		
	def ranks = [top7dayRank,
				top1monthRank,
				top3monthRank,
				top6monthRank,
				top12monthRank,
				topoverallRank]
	
	Date lastGraphed
	
	static rankNames = ["7day", "1month", "3month", "6month", "12month", "overall"]
	static humanRankNames = ["7 days", "1 month", "3 months", "6 months", "12 months", "Overall" ] 
	
	Date lastSynced
	
	static transients = ["name", "lastId", "ranks"]
	
	static belongsTo = [user: User, artist: Artist]
	static hasMany = [tracks: Track, albums: UserAlbum]

    static constraints = {
		lastSynced nullable: true
		lastGraphed nullable: true
    }
}
