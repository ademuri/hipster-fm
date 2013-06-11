function heatmap(chartdiv, data2) {
	var data = [ 0, 1, 2, 3, 4, 5, 6, 7
	];
	
	var cd = $("#" + chartdiv);
	cd.attr("id", "heatmapdiv");
	cd.append("<tr>");
	
	var tr = $("#heatmapdiv tr");
	for (datum in data) {
		var i = (Math.floor((0xFF * datum) / 8) + 1).toString();
		var rgb = "rgb(" + i + "," + i + "," + i + ")";
		console.log(rgb);
		tr.append('<td style="background-color:' + rgb + '"></td>'); 
	}
	
	$("#heatmapdiv tr").css("height", "3em");
	$("#heatmapdiv td").css("width", "1px");
	$("#heatmapdiv td").css("padding", "0");
} 
