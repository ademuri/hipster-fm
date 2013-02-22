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
		
		<div id="progress">
		</div>
		
		<div id="show-graph" class="content" role="main">
			<h1>${artistName}<g:if test="${userName}"> - ${userName}</g:if></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
		
			<div id="chartdiv" style="height:800px;width:95%;"></div>
			
		</div>
		
		<script>
			var response;
			var maxY;
			function graph() {
				//console.log("graphing");
				//console.log(response);
				var responseObject = JSON.parse(response.responseText); 
				var chartdata = responseObject.chartdata;
				maxY = responseObject.maxY;
				//console.log(chartdata);
				var series = chartdata.series;
				var data = chartdata.data;
				//console.log(data);

				<g:if test="${params?.type == 'breakout'}">
					<g:render template="breakout" />
				</g:if>
				<g:else>
					<g:render template="compare" />
				</g:else>

				if (maxY > 0) {
					jqplotOptions.axes.yaxis.max = maxY;
				}

				$.jqplot('chartdiv', data, jqplotOptions);
			}

			$(window).load(function() {
				//console.log("loading");
				response = ${remoteFunction(action: "ajaxGraphData", onComplete: "graph()", params: params)};
			});
		</script>
		
		
		
		
		<g:form method="get" >
			<g:render template="window"/>
			<g:each var="p" in="${params}">
				<g:if test="${!(['tickSize', 'intervalSize', 'startDate', 'endDate', 
					'_action_show', 'action', 'controller', 'userMaxY'].contains(p.key ))}">
					<g:hiddenField name="${p.key}" value="${p.value}" />
				</g:if> 				
			</g:each>
			<fieldset class="buttons">
				<g:actionSubmit class="submit" action="show" value="Submit"  />
			</fieldset>
		</g:form>
		
	</body>
</html>