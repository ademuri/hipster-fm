<%@ page import="com.ademuri.hipster.User" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Find User</title>
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="setup" controller="graph" action="setup">Setup graph</g:link></li>
				<li><g:link class="setup" controller="graph" action="setupHeatmap">Setup Heatmap</g:link></li>
			</ul>
		</div>
		
		<div id="find-user" class="content" role="main">
			<h1>Find User</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="user-form">
				<g:form method="post" >
				<fieldset class="form">
					<span class="fieldcontain">
						<label for="username">
							Search for user
						</label>
						<g:textField name="username"/>
					</span>
					<g:actionSubmit class="submit" action="find" value="Search" />
				</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>