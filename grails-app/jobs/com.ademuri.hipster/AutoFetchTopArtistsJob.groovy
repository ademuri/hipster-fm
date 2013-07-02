package com.ademuri.hipster

import grails.util.Environment;
import com.ademuri.hipster.User;



class AutoFetchTopArtistsJob {
	def graphDataService
	
	static def SEC = 1000l
	static def MIN = SEC * 60
	static def HOUR = MIN * 60
	
    static triggers = {
		simple repeatInterval: 3*HOUR, startDelay: 5*MIN // start 5m after server starts, run every 3 hours
		//cron name: 'cronTrigger', cronExpression: '0 0 2 ? * MON-FRI'
    }


    def execute() {
		User.withNewSession {
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
}
