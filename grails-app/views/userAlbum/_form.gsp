<%@ page import="com.ademuri.hipster.UserAlbum" %>



<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'lastId', 'error')} required">
	<label for="lastId">
		<g:message code="album.lastId.label" default="Last Id" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="lastId" required="" value="${albumInstance?.lastId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'artist', 'error')} required">
	<label for="artist">
		<g:message code="album.artist.label" default="Artist" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="artist" name="artist.id" from="${com.ademuri.hipster.UserArtist.list()}" optionKey="id" required="" value="${albumInstance?.artist?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="album.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${albumInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'tracks', 'error')} ">
	<label for="tracks">
		<g:message code="album.tracks.label" default="Tracks" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${albumInstance?.tracks?}" var="t">
    <li><g:link controller="track" action="show" id="${t.id}">${t?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="track" action="create" params="['album.id': albumInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'track.label', default: 'Track')])}</g:link>
</li>
</ul>

</div>

