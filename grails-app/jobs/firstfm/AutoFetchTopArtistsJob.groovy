package firstfm

import hipsterfm.User;



class AutoFetchTopArtistsJob {
    static triggers = {
      simple repeatInterval: 5000l // execute job once in 5 seconds
		//cron name: 'cronTrigger', cronExpression: '0 0 2 ? * MON-FRI'
    }


    def execute() {
		
    }
}
