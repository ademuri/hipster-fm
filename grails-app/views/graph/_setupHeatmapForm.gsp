<fieldset class="form">
	<div class="setup-group">
		<div class="heatmap-button heatmap-add">+</div>
		<div class="heatmap-button heatmap-delete">-</div>
		<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
			<label for="user">
				User
			</label>
			<g:textField name="user0" value="${user}" class="user"/>
		</div>
		
		<div class="fieldcontain ${hasErrors(field: 'artist', 'error')} ">
			<label for="artist">
				Artist
			</label>
			<g:textField name="artist0" value="${artistName}" class="artist" />
		</div>
		
		<div class="fieldcontain" ${hasErrors(field: 'type', 'error')} ">
			<label for="type">Type</label>
			<g:select name='type0' from='${heatmapTypes}'  class="type"/>
		</div>
	</div>
</fieldset>