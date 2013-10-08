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
		
			<div id="chartdiv" style="height:800px;width:95%;"></div>
			
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
		</style>
		
		<script>
			var response;
			var responseObject;
			var maxY;
			var params = []; 
			var ONE_DAY = 1000 * 60 * 60 * 24;

			var rawParams = $(location).attr('href');
			rawParams = rawParams.substring(rawParams.indexOf('?')+1, rawParams.length);
			params = rawParams.split('&');
			params.sort();
			
			var tries = 0;
			function graph() {
				tries++;
				if (tries > 2) {
					$("#chartdiv").text("Error getting data");
					return;
				}
				if (!responseObject) {
					try {
						responseObject = JSON.parse(response.responseText);
					}
					catch (e) {
						console.log("Got error getting data, trying again");
						getData();
						return;
					}
					responseObject.date = new Date();
					<g:if env="production">
						store.set(JSON.stringify(params), responseObject);
					</g:if>
				}
				
				if (responseObject.error) {
					$("#chartdiv").text(responseObject.error);
					return;
				}
				
				var chartdata = responseObject.chartdata;
				maxY = chartdata.maxY;
				var series = chartdata.series;
				var data = chartdata.data;
				var rawTime = chartdata.time;
				console.log("series");
				console.log(series);
				//console.log("data");
				//console.log(data);
				//console.log("rawTime");
				//console.log(rawTime);

				var allData = [];	// for determining max values
				$.each(data, function(index, value) {
					$.each(value, function(index2, number) {
						allData.push(number);
					});
				});
				//console.log(allData);
				
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

				console.log("coolData");
				console.log(coolData);
				
				// margin convention http://bl.ocks.org/mbostock/3019563
				var m = {top: 50, right: 120, bottom: 50, left: 50};
				
				var w = 960 - m.left - m.right,
					h = 600 - m.top - m.bottom;

				var x = d3.time.scale().range([0, w]);
				x.domain(d3.extent(coolData, function(d) { return d.date; }));
				var y = d3.scale.linear().domain([0, d3.max(allData)]).range([h, 0]);

				//x.domain(d3.extent(time, function(t) { return t }));

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
					.domain(d3.keys(coolData[0]).filter(function(key) { return key !== "date"; }));

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
					.style("stroke", function(d) { return color(d.name); });

				user.append("text")
					.datum(function(d) { return {name: d.name, value: d.values[d.values.length-1]}; })
					.attr("transform", function(d) { return "translate(" + x(d.value.date) + "," + y(d.value.count) + ")"; })
					.attr("x", 3)
					.attr("dy", ".35em")
					.text(function(d) { return d.name; });
					//.style("stroke", function(d) {
					//	console.log("d: " + d.name + ", color: " + color(d.name)); 
					//	return color(d.name);});
					
				
				
				//chart.append("path").attr("d", line(data[0]));
									
				var numUsers = data.length;
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
			
			function getData() {
				response = ${remoteFunction(action: "ajaxGraphData", onComplete: "graph()", params: params)};
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
					if (storedData && ((new Date()) - (new Date(storedData.date))) < ONE_DAY) {
						responseObject = storedData;
						graph();
						return
					}
				</g:if>

				getData();
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