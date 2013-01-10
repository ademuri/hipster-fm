import grails.util.Environment

class DisableFilters {
//	def disable = {
//		return true
//	}
	def filters = {
		monkeyEditCheck(controller: '*', action: 'edit') {
			before = {
				if (Environment.getCurrent() == Environment.PRODUCTION) {
					log.info "Tried to access edit"
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyDeleteCheck(controller: '*', action: 'delete') {
			before = {
				if (Environment.getCurrent() == Environment.PRODUCTION) {
					log.info "Tried to access delete"
					return false
				} else {
					return true
				}
			}
		}
		
	}

}
