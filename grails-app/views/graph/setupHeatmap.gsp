
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Setup Heatmap</title>
		<r:require modules="jquery, irex, store_js" />
	</head>
	<body>
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

		var usernames = [];
		var artists = [];
		var maxIndex = 1;
		
		function setIds(field) {
			$("." + field).each( function(i, input) {
				$(input).attr("id", ("" + field) + i);
				$(input).attr("name", ("" + field) + i);
			});
		}
		
		function add() {
			var thing = $("#setup-heatmap-template").clone(); 
			thing.insertBefore($(this).parent().parent());
			thing.attr("id", "setup-heatmap" + maxIndex++);
			$(".heatmap-remove").unbind();
			$(".heatmap-add").unbind();
			$(".heatmap-remove").click(remove);
			$(".heatmap-add").click(add);
			setIds("user");
			setIds("artist");
			setIds("type");
			$("#" + thing.attr("id") + " .artist").autocomplete({
				source: artists
			});
			$("#" + thing.attr("id") + " .user").autocomplete({
				source: usernames
			});
		}
		
		function remove() {
			$(this).parent().parent().remove();
		}

		$(document).ready( function() {
			$(".heatmap-remove").click(remove);
			$(".heatmap-add").click(add);

			getCache('artist_cache', '${createLink(controller: 'artist', action: 'ajaxGetArtistList')}', 
					function(data) {
				$(".artist").autocomplete({
					source: data
				});
				artists = data;
			});

			getCache('username_cache', '${createLink(controller: 'artist', action: 'ajaxGetArtistList')}', 
					function(data) {
				$(".user").autocomplete({
					source: data
				});
				usernames = data;
			});
		});
		</script>
	</body>
</html>