<%@ page import="com.ademuri.hipster.ShortLink" %>



<div class="fieldcontain ${hasErrors(bean: shortLinkInstance, field: 'shortUrl', 'error')}">
	<label for="shortUrl">
		<g:message code="shortLink.shortUrl.label" default="Short Url" />
	</label>
	<g:textField name="shortUrl" value="${shortLinkInstance?.shortUrl}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: shortLinkInstance, field: 'fullUrl', 'error')} required">
	<label for="fullUrl">
		<g:message code="shortLink.fullUrl.label" default="Full Url" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="fullUrl" required="" value="${shortLinkInstance?.fullUrl}"/>
</div>

