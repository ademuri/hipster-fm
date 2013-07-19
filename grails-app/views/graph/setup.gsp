<%@ page import="com.ademuri.hipster.UserArtist" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Setup Graph</title>
		<r:require modules="jquery, irex, store_js" />
	</head>
	<body>
		<div id="setup-graph" class="content" role="main">
			<h1>Setup Graph</h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<div id="setup-graph-form" >
				<g:form method="post">
					<fieldset class="form">
					<div class="setup-group">
						<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
							<label for="user">
								Users
							</label>
							<g:textField name="user" value="${user}"/>
							<r:img dir="images" file="spinner.gif" width="16" height="16" id="user-spinner" />
							<label>(click to show friends)</label>
						</div>
						<div class="fieldcontain">
							<label for="addAllFriends">Add all friends with artist</label>
							<g:checkBox name="addAllFriends" />
						</div>
						
						
						<div class="checkbox">
							<g:each var="user" in="${friends}" status="i">
								<span class="friend-select">
									<div class="single-friend">
										<g:checkBox name="u_${user.id}"/>
										<label for="u_${user.id}">${user.toString()}</label>
									</div>
								</span>
							</g:each>
						</div>
					</div>
						<div class="setup-group">
							<div class="fieldcontain ${hasErrors(field: 'artist', 'error')} ">
								<label for="artist">
									Artist
								</label>
								<g:textField name="artist" value="${artistName}" class="inputosaurus-autocomplete" />
								<r:img dir="images" file="spinner.gif" width="16" height="16" id="artist-spinner" />
							</div>
						
							<g:if test="${topArtists?.size() > 0}">
								<div class="fieldcontain" >
									<label for="artist-select"></label>
									<g:select name="artist-select" from="${topArtists}" optionKey="name" optionValue="name" multiple="true" />
								</div>
							</g:if>
							<div class="fieldcontain ${hasErrors(field: 'album', 'error')} ">
								<label for="album">
									Album
								</label>
								<g:textField name="album"/>
							</div>
						</div>
						
						<div class="fieldcontain" >
							<label for="removeOutliers">Remove Outliers</label>
							<g:checkBox name="removeOutliers" />
						</div>
					</fieldset>
					<div class="setup-group">
						<g:render template="window"/>
					</div>
					<fieldset class="buttons">
						<g:actionSubmit class="submit" action="search" value="Submit" id="setup-submit" />
					</fieldset>
				</g:form>
						
			</div>
		
		</div>
		<script>
		$("#artist-select").change(function() {
			$("#artist").val($("#artist-select").val());
		});

		var friends = [];
		
		function showFriends(name) {
			var url = '${createLink(controller: 'user', action: 'ajaxGetFriendsByName')}';
			url += "?username=" + name;
			getCache('friend_cache_' + name, url, function(data) {
				$("div.checkbox").html(data);

				$("div.checkbox div.single-friend input").unbind("click");
				$("div.checkbox div.single-friend input").click( function() {
					var name = $(this).parent().find('input.username').attr("name");
					$("li.inputosaurus-input input").val(name + " ");
					$("li.inputosaurus-input input").trigger("change");
				});
			});
		}
		
		$(document).ready( function() {
			$.ajaxSetup({ cache: true });
			$("#spinner").offset({top: -100, left: -100});
			
			getCache('username_cache', '#user-spinner', '${createLink(controller: 'user', action: 'ajaxGetUserList')}', 
					function(data) {
				$("#user").inputosaurus({
					width: '450px',
					autoCompleteSource: data,
					autoCompleteDelay: 0,
					parseOnBlur: true,
					submitOnEmptyTag: "#setup-submit",
					inputDelimiters: [',', ' '],
					tagFocusHook: showFriends
				});
			});

			$("#artist").autocomplete({
				source: [],
				delay: 0
			});
			getCache('artist_cache', '#artist-spinner', '${createLink(controller: 'artist', action: 'ajaxGetArtistList')}', 
					function(data) {
						$("#artist").autocomplete("option", "source", data);				
					}
			);

			// clicking on tagged user will show that users' friends, which can also be added
			$("li[data-inputosaurus]").click( function() {
				var span = $($(this)[0].firstChild);
				var name = span.text();
			});
		});
		</script>
	</body>
</html>