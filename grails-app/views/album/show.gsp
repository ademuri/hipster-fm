
<%@ page import="com.ademuri.hipster.Album" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'album.label', default: 'Album')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-album" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div id="show-album" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list album">
			
				<g:if test="${albumInstance?.lastId}">
				<li class="fieldcontain">
					<span id="lastId-label" class="property-label"><g:message code="album.lastId.label" default="Last Id" /></span>
					
						<span class="property-value" aria-labelledby="lastId-label"><g:fieldValue bean="${albumInstance}" field="lastId"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${albumInstance?.artist}">
				<li class="fieldcontain">
					<span id="artist-label" class="property-label"><g:message code="album.artist.label" default="Artist" /></span>
					
						<span class="property-value" aria-labelledby="artist-label"><g:link controller="artist" action="show" id="${albumInstance?.artist?.id}">${albumInstance?.artist?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${albumInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="album.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${albumInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${albumInstance?.userAlbums}">
				<li class="fieldcontain">
					<span id="userAlbums-label" class="property-label"><g:message code="album.userAlbums.label" default="User Albums" /></span>
					
						<g:each in="${albumInstance.userAlbums}" var="u">
						<span class="property-value" aria-labelledby="userAlbums-label"><g:link controller="userAlbum" action="show" id="${u.id}">${u?.encodeAsHTML()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${albumInstance?.id}" />
					<g:link class="edit" action="edit" id="${albumInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
