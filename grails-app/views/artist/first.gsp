<%@ page import="hipsterfm.Artist" %>
<%@ page import="hipsterfm.User" %>
<%@ page import="hipsterfm.UserArtist" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'artist.label', default: 'Artist')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-artist" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		
		<div id="show-users" class="content scaffold-show" role="main">
			<ol class="propery-list user">
				<g:if test="${users?.size() > 0}">
				<li class="fieldcontain">
					<span id="users-label" class="property-label">Users</span>
					
					<g:each in="${users}" var="u">
						<span class="property-value" aria-labelledby="users-label"><g:link controller="track" action="show" id="${u.track_id}">${u.name} - ${u.date.toString()}</g:link></span>
					</g:each>
				</li>
				</g:if>
			</ol>
		</div>
	</body>
		

</html>