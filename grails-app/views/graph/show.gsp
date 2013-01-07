<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Show Graph</title>
		<r:require modules="jquery, jqplot" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" controller="artist" action="list">Artists</g:link></li>
			</ul>
		</div>
		
		<div id="show-graph" class="content" role="main">
			<h1>${artistName}</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
		
			<div id="chartdiv" style="height:800px;width:95%;"></div>
			
		</div>
		
		<script>
			
			$(document).ready(function() {
				var chartdata = ${chartdata};
				var series = chartdata.series;
				var data = chartdata.data;

				
				var jqplotOptions = {
					seriesDefaults: {
						markerOptions: {
							show: true, 
							onlyFirst: true
						},
						shadow: false
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
						renderer: $.jqplot.EnhancedLegendRenderer,
						show: true,
						location: 's',
						placement: 'outsideGrid',
						seriesToggle: true
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