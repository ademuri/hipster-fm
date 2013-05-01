<%@ page import="hipsterfm.UserArtist" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Setup Graph</title>
		<r:require modules="jquery, jqplot" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="find" controller="user" action="find">Find user</g:link></li>
			</ul>
		</div>
		
		<div id="setup-graph" class="content" role="main">
			<h1>Setup Graph</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="user-form">
				<g:form method="post" >
				<fieldset class="form">
					<span class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
						<label for="user">
							Search for user
						</label>
						<g:textField name="user" value="${user}"/>
					</span>
					<g:actionSubmit class="submit" action="setup" value="Search" />
					<div class="fieldcontain">
						<label for="interval">Interval</label>
						<g:select id="interval" name="interval" from="${UserArtist.rankNames}" value="${interval ?: '3month'}" />
					</div>
				</fieldset>
				</g:form>
			</div>
			
			<div id="setup-graph-form">
				<g:form method="post" >
					<fieldset class="form">
					<div class="setup-group">
						<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
							<label for="user">
								Users
							</label>
							<g:textField name="user" value="${user}"/>
							<label>(space-seperated list)</label>
						</div>
						<div class="fieldcontain">
							<label for="user-all-friends">Add all friends with artist</label>
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
								<g:textField name="artist" value="${artistName}" />
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
						<g:actionSubmit class="submit" action="search" value="Submit" />
					</fieldset>
				</g:form>
						
			</div>
		
		</div>
		<script>
		$("#artist-select").change(function() {
			$("#artist").val($("#artist-select").val());
		});
		</script>
	</body>
</html>