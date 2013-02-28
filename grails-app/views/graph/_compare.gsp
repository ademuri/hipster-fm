	
var jqplotOptions = {
	seriesColors: ["#FF0000", "#FFBA10", "#970CE8", "#0D4EFF", "#E87C15", 
					"#1ECC21", "#00E8C2", "#E232EA", "#4F826A", "#999999",
					"#333333", "#804000", "#FF6AD7", "#80002E", "#77AAFF"
					],
	seriesDefaults: {
		markerOptions: {
			show: true, 
			onlyFirst: true
		},
		shadow: false,
		rendererOptions: {
			smooth: true,
		},
	},
	axes: {
		xaxis: {
			renderer: $.jqplot.DateAxisRenderer,
			pad: 0,
			padMin: 0,
		},
		yaxis: {
			min: 0,
			padMin: 0,
			padMax: .5,
			tickOptions: {
				formatString: '%.0f'
			}
		}
	},
	legend: {
		renderer: $.jqplot.EnhancedLegendRenderer,
		show: true,
		location: 's',
		placement: 'outsideGrid',
		seriesToggle: true
	},
	series: series
};