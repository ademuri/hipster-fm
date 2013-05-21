package com.ademuri.hipster

import grails.converters.JSON

class GraphDataCache {
	
	Date startDate
	Date endDate
	Long tickSize
	Long intervalSize
	Long userMaxY
	Long groupBy
	Long albumId
	boolean removeOutliers
	String userArtists 	// yes, this is gross.
	
	def chartdata
	String chartdataJSON
	Long hitsSinceSync = 0
	Date dateCreated
	Date lastUpdated
	
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
	
	static mapping = {
		autoTimestamp true
	}

    static constraints = {
		dateCreated(nullable: true)
		lastUpdated(nullable: true)
		hitsSinceSync(nullable: true)
		
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
