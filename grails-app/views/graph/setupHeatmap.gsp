
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Setup Heatmap</title>
		<r:require modules="jquery, jqplot" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="find" controller="user" action="find">Find user</g:link></li>
				<li><g:link class="setup" controller="options" action="colors">Colors</g:link></li>
				<g:if env="development">
				<li><g:link class="fetchTopArtists" controller="graph" action="fetchTopArtists">Fetch top artists</g:link></li>
				</g:if>
			</ul>
		</div>
		
		<div id="setup-heatmap-template">
			<g:render template="setupHeatmapForm" />
		</div>
		
		<div id="setup-graph" class="content" role="main">
			<h1>Setup Heatmap</h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="setup-heatmap-form">
				<g:form method="post" >
					<div id="setup-heatmap-target">
						<g:render template="setupHeatmapForm" />
					</div>
 					<fieldset class="buttons">
						<g:actionSubmit class="submit" action="heatmapSearch" value="Submit" />
					</fieldset>
				</g:form>
			</div>
		</div>
		
		<script>
		function setIds(field) {
			$("." + field).each( function(i, input) {
				$(input).attr("id", ("" + field) + i);
				$(input).attr("name", ("" + field) + i);
			});
		}
		
		function add() {
			var thing = $("#setup-heatmap-template").clone(); 
			thing.insertBefore($(this).parent().parent());
			thing.attr("id", "");
			$(".heatmap-remove").unbind();
			$(".heatmap-add").unbind();
			$(".heatmap-remove").click(remove);
			$(".heatmap-add").click(add);
			setIds("user");
			setIds("artist");
			setIds("type");
		}
		
		function remove() {
			$(this).parent().parent().remove();
		}

		$(document).ready( function() {
			$(".heatmap-remove").click(remove);
			$(".heatmap-add").click(add);
		});
		</script>
	</body>
</html>