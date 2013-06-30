<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="grails.converters.JSON" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="main"/>
	<title>Heatmap</title>
	<r:require modules="jquery, d3" />
</head>
<body>
	<div class="nav" role="navigation">
			<ul>
				<li><g:link class="setup" action="setup">Setup graph</g:link></li>
				<li><g:link class="setup" controller="graph" action="setupHeatmap">Setup Heatmap</g:link></li>
				<li><g:link class="find" controller="user" action="find">Find user</g:link></li>
				<li><g:link class="setup" action="setup" params="${params}">Filter graph</g:link></li>
				<li><g:link class="setup" controller="options" action="colors">Colors</g:link></li>
			</ul>
		</div>

  <div class="body">
		<h1>${artistName}<g:if test="${userName && artistName}"> - </g:if>${userName}</h1>  
  </div>
  
	<g:render template="heatmap${params.type}" />
  
  <script>
  $(document).ready(function() {
	d3.json('${createLink(controller: 'graph', action: 'ajaxHeatmapData', params:params)}', doMap);
  });
  </script>
</body>
</html>