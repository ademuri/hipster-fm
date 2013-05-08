import org.apache.log4j.DailyRollingFileAppender

// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

// local configuration of DB stuff
//def extConfig = System.properties.getProperty('EXTCONFIG')
def extConfig = System.getenv().get('EXTCONFIG').toString()
log.error "extConfig: ${extConfig}"
grails.config.locations = [ "file:/opt/grails-config/config.groovy", hipsterfm.DatabaseCredentials ]

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        grails.serverURL = "http://www.ademuri.com/hipster"
    }
}

// log4j configuration
dev = {
	log4j = {
		// Example of changing the log pattern for the default console appender:
		//
		//appenders {
		//    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
		//}
	
		error  'org.codehaus.groovy.grails.web.servlet',        // controllers
			   'org.codehaus.groovy.grails.web.pages',          // GSP
			   'org.codehaus.groovy.grails.web.sitemesh',       // layouts
			   'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			   'org.codehaus.groovy.grails.web.mapping',        // URL mapping
			   'org.codehaus.groovy.grails.commons',            // core / classloading
			   'org.codehaus.groovy.grails.plugins',            // plugins
			   'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
			   'org.springframework',
			   'org.hibernate',
			   'net.sf.ehcache.hibernate'
			   
		warn 'groovyx.net.http'
	
		info   'grails.app'
	}
}

//String commonPattern = "%d{yyyy-MMM-dd HH:mm:ss,SSS} [%t] %c %x%n %-5p %m%n"
def commonPattern = "%d{yyyy-MMM-dd HH mm ss,SSS} [%t] %c %x%n %-5p %m%n"
def logDirectory = "/opt/apache-tomcat-7.0.34/logs"
environments {
	development(dev)
	test(dev)
	
	production {
		log4j = {
			info 'grails.app'
			warn 'groovyx.net.http'
			error 'org.codehaus.groovy.grails.web.servlet',        // controllers
			   'org.codehaus.groovy.grails.web.pages',          // GSP
			   'org.codehaus.groovy.grails.web.sitemesh',       // layouts
			   'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
			   'org.codehaus.groovy.grails.web.mapping',        // URL mapping
			   'org.codehaus.groovy.grails.commons',            // core / classloading
			   'org.codehaus.groovy.grails.plugins',            // plugins
			   'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
			   'org.springframework',
			   'org.hibernate',
			   'net.sf.ehcache.hibernate'
			   
			   appenders {
				   file name: "errors", file: "${logDirectory}/hipster-errors.log",
				   		layout: pattern(conversionPattern: commonPattern)
					appender new DailyRollingFileAppender(
		               name:"roll", datePattern: "'.'yyyy-MM-dd",
		               file:"${logDirectory}/hipster-rolling.log",
		               layout: pattern(conversionPattern: commonPattern))
					
					file name: "prod-errors", file: "${logDirectory}/hipster-errors.log",
						layout: pattern(conversionPattern: commonPattern)
					appender new DailyRollingFileAppender(
						  name:"prod-roll", datePattern: "'.'yyyy-MM-dd",
						  file:"${logDirectory}/hipster-errors-daily.log",
						  layout: pattern(conversionPattern: commonPattern))
			   }
			   
			   root {
				   info "prod-roll", "prod-errors", "roll", "errors"
			   }
		}
	}
	
	
}
