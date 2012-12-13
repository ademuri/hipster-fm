<%@ page import="hipsterfm.User" %>



<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'username', 'error')} required">
	<label for="username">
		<g:message code="user.username.label" default="Username" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="username" required="" value="${userInstance?.username}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'email', 'error')} ">
	<label for="email">
		<g:message code="user.email.label" default="Email" />
		
	</label>
	<g:textField name="email" value="${userInstance?.email}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userInstance, field: 'artists', 'error')} ">
	<label for="artists">
		<g:message code="user.artists.label" default="Artists" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${userInstance?.artists?}" var="a">
    <li><g:link controller="userArtist" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="userArtist" action="create" params="['user.id': userInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'artist.label', default: 'Artist')])}</g:link>
</li>
</ul>

</div>

