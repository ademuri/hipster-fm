<!doctype html>
<html>
<head>
	<meta name="layout" content="main" >
	<title>Licenses</title>
</head>

<body>
	<div class="nav" role="navigation">
		<ul>
			<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
		</ul>
	</div>
	
	<div class="content" role="main">
		<div class="show-license">
			<g:render template="${params.license}"></g:render>
		</div>
	</div>
</body>
</html>