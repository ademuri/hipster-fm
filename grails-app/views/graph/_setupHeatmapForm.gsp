<fieldset class="form">
	<div class="setup-group">
		<div class="heatmap-button heatmap-add">+</div>
		<div class="heatmap-button heatmap-remove">-</div>
		<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
			<label for="user">
				User
			</label>
			<g:textField name="user" value="${user}" class="user"/>
			<r:img dir="images" file="spinner.gif" width="16" height="16" class="user-spinner" />
		</div>
		
		<div class="fieldcontain ${hasErrors(field: 'artist', 'error')} ">
			<label for="artist">
				Artist
			</label>
			<g:textField name="artist" value="${artistName}" class="artist" />
			<r:img dir="images" file="spinner.gif" width="16" height="16" class="artist-spinner" />
		</div>
		
		<div class="fieldcontain" ${hasErrors(field: 'type', 'error')} ">
			<label for="type">Type</label>
			<g:select name='type' from='${heatmapTypes}'  class="type"/>
		</div>
	</div>
</fieldset>