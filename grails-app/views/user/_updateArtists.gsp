<g:each in="${artistList}" status = "i" var="artist" >
	<li><g:link controller="userArtist" action="show" id="${artist.id}">${artist}</g:link></li>
</g:each>