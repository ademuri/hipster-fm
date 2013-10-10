<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Show Graph</title>
		<r:require modules="jquery, d3, store_js" />
	</head>
	<body>
		<div id="progress">
		</div>
		
		<div id="show-graph" class="content" role="main">
			<h1>${artistName}<g:if test="${userName && artistName}"> - </g:if>${userName}</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="shorten-link-button"><h5>Link to this page</h5>
			<input id="shorten-link-result" type="text" readonly="" />
			</div>
		
			<div id="chartdiv"></div>
			
		</div>
		
		<style>
			path {
				stroke: blue;
				stroke-width: 2;
				fill: none;
			}
			.axis path {
				stroke: #333;
				stroke-width: 1;
				fill: none;
			}
			g.legend {
				fill: none;
			}
			g.legend text {
				fill: #000;	
			}
			.hover path,.click path {
				stroke-width: 5;
			}
			.hover text,.click text {
				font-weight: bold;
			}
		</style>

		<script>
			var fastResponse;
			var response = null;
			var responseObject;
			var maxY;
			var params = []; 
			var ONE_DAY = 1000 * 60 * 60 * 24;
			var fullGraphDone = false;

			var rawParams = $(location).attr('href');
			rawParams = rawParams.substring(rawParams.indexOf('?')+1, rawParams.length);
			params = rawParams.split('&');
			params.sort();

			var tries = 0;
			function resetAndGraph() {
				console.log("reset and graph");
				tries++;
				if (tries > 2) {
					$("#chartdiv").text("Error getting data");
					return;
				}
				
				try {
					responseObject = JSON.parse(response.responseText);
				}
				catch (e) {
					console.log("Got error getting data, trying again");
					getData();
					return;
				}
				fullGraphDone = true;
				responseObject.date = new Date();
				<g:if env="production">
					store.set(JSON.stringify(params), responseObject);
				</g:if>
				
				
				if (responseObject.error) {
					$("#chartdiv").text(responseObject.error);
					return;
				}

				var oldGraph = $("#chartdiv svg");
				if (oldGraph) {
					oldGraph.remove();
				}

				graph();
			}
			
			function fastGraph() {
				if (fullGraphDone) {
					return;
				}
				
				console.log("fast graph");
				if (!responseObject) {
					try {
						responseObject = JSON.parse(fastResponse.responseText);
						if (responseObject['error'] != null) {
							return;
						}
					} 
					catch (e) {
						console.log("Got error getting fast data");
						return;
					}
				}
				graph();
			}
			
			function graph() {
				var colors = store.get('colors-graph');
				if (!colors) {
					// TODO: make default colors exist somewhere instead of hardcoding it
					colors = ["#FF0000", "#FFBA10", "#970CE8", "#0D4EFF", "#E87C15", 
													"#1ECC21", "#00E8C2", "#E232EA", "#4F826A", "#999999",
													"#333333", "#804000", "#FF6AD7", "#80002E", "#77AAFF"
													];
					//console.log("setting colors: " + colors);
				}
				
				var chartdata = responseObject.chartdata;
				maxY = chartdata.maxY;
				var series = chartdata.series;
				var data = chartdata.data;
				var rawTime = chartdata.time;

				var allData = [];	// for determining max values
				$.each(data, function(index, value) {
					$.each(value, function(index2, number) {
						allData.push(number);
					});
				});
				console.log(chartdata);

				var time = [];
				var parseDate = d3.time.format("%Y-%m-%d").parse;
				$.each(rawTime, function(index, t) {
					time.push(parseDate(t));
				});

				// compose the data into format that's easy to use with D3
				// format: list of (date, [user1 point], [user2 point], ...)
				var coolData = [];

				// add time
				for (var i=0; i<time.length; i++) {
					var tmp = new Object();
					tmp.date = time[i];
					coolData.push(tmp);
				}
				// add users
				for (var i=0; i<series.length; i++) {
					for (var j=0; j<time.length; j++) {
						coolData[j][series[i].label] = data[i][j];
					}
				}

				// margin convention http://bl.ocks.org/mbostock/3019563
				var m = {top: 50, right: 200, bottom: 50, left: 50};
				
				var w = Math.max($(window).width()-100, 640) - m.left - m.right,
					h = Math.max($(window).height()-50, 480) - m.top - m.bottom;

				// make our container the actual size, for spacing
				$("#chartdiv").attr("width", w + m.left + m.right).attr("height", h + m.top + m.bottom);

				var x = d3.time.scale().range([0, w]);
				x.domain(d3.extent(coolData, function(d) { return d.date; }));
				var y = d3.scale.linear().domain([0, d3.max(allData)]).range([h, 0]);

				var chart = d3.select("#chartdiv")
					.append("svg")
					.attr("width", w + m.left + m.right)
					.attr("height", h + m.top + m.bottom)
					.append("g")
					.attr("transform", "translate(" + m.left + "," + m.top + ")");

				var line = d3.svg.line()
					.x(function(d) {
						return x(d.date); 
					})
					.y(function(d) { 
						return y(d.count); 
					})
					.interpolate("monotone");

				var xAxis = d3.svg.axis().scale(x).orient("bottom");
				chart.append("g")
					.attr("class", "x axis")
					.attr("transform", "translate(0," + h + ")") 
					.call(xAxis);

				var yAxis = d3.svg.axis().scale(y).ticks(5).orient("left");
				chart.append("g")
					.attr("class", "y axis")
					//.attr("transform", "translate(-50,0)")
					.call(yAxis);

				var color = d3.scale.category10()
					.domain(d3.keys(coolData[0]).filter(function(key) { return key !== "date"; })).range(colors);

				var users = color.domain().map(function(name) {
					return {
						name: name,
						values: coolData.map(function(d) {
							return {date: d.date, count: +d[name]};
						})
					};
				});
			

				var user = chart.selectAll(".user")
					.data(users)
					.enter().append("g")
					.attr("class", "user");

				user.append("path")
					.attr("class", "line")
					.attr("d", function(d) { return line(d.values); })
					.style("stroke", function(d) { return color(d.name); })
					.on("mouseover", lineover)
					.on("mouseout", lineout)
					.on("click", lineclick)
					;

				user.append("text")
					.datum(function(d) { return {name: d.name, value: d.values[d.values.length-1]}; })
					.attr("transform", function(d) { return "translate(" + x(d.value.date) + "," + y(d.value.count) + ")"; })
					.attr("x", 3)
					.attr("dy", ".35em")
//					.text(function(d) { return d.name; });
					//.style("stroke", function(d) {
					//	console.log("d: " + d.name + ", color: " + color(d.name)); 
					//	return color(d.name);});
					
				
				// space out the user labels if they overlap
				var prevHeight = 0;
				var prevEl;
				$("g.user text").each(function(index, el) {
					var top = $(el).position().top;
					var bottom = top - $(el).height();
					//if (prevEl && (top < prevHeight) && (bottom > prevHeight)) {
					if (prevEl) {
						$(el).position({'my': 'bottom', 'at': 'top', 'of': $(prevEl)}); 
					}
					
					prevHeight = top;
					prevEl = el;
				});
	
				// This legend code is a bit of a hack, but it works
				var legend = user.append('g')
					.attr('class', 'legend');

				legend.append('rect')
					.attr('x', w - 20)
					.attr('y', function(d, i){ return i * 22;})
			        .attr('width', 10)
			        .attr('height', 10)
			        .style('fill', function(d) { 
			          return color(d.name);
			        })
			        .on("mouseover", legendover)
			        .on("mouseout", legendout);

			    legend.append('text')
			        .attr('x', w - 8)
			        .attr('y', function(d, i){ return (i *  22) + 9;})
			        .text(function(d){ return d.name; })
			        .on("mouseover", legendover)
			        .on("mouseout", legendout)
			        .on("click", legendclick);

		        // center the chart
		        $('html, body').animate({
			        scrollTop: $("#show-graph").offset().top
		        }, 1000);
			}

			function userHover(parent) {
				var oldClass = parent.attr("class");
				parent.attr("class", oldClass + " hover");
			}

			function userUnHover(parent) {
				var oldClass = parent.attr("class");
				parent.attr("class", oldClass.replace("hover", ""));
			}

			function lineover(d, i) {
				var parent = $(d3.select(this).node()).parent();
				userHover(parent);
			}

			function lineout(d, i) {
				var parent = $(d3.select(this).node()).parent();
				userUnHover(parent);
			}

			function legendover(d, i) {
				var parent = $(d3.select(this).node()).parent().parent();
				userHover(parent);
			}
			
			function legendout(d, i) {
				var parent = $(d3.select(this).node()).parent().parent();
				userUnHover(parent);
			}


			function doclick(parent) {
				var oldClass = parent.attr("class");
				
				var clicked = $(".click");
				clicked.each(function(i, e) {
					var theClass = $(e).attr("class");
					$(e).attr("class", theClass.replace("click", ""));
					
				});

				// If we clicked on a focused element, unfocus it
				if (oldClass.indexOf("click") == -1) {
					parent.attr("class", oldClass + " click");
				}
			}
			
			// code duplication is bad, kids. stay in school!
			function legendclick(d, i) {
				var parent = $(d3.select(this).node()).parent().parent();
				doclick(parent);				
			}

			function lineclick(d, i) {
				var parent = $(d3.select(this).node()).parent();
				doclick(parent);
			}
			
			function showShortened(data, textStatus) {
				$("#shorten-link-result").show();
				$("#shorten-link-result").val(data.url);
				$("#shorten-link-result").select();
				$("#shorten-link-result").attr('size', $("#shorten-link-result").val().length);
				
				$("#shorten-link-button").click( function() {
					$("#shorten-link-result").select();
				});	
			}
			
			function errorFunc(XMLHttpRequest,textStatus,errorThrown) {
				console.log("error: " + textStatus + ", " + errorThrown);	
			}

			// called when page is loaded & if data is malformed
			function getData() {
				response = ${remoteFunction(action: "ajaxGraphData", onComplete: "resetAndGraph()", params: params)};
			}

			// called when the page is loaded
			function firstGetData() {
				fastResponse = ${remoteFunction(action: "ajaxFastGraphData", onComplete: "fastGraph()", params: params)};	// fetch stale data first & graph while waiting for full data
				getData();
			}
			
			$(window).load(function() {
				// deal with short link
				$("#shorten-link-result").hide();
				$("#shorten-link-button").click( function() {
					$("#shorten-link-button").unbind();
					$("#shorten-link-result").show;
					
					// ajax call to get shortened url
					$.ajax({type:'POST',data:{fullUrl: window.location.pathname + window.location.search}, url:'${createLink(controller: 'shortLink', action: 'ajaxShortenUrl')}',success:showShortened,error:errorFunc});
				});
			
				<g:if env="production">
					var storedData = store.get(JSON.stringify(params));
	
					// cache result for 1 day
					if (storedData && storedData['version'] == 1 &&  ((new Date()) - (new Date(storedData.date))) < ONE_DAY) {
						responseObject = storedData;
						graph();
						return
					}
				</g:if>

				firstGetData();
			});
		</script>
		
		<g:form method="get" >
			<g:render template="window"/>
			<g:each var="p" in="${params}">
				<g:if test="${!(['tickSize', 'intervalSize', 'startDate', 'endDate', 
					'_action_show', 'action', 'controller', 'userMaxY'].contains(p.key ))}">
					<g:hiddenField name="${p.key}" value="${p.value}" />
				</g:if> 				
			</g:each>
			<fieldset class="buttons">
				<g:actionSubmit class="submit" action="show" value="Submit"  />
			</fieldset>
		</g:form>
		
	</body>
</html>