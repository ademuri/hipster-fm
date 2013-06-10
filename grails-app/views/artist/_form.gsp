<%@ page import="com.ademuri.hipster.Artist" %>



<div class="fieldcontain ${hasErrors(bean: artistInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="artist.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${artistInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: artistInstance, field: 'lastId', 'error')} ">
	<label for="lastId">
		<g:message code="artist.lastId.label" default="Last Id" />
		
	</label>
	<g:textField name="lastId" value="${artistInstance?.lastId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: artistInstance, field: 'userArtists', 'error')} ">
	<label for="userArtists">
		<g:message code="artist.userArtists.label" default="User Artists" />
		
	</label>
	
<ul class="one-to-many">
<g:each in="${artistInstance?.userArtists?}" var="u">
    <li><g:link controller="userArtist" action="show" id="${u.id}">${u?.encodeAsHTML()}</g:link></li>
</g:each>
<li class="add">
<g:link controller="userArtist" action="create" params="['artist.id': artistInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'userArtist.label', default: 'UserArtist')])}</g:link>
</li>
</ul>

</div>

