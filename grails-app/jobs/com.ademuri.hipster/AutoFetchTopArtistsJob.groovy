package com.ademuri.hipster

import grails.util.Environment;
import com.ademuri.hipster.User;



class AutoFetchTopArtistsJob {
	def graphDataService
	
	static def SEC = 1000l
	static def MIN = SEC * 60
	static def HOUR = MIN * 60
	
    static triggers = {
      simple repeatInterval: 3*HOUR, startDelay: 10*MIN // start 10m after server starts, run every 3 hours
		//cron name: 'cronTrigger', cronExpression: '0 0 2 ? * MON-FRI'
    }


    def execute() {
		Environment.executeForCurrentEnvironment {
			production {
				graphDataService.autoUpdateUsers()
			}
			stage {
				graphDataService.autoUpdateUsers()
			}
		}
    }
}
