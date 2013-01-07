modules = {
    application {
        resource url:'js/application.js'
    }
	def jqplot_dir = 'js/jqplot-1.0.4r1121'
	jqplot {
		dependsOn "jquery"
		resource url:"${jqplot_dir}/jquery.jqplot.js"
		resource url:"${jqplot_dir}/plugins/jqplot.dateAxisRenderer.js"
		resource url:"${jqplot_dir}/jquery.jqplot.css"
	}
}