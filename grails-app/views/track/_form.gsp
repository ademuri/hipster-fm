<%@ page import="com.ademuri.hipster.Track" %>



<div class="fieldcontain ${hasErrors(bean: trackInstance, field: 'date', 'error')} required">
	<label for="date">
		<g:message code="track.date.label" default="Date" />
		<span class="required-indicator">*</span>
	</label>
	<g:datePicker name="date" precision="day"  value="${trackInstance?.date}"  />
</div>

<div class="fieldcontain ${hasErrors(bean: trackInstance, field: 'artist', 'error')} required">
	<label for="artist">
		<g:message code="track.artist.label" default="Artist" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="artist" name="artist.id" from="${com.ademuri.hipster.UserArtist.list()}" optionKey="id" required="" value="${trackInstance?.artist?.id}" class="many-to-one"/>
</div>

<div class="fieldcontain ${hasErrors(bean: trackInstance, field: 'lastId', 'error')} ">
	<label for="lastId">
		<g:message code="track.lastId.label" default="Last Id" />
		
	</label>
	<g:textField name="lastId" value="${trackInstance?.lastId}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: trackInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="track.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${trackInstance?.name}"/>
</div>

