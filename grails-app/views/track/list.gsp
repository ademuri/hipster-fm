
<%@ page import="com.ademuri.hipster.Track" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'track.label', default: 'Track')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-track" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-track" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="date" title="${message(code: 'track.date.label', default: 'Date')}" />
					
						<th><g:message code="track.artist.label" default="Artist" /></th>
					
						<g:sortableColumn property="lastId" title="${message(code: 'track.lastId.label', default: 'Last Id')}" />
					
						<g:sortableColumn property="name" title="${message(code: 'track.name.label', default: 'Name')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${trackInstanceList}" status="i" var="trackInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${trackInstance.id}">${fieldValue(bean: trackInstance, field: "date")}</g:link></td>
					
						<td>${fieldValue(bean: trackInstance, field: "artist")}</td>
					
						<td>${fieldValue(bean: trackInstance, field: "lastId")}</td>
					
						<td>${fieldValue(bean: trackInstance, field: "name")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${trackInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
