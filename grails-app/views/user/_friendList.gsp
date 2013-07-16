<g:each var="user" in="${friends}" status="i">
	<span class="friend-select">
		<div class="single-friend">
			<g:checkBox name="u_${user.id}"/>
			<label for="u_${user.id}">${user.toString()}</label>
		</div>
	</span>
</g:each>