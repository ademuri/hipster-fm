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
				</fieldset>
				</g:form>
			</div>
			
			<div id="setup-graph-form">
				<g:form method="post" >
					<fieldset class="form">
						<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
							<label for="user">
								Users
							</label>
							<g:textField name="user" value="${user}"/>
							<label>(space-seperated list)</label>
						</div>
						<div class="checkbox">
							<g:each var="user" in="${friends}" status="i">
								<span class="friend-select">
									<g:checkBox name="user_${user.id}"/>
									<label for="user_${user.id}">${user.toString()}</label>
								</span>
							</g:each>
						</div>
						<div class="fieldcontain ${hasErrors(field: 'artist', 'error')} ">
							<label for="artist">
								Artist
							</label>
							<g:textField name="artist"/>
						</div>
						<div class="fieldcontain" >
							<label for="removeOutliers">Remove Outliers</label>
							<g:checkBox name="removeOutliers" />
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