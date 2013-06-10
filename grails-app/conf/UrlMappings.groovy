class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		
		"/s/$shortUrl"(controller: "shortLink", action: "shortToFull")

		"/"(controller:"graph", action:"setup")
		"500"(view:'/error')
	}
}
