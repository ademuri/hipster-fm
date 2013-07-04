modules = {
    application {
		defaultBundle 'core'
        resource url:'js/application.js'
    }
	def jqplot_dir = 'js/jqplot-1.0.4r1121'
	jqplot {
		defaultBundle 'core'
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
		defaultBundle 'lib'
		resource url:"${spectrum_dir}/spectrum.js"
		resource url:"${spectrum_dir}/spectrum.css"
	}
	
	def store_dir = 'js/store.js'
	store_js {
		defaultBundle 'lib'
		dependsOn "jquery"
		resource url:"${store_dir}/store.js"
		resource url:"${store_dir}/json.js"
	}
	
	def d3_dir = 'js/d3-v3'
	d3 {
		defaultBundle 'lib'
		resource url:"${d3_dir}/d3.v3.js"
	}
	
	def irex_dir = 'js/inputosaurus-text'
	irex {
		defaultBundle 'lib'
		dependsOn ['jquery', 'jquery-ui']
		resource url:"${irex_dir}/inputosaurus.js"
		resource url:"${irex_dir}/inputosaurus.css"
	}
	
	overrides {
		jquery {
			defaultBundle 'core'
		}
		
		'jquery-ui' {
			resource id:'theme', url:'/css/jquery-ui.css'	
		}
	}
}