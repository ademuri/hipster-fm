import grails.util.Environment

class DisableFilters {
//	def disable = {
//		return true
//	}
	
	def filters = {
		monkeyEditCheck(controller: '*', action: 'edit') {
			before = {
				if (Environment.getCurrent() == Environment.DEVELOPMENT) {
					log.info "Tried to access edit"
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyDeleteCheck(controller: '*', action: 'delete') {
			before = {
				if (Environment.getCurrent() == Environment.DEVELOPMENT) {
					log.info "Tried to access delete"
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyCreateCheck(controller: '*', action: 'create') {
			before = {
				if (Environment.getCurrent() == Environment.DEVELOPMENT) {
					log.info "Tried to access create"
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyUpdateCheck(controller: '*', action: 'update') {
			before = {
				if (Environment.getCurrent() == Environment.DEVELOPMENT) {
					log.info "Tried to access update"
					return false
				} else {
					return true
				}
			}
		}
		
		monkeySaveCheck(controller: '*', action: 'save') {
			before = {
				if (Environment.getCurrent() == Environment.DEVELOPMENT) {
					log.info "Tried to access save"
					return false
				} else {
					return true
				}
			}
		}
		
	}

}
