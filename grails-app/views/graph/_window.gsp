<div id="graph-window-form">
	<fieldset class="form">
		<h5>Window Options</h5>
		<div class="fieldcontain ${hasErrors(field: 'tickSize', 'error')} ">
			<label for="tickSize">Number of points</label>
			<g:textField name="tickSize" value="${params?.tickSize ?: 20 }"/>
		</div>
		<div class="fieldcontain ${hasErrors(field: 'intervalSize', 'error')} ">
			<label for="intervalSize">Interval size</label>
			<g:textField name="intervalSize" value="${params?.intervalSize ?: 20 }"/>
		</div>
	</fieldset>
</div>