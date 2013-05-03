modules = {
    application {
        resource url:'js/application.js'
    }
	def jqplot_dir = 'js/jqplot-1.0.4r1121'
	jqplot {
		dependsOn "jquery"
		resource url:"${jqplot_dir}/jquery.jqplot.js"
		resource url:"${jqplot_dir}/plugins/jqplot.dateAxisRenderer.js"
		resource url:"${jqplot_dir}/plugins/jqplot.enhancedLegendRenderer.js"
		resource url:"${jqplot_dir}/jquery.jqplot.css"
		resource url:"css/jqplot_custom.css"
	}
	
	def spectrum_dir = 'js/spectrum'
	spectrum {
		dependsOn "jquery"
		resource url:"${spectrum_dir}/spectrum.js"
		resource url:"${spectrum_dir}/spectrum.css"
	}
	
	def store_dir = 'js/store.js'
	store_js {
		dependsOn "jquery"
		resource url:"${store_dir}/store.js"
		resource url:"${store_dir}/json.js"
	}
}