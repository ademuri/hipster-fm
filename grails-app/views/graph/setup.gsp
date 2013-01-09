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
				<li><g:link class="list" controller="artist" action="list">Artists</g:link></li>
			</ul>
		</div>
		
		<div id="setup-graph" class="content" role="main">
			<h1>Setup Graph</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<div id="setup-graph-form">
				<g:form method="post" >
					<fieldset class="form">
						<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
							<label for="user">
								User
							</label>
							<g:textField name="user"/>
						</div>
						<div class="fieldcontain ${hasErrors(field: 'artist', 'error')} ">
							<label for="artist">
								Artist
							</label>
							<g:textField name="artist"/>
						</div>
					</fieldset>
					<fieldset class="buttons">
						<g:actionSubmit class="submit" action="search" value="Submit" />
					</fieldset>
				</g:form>
						
			</div>
		
		</div>
	</body>
</html>