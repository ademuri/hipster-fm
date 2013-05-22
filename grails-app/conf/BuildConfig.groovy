grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()
		
		grailsRepo "http://grails.org/plugins"
		ebr()

        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime 'mysql:mysql-connector-java:5.1.20'
		compile "org.codehaus.gpars:gpars:1.0.0"
    }

    plugins {
        runtime ":hibernate:$grailsVersion"

        build ":tomcat:$grailsVersion"
		
		compile ':cache:1.0.1'
		compile ':cache-headers:1.1.5'
		compile ':cached-resources:1.0'
		compile ':form-helper:0.2.8'
		compile ':hibernate-stats:1.1'
		compile ':jquery:1.7.2'
		compile ':jquery-ui:1.8.7'
		compile ':pretty-time:2.1.3.Final-1.0.1'
		compile ':quartz:1.0-RC7'
		compile ':quartz-monitor:0.3-RC1'
		compile ':resources:1.2.RC2'
		compile ':rest:0.7'
		compile ':webxml:1.4.1'
		compile ':yui-minify-resources:0.1.5'
		compile ':zipped-resources:1.0'
    }
}
