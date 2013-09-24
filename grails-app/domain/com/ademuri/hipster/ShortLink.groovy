package com.ademuri.hipster

import grails.util.Environment;

class ShortLink {
	
	static Long curId = -1
	
	String shortUrl
	String fullUrl

    static constraints = {
		shortUrl(nullable: false, blank: false)
		fullUrl(nullable: false, blank: false, maxSize: 5000)
    }
	
	/*static mapping = {
		shortUrl index: "Short_Url_Idx"
	}*/
	
	def beforeValidate() {
		if (!shortUrl || shortUrl.size() == 0) {
			if (curId < 0) {
				def tmp = ShortLink.withCriteria {
					maxResults 1
					order "id", "desc"
				}
				if (tmp) {
					curId = tmp.get(0).id + 1
					log.info "Found max id, ${curId-1}"
				} else {
					log.info "Setting current id as 1"
					curId = 1
				}
			}
			
			log.info "curId: ${curId}"
			shortUrl = Base62.encode(curId)
			curId++
		}
	}
	
	static Long findId(String shortUrl) {
		// hack since decode returns a byte array
		// http://stackoverflow.com/questions/1026761/how-to-convert-a-byte-array-to-its-numeric-value-java
		def theId = Base62.decode(shortUrl) as Long
		return theId
	}
	
	def getCanonicalShort() {
		return grails.serverUrl
	}
}