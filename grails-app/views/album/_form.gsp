<%@ page import="com.ademuri.hipster.Album" %>



<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'lastId', 'error')} ">
	<label for="lastId">
		<g:message code="album.lastId.label" default="Last Id" />
		
	</label>
	<g:textField name="lastId" value="${albumInstance?.lastId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'artist', 'error')} required">
	<label for="artist">
		<g:message code="album.artist.label" default="Artist" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="artist" name="artist.id" from="${com.ademuri.hipster.Artist.list()}" optionKey="id" required="" value="${albumInstance?.artist?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="album.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${albumInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: albumInstance, field: 'userAlbums', 'error')} ">
	<label for="userAlbums">
		<g:message code="album.userAlbums.label" default="User Albums" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${albumInstance?.userAlbums?}" var="u">
    <li><g:link controller="userAlbum" action="show" id="${u.id}">${u?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="userAlbum" action="create" params="['album.id': albumInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'userAlbum.label', default: 'UserAlbum')])}</g:link>
</li>
</ul>

</div>

