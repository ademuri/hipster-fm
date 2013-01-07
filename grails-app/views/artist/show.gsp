
<%@ page import="hipsterfm.Artist" %>
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
		<div id="show-artist" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list artist">
			
				<g:if test="${artistInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="artist.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${artistInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${artistInstance?.lastId}">
				<li class="fieldcontain">
					<span id="lastId-label" class="property-label"><g:message code="artist.lastId.label" default="Last Id" /></span>
					
						<span class="property-value" aria-labelledby="lastId-label"><g:fieldValue bean="${artistInstance}" field="lastId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${artistInstance?.userArtists}">
				<li class="fieldcontain">
					<span id="userArtists-label" class="property-label"><g:message code="artist.userArtists.label" default="Users" /></span>
					
						<g:each in="${artistInstance.userArtists}" var="u">
						<span class="property-value" aria-labelledby="userArtists-label"><g:link controller="userArtist" action="show" id="${u.id}">${u?.user.username}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${artistInstance?.id}" />
					<g:link class="edit" action="edit" id="${artistInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					<g:actionSubmit class="sync" action="sync" value="${message(code: 'default.button.sync.label', default: 'Sync')}"/>
					<g:actionSubmit class="first" action="first" value="${message(code: 'default.button.first.label', default: 'First')}"/>
					<g:link class="graph" controller="graph" action="show" id="${artistInstance?.id}" value="${message(code: 'default.button.graph.label', default: 'Graph')}">Graph</g:link>
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
