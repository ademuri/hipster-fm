	
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
						<input type='reset' value='Reset' id="reset" />
					</fieldset>
				</g:form>
			</div>
		</div>
		
		<script>

		var usernames = [];
		var artists = [];
		var maxIndex = 1;
		var animate = false;	// set this to true once we've autoloaded the previous data

		function storeField(field) {
			index = 0;
			$("div#setup-heatmap-target ." + field).each( function() {
				var contents = $(this).val();

				sessionStorage.setItem("heatmap-" + field + "-" + index, contents);
				index++;
			});
			return index;
		}
		
		function updateSessionStorage() {
			var index = storeField("artist");
			storeField("user");
			storeField("type");

			index--;
			sessionStorage.setItem("heatmap-num", index);
		}

		function attachSessionStorage(form) {
			form.find("input").blur(updateSessionStorage);
			form.find("select").change(updateSessionStorage);
		}
		
		function setIds(field) {
			$("div#setup-heatmap-target ." + field).each( function(i, input) {
				$(input).attr("id", ("" + field) + i);
				$(input).attr("name", ("" + field) + i);
			});
		}
		
		function add() {
			var thing = $("#setup-heatmap-template").clone();
			thing.insertAfter($(this).parent().parent());
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
			attachSessionStorage(thing);
			if (animate) {
				thing.hide(1);
				thing.show(400);
			}
		}

		function fadeOutRemove(thing) {
			thing.hide(400, function() {
				thing.remove();
				if ($("div#setup-heatmap-target input").size() > 0) {
					$("div#setup-heatmap-target input").blur();
				}
			});
		}
		
		function remove() {
			var thing = $(this).parent().parent();
			if(animate) {
				fadeOutRemove(thing);
			} else {
				thing.remove();
			}
		}

		function reset() {
			var index = 0;
			$("div#setup-heatmap-target fieldset").each( function() {
				if (index > 0) {
					fadeOutRemove($(this));
				}
				index++;
			});
		}

		$(document).ready( function() {
			$("#spinner").offset({top: -100, left: -100});
			$(".heatmap-remove").click(remove);
			$(".heatmap-add").click(add);
			$("#reset").click(reset);

			setIds('artist');
			setIds('user');
			setIds('type');

			var numString = sessionStorage.getItem("heatmap-num");
			if (numString) {
				var num = parseInt(numString, 10);
				for (var i=0; i<num; i++) {
					$("div#setup-heatmap-target .heatmap-add").click();
				}

				for (var i=0; i<=num; i++) {
					var artist = sessionStorage.getItem("heatmap-artist-" + i);
					if (artist) {
						$("#artist" + i).val(artist);
					}

					var user = sessionStorage.getItem("heatmap-user-" + i);
					if (user) {
						$("#user" + i).val(user);
					}

					var type = sessionStorage.getItem("heatmap-type-" + i);
					if (type) {
						$("#type" + i).val(type);
					}
				}
			}

			attachSessionStorage($("div#setup-heatmap-target fieldset"));
			$("form").submit(updateSessionStorage);

			animate = true;

			$(".artist").autocomplete({
				source: [],
				delay: 0
			});
			getCache('artist_cache', '.artist-spinner', '${createLink(controller: 'artist', action: 'ajaxGetArtistList')}', 
					function(data) {
						$(".artist").autocomplete("option", "source", data);	
						artists = data;
			});

			$(".user").autocomplete({
				source: [],
				delay: 0
			});
			getCache('username_cache', '.user-spinner', '${createLink(controller: 'user', action: 'ajaxGetUserList')}', 
					function(data) {
						$(".user").autocomplete("option", "source", data);	
						usernames = data;
			});
		});
		</script>
	</body>
</html>