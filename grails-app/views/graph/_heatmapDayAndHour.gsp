<script>
function doMap${it.index}(error, data) {
	var colors = ['#EEE', '#DDD', '#CCC', '#BBB', '#AAA', '#999', '#888', '#777', '#666', '#555', '#444', '#333', '#222', '#111', '#000'];
	var margin = { top: 50, right: 0, bottom: 100, left: 30 },
		squareHeight = 25,
		squareMargin = 2,
		gridHeight = 7 * squareHeight + 6 * squareMargin,
		gridWidth = 24 * squareHeight + 23 * squareMargin;
		
	var days = ['S', 'M', 'T', 'W', 'R', 'F', 'S'],
		times = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
	
	var colorScale = d3.scale.quantile()
		.domain([0, d3.max(data, function(d) { return d.count; })])
		.range(colors);
	
	var svg = d3.select(".body${it.index}")
  		.append("svg")
  		.attr("width", (squareHeight + squareMargin) * 2 * colors.length + margin.left + margin.right)
  		.attr("height", gridHeight + margin.top + margin.bottom)
  		.append("g")
  		.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
  		
  	var dayLabels = svg.selectAll(".dayLabel")
        .data(days)
        .enter().append("text")
        .text(function (d) { return d; })
        .attr("x", 0)
        .attr("y", function (d, i) { return i * gridHeight / 7; })
        .style("text-anchor", "end")
        .attr("transform", "translate(-6," + (gridHeight / 7 - squareHeight / 2.5) + ")")
        .attr("class", function (d) { return "dayLabel mono axis"; });
             
    var timeLabels = svg.selectAll(".timeLabel")
        .data(times)
        .enter().append("text")
        .text(function (d) { return d; })
        .attr("x", function (d, i) { return (i+1)*squareHeight + i*squareMargin; })
        .attr("y", 0)
        .style("text-anchor", "end")
        .attr("transform", "translate(-" + squareHeight / 4 + ", -6)")
        .attr("class", function (d) { return "hourLabel mono axis"; });

	var heatmap = svg.selectAll(".day")
  		.data(data)
  		.enter()
  		.append("rect")
  		.attr("class", "day")
  		.attr("width", squareHeight)
  		.attr("height", squareHeight)
  		.attr("x", function(d) { return d.hour * (squareHeight + squareMargin); })
  		.attr("y", function(d) { return (d.day-1) * (squareHeight + squareMargin); })
  		.style("fill", function(d) { return colorScale(d.count); });
  		
  	var legend = svg.selectAll(".legend")
        .data([0].concat(colorScale.quantiles()), function(d) { return d; })
        .enter().append("g")
        .attr("class", "legend");
          
    legend.append("rect")
        .attr("x", function(d, i) { return squareHeight * 2 * i; })
        .attr("y", gridHeight + squareHeight)
        .attr("width", squareHeight * 2)
        .attr("height", squareHeight)
        .style("fill", function(d, i) { return colors[i]; })

    legend.append("text")
      .attr("class", "mono")
      .text(function(d) { return "≥ " + Math.round(d); })
      .attr("x", function(d, i) { return squareHeight * 2 * i; })
      .attr("y", gridHeight + squareHeight * 1.75)
        .style("fill", function(d, i) { return i > (colors.length / 2) ? '#FFF' : '#000'; });
}
</script>	