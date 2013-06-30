import grails.util.Environment

class DisableFilters {
	def filters = {
		monkeyEditCheck(controller: '*', action: 'edit') {
			before = {
				if (Environment.getCurrent() != Environment.DEVELOPMENT) {
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyDeleteCheck(controller: '*', action: 'delete') {
			before = {
				if (Environment.getCurrent() != Environment.DEVELOPMENT) {
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyCreateCheck(controller: '*', action: 'create') {
			before = {
				if (Environment.getCurrent() != Environment.DEVELOPMENT) {
					return false
				} else {
					return true
				}
			}
		}
		
		monkeyUpdateCheck(controller: '*', action: 'update') {
			before = {
				if (Environment.getCurrent() != Environment.DEVELOPMENT) {
					return false
				} else {
					return true
				}
			}
		}
		
		monkeySaveCheck(controller: '*', action: 'save') {
			before = {
				if (Environment.getCurrent() != Environment.DEVELOPMENT) {
					return false
				} else {
					return true
				}
			}
		}
		
	}

}
