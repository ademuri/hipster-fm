<g:each in="${artistList}" status = "i" var="artist" >
	<li id=${artist.id}><g:link controller="graph" action="setup" params="[artist: artist, user: user]">${artist}</g:link></li>
</g:each>