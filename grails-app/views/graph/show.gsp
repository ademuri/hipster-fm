<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Show Graph</title>
		<r:require modules="jquery, jqplot" />
	</head>
	<body>
		<div id="chartdiv" style="height:800px;width:900px;"></div>
		
		<script>
			
			$(document).ready(function() {
				var chartdata = ${chartdata};
				var series = chartdata.series;
				var data = chartdata.data;

				
				var jqplotOptions = {
					seriesDefaults: {
						markerOptions: {show: false}
					},
					axes: {
						xaxis: {
							renderer: $.jqplot.DateAxisRenderer,
							pad: 0
						},
						yaxis: {
							padMin: 0,
							padMax: .5
						}
					},
					legend: {
						show: true,
						location: 's',
						placement: 'outsideGrid'
					},
					series: series
				};
				$.jqplot('chartdiv', data,
						jqplotOptions);
			});
			console.log(${chartdata});
		</script>
		
	</body>
</html>