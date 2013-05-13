package hipsterfm

import grails.converters.JSON

class GraphDataCache {
	
	Date startDate
	Date endDate
	
	Date dateCreated
	
	Long tickSize
	Long intervalSize
	Long userMaxY
	Long groupBy
	Long albumId
	
	boolean removeOutliers
	
	def chartdata
	
	String chartdataJSON

		String userArtists 	// yes, this is gross.
	
	def afterLoad() {
		if (chartdataJSON) {
			chartdata = JSON.parse(chartdataJSON)
		}
	}
	
	def beforeValidate() {
		if (chartdata) {
			chartdataJSON = chartdata as JSON
		}
	}
	
	static transients = ['chartdata']

    static constraints = {
		dateCreated()
		
		startDate(nullable: true)
		endDate(nullable: true)
		userMaxY(nullable: true)
		albumId(nullable: true)
		
		removeOutliers(nullable: false)
		groupBy(nullable: false)
		tickSize(nullable: false)
		intervalSize(nullable: false)
		
		chartdataJSON(maxSize: 10000, blank: true)
		userArtists(maxSize: 1000, blank: false)
    }
}
