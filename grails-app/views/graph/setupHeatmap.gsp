
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Setup Heatmap</title>
		<r:require modules="jquery, jqplot" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="find" controller="user" action="find">Find user</g:link></li>
				<li><g:link class="setup" controller="options" action="colors">Colors</g:link></li>
				<g:if env="development">
				<li><g:link class="fetchTopArtists" controller="graph" action="fetchTopArtists">Fetch top artists</g:link></li>
				</g:if>
			</ul>
		</div>
		
		<div id="setup-graph" class="content" role="main">
			<h1>Setup Heatmap</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="setup-heatmap-form">
				<g:form method="post" >
					<fieldset class="form">
						<div class="setup-group">
							<div class="fieldcontain ${hasErrors(field: 'user', 'error')} ">
								<label for="user">
									User
								</label>
								<g:textField name="user" value="${user}"/>
							</div>
							
							<div class="fieldcontain ${hasErrors(field: 'artist', 'error')} ">
								<label for="artist">
									Artist
								</label>
								<g:textField name="artist" value="${artistName}" />
							</div>
							
							<div class="fieldcontain" ${hasErrors(field: 'type', 'error')} ">
								<label for="type">Type</label>
								<g:select name='type' from='${heatmapTypes}' />
							</div>
						</div>
					</fieldset>
					<fieldset class="buttons">
						<g:actionSubmit class="submit" action="heatmapSearch" value="Submit" />
					</fieldset>
				</g:form>
			</div>
		</div>
	</body>
</html>