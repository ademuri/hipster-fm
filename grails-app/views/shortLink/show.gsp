
<%@ page import="com.ademuri.hipster.ShortLink" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'shortLink.label', default: 'ShortLink')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-shortLink" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-shortLink" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list shortLink">
			
				<g:if test="${shortLinkInstance?.shortUrl}">
				<li class="fieldcontain">
					<span id="shortUrl-label" class="property-label"><g:message code="shortLink.shortUrl.label" default="Short Url" /></span>
					
						<span class="property-value" aria-labelledby="shortUrl-label"><g:fieldValue bean="${shortLinkInstance}" field="shortUrl"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${shortLinkInstance?.fullUrl}">
				<li class="fieldcontain">
					<span id="fullUrl-label" class="property-label"><g:message code="shortLink.fullUrl.label" default="Full Url" /></span>
					
						<span class="property-value" aria-labelledby="fullUrl-label"><g:fieldValue bean="${shortLinkInstance}" field="fullUrl"/></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${shortLinkInstance?.id}" />
					<g:link class="edit" action="edit" id="${shortLinkInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
