
<%@ page import="com.ademuri.hipster.ShortLink" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'shortLink.label', default: 'ShortLink')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-shortLink" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-shortLink" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
					
						<g:sortableColumn property="shortUrl" title="${message(code: 'shortLink.shortUrl.label', default: 'Short Url')}" />
					
						<g:sortableColumn property="fullUrl" title="${message(code: 'shortLink.fullUrl.label', default: 'Full Url')}" />
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${shortLinkInstanceList}" status="i" var="shortLinkInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${shortLinkInstance.id}">${fieldValue(bean: shortLinkInstance, field: "shortUrl")}</g:link></td>
					
						<td>${fieldValue(bean: shortLinkInstance, field: "fullUrl")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${shortLinkInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
