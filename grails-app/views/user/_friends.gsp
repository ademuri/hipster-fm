<li class="fieldcontain">
	<span id="friends-label" class="property-label"><g:message code="user.friends.label" default="Friends" /></span>
	
		<g:each in="${friends}" var="a">
		<span class="property-value" aria-labelledby="friends-label"><g:link controller="user" action="show" id="${a.id}">${a?.toString()}</g:link></span>
		</g:each>
</li>