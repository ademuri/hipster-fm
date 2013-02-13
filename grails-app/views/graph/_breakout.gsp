<div id="show-graph" class="content" role="main">
			<h1>${artistName} - ${userName}</h1>
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
					seriesColors: ["#FF0000", "#FFBA10", "#970CE8", "#0D4EFF", "#E87C15", 
									"#1ECC21", "#00E8C2", "#E232EA", "#4F826A",
									],
					seriesDefaults: {
						fill: true,
						shadow: false,
						disableStack: false,
						markerOptions: {
							show: true, 
							onlyFirst: true
						},
						rendererOptions: {
							smooth: true,
							
							fillToZero: true,
						},
					},
					axes: {
						xaxis: {
							renderer: $.jqplot.DateAxisRenderer,
							pad: 0
						},
						yaxis: {
							<g:if test="${maxY}">
							max: ${maxY},
							</g:if>
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
					series: series,
					stackSeries: true
				};
				
				$.jqplot('chartdiv', data,
						jqplotOptions);
			});
			console.log(${chartdata});
		</script>