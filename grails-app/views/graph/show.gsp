<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Show Graph</title>
		<r:require modules="jquery, jqplot" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="setup" action="setup">Setup graph</g:link></li>
			</ul>
		</div>
		
		<g:if test="${params?.type == 'breakout'}">
			<g:render template="breakout" />
		</g:if>
		<g:else>
			<g:render template="compare" />
		</g:else>
		
		<g:form method="get" >
			<g:render template="window"/>
			<g:each var="p" in="${params}">
				<g:if test="${p.key != 'tickSize' && p.key != 'intervalSize' && p.key != '_action_show'
					&& p.key != 'action' && p.key != 'controller'}">
					<g:hiddenField name="${p.key}" value="${p.value}" />
				</g:if> 				
			</g:each>
			<fieldset class="buttons">
				<g:actionSubmit class="submit" action="show" value="Submit"  />
			</fieldset>
		</g:form>
		
	</body>
</html>