<g:each var="user" in="${friends}" status="i">
	<span class="friend-select">
		<div class="single-friend">
			<input type="button" name="u_${user.id}" value="${user.toString()}" />
			<input type="hidden" class="username" name="${user.username}">
		</div>
	</span>
</g:each>