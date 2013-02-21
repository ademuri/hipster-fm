
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
		<div id="show-artist" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list artist">
			
				<g:if test="${artistInstance?.artist}">
					<li class="fieldcontain">
						<span id="artist-label" class="property-label"><g:message code="artist.artist.label" default="Artist" /></span>
						<span class="property-value" aria-labelledby="artist-label"><g:link controller="artist" action="show" id="${artistInstance.artist.id}">${artistInstance.artist?.toString()}</g:link></span>
				</g:if>
				
				<g:if test="${artistInstance?.albums}">
				<li class="fieldcontain">
					<span id="albums-label" class="property-label"><g:message code="artist.albums.label" default="Albums" /></span>
					
						<g:each in="${artistInstance.albums}" var="t">
						<span class="property-value" aria-labelledby="albums-label"><g:link controller="userAlbum" action="show" id="${t.id}">${t?.toString()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
				
				<g:if test="${artistInstance?.user}">
				<li class="fieldcontain">
					<span id="user-label" class="property-label"><g:message code="artist.user.label" default="User" /></span>
					
						<span class="property-value" aria-labelledby="user-label"><g:link controller="user" action="show" id="${artistInstance?.user?.id}">${artistInstance?.user?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
				
				<li class="fieldcontain">
					<span id="numscrobbles-label" class="property-label"><g:message code="userartist.scrobbles.label" default="Number of plays" /></span>
						<span class="property-value" aria-labelledby="numscrobbles-label">${artistInstance.numScrobbles}</span>
				</li>
			
				<g:if test="${artistInstance?.tracks}">
				<li class="fieldcontain">
					<span id="tracks-label" class="property-label"><g:message code="artist.tracks.label" default="Tracks" /></span>
					
						<g:each in="${artistInstance.tracks.sort {it.date}}" var="t">
						<span class="property-value" aria-labelledby="tracks-label"><g:link controller="track" action="show" id="${t.id}">${t?.toString()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
				
				<li class="fieldcontain">
					<g:each in="${UserArtist.rankNames}">
						<g:if test='${artistInstance."isTop${it}" }'>
							<span id="top${it}Rank-label" class="property-label">${it} rank</span>
							<span class="property-value" aria-labelledby="top${it}Rank-label">${artistInstance."top${it}Rank"}</span>
						</g:if>
					</g:each>
				</li>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${artistInstance?.id}" />
					<g:link class="edit" action="edit" id="${artistInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
