<%@ page import="hipsterfm.UserArtist" %>



<div class="fieldcontain ${hasErrors(bean: artistInstance, field: 'tracks', 'error')} ">
	<label for="tracks">
		<g:message code="artist.tracks.label" default="Tracks" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${artistInstance?.tracks?}" var="t">
    <li><g:link controller="track" action="show" id="${t.id}">${t?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="track" action="create" params="['artist.id': artistInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'track.label', default: 'Track')])}</g:link>
</li>
</ul>

</div>

<div class="fieldcontain ${hasErrors(bean: artistInstance, field: 'user', 'error')} required">
	<label for="user">
		<g:message code="artist.user.label" default="User" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="user" name="user.id" from="${hipsterfm.User.list()}" optionKey="id" required="" value="${artistInstance?.user?.id}" class="many-to-one"/>
</div>

