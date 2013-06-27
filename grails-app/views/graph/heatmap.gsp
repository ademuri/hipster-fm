<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="grails.converters.JSON" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="main"/>
	<title>Heatmap</title>
	<r:require modules="jquery, d3" />
</head>
<body>
  <div class="body">
  
  </div>
  
  <script>
	function doMap(error, data) {
		var colors = ['#CCC', '#999', '#666', '#333', '#000'];
		var squareHeight = 20;
		
		var newData = [];
		for (var i=0; i<data.data.length; i++) {
			var day = new Object();
			day.index = i;
			day.data = data.data[i];
			newData.push(day);
		}
		console.log(newData);
		
		var colorScale = d3.scale.quantile()
		.domain([0, d3.max(newData, function(d) { return d.data; })])
		.range(colors);
		
		var svg = d3.select(".body")
  		.append("svg")
  		.attr("width", 7 * (squareHeight + 5))
  		.attr("height", squareHeight * 1.5)
  		.append("g");


  		var heatmap = svg.selectAll(".day")
  		.data(newData)
  		.enter()
  		.append("rect")
  		.attr("class", "hour")
  		.attr("width", squareHeight)
  		.attr("height", squareHeight)
  		.attr("x", function(d) { return d.index * (squareHeight + 5); })
  		.style("fill", function(d) { return colorScale(d.data); });
  			

//  		.text(function(d) { return d; });
	}
  
  $(document).ready(function() {
	d3.json('${createLink(controller: 'graph', action: 'ajaxHeatmapData')}', doMap);
  });
  </script>
</body>
</html>