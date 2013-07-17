<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="grails.converters.JSON" %>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="main"/>
	<title>Heatmap</title>
	<r:require modules="jquery, d3" />
</head>
<body>
	<script type="text/javascript">
	var running = 0;
	$(document).ready(function() {
		$("#spinner").unbind();
		$("#spinner").show();
	});
			
	</script>

	<g:each in="${graphs}" var="graph">
	  	<div class="body${graph.index}">	
			<h1>${graph.artistName} - ${graph.userName}</h1>  
	  	</div>
	  	
	  	<g:render template="heatmap${graph.type}" bean="${graph}" />
	  
		<script>
			$(document).ready(function() {
				running++;
				d3.json('${createLink(controller: 'graph', action: 'ajaxHeatmapData', params:[u: graph.user, a: graph.artist, t: graph.type])}', function(a, b) {
					doMap${graph.index}(a, b) 
					running--;
					if (running == 0) {
						$("#spinner").hide();
					}
				});
			});
	  	</script>
	</g:each>
</body>
</html>