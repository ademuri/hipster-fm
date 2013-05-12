package firstfm

import grails.util.Environment;
import hipsterfm.User;



class AutoUpdateGraphDataCacheJob {
	def graphDataService
	
	static def SEC = 1000l
	static def MIN = SEC * 60
	static def HOUR = MIN * 60
	
    static triggers = {
      simple repeatInterval: 6*HOUR, startDelay: 1*HOUR // start 1h after server starts, run every 6 hours
		//cron name: 'cronTrigger', cronExpression: '0 0 2 ? * MON-FRI'
    }


    def execute() {
		log.info "Running autoUpdateGraphDataCache"
		Environment.executeForCurrentEnvironment {
			production {
				graphDataService.autoUpdateGraphDataCache()
			}
		}
    }
}
