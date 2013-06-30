<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Show Graph</title>
		<r:require modules="jquery, jqplot, store_js" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="setup" action="setup">Setup graph</g:link></li>
				<li><g:link class="setup" controller="graph" action="setupHeatmap">Setup Heatmap</g:link></li>
				<li><g:link class="find" controller="user" action="find">Find user</g:link></li>
				<li><g:link class="setup" action="setup" params="${params}">Filter graph</g:link></li>
				<li><g:link class="setup" controller="options" action="colors">Colors</g:link></li>
				<li><g:link class="setup" controller="graph" action="updateCache">Update cache</g:link></li>
			</ul>
		</div>
		
		<div id="progress">
		</div>
		
		<div id="show-graph" class="content" role="main">
			<h1>${artistName}<g:if test="${userName && artistName}"> - </g:if>${userName}</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="shorten-link-button"><h5>Link to this page</h5>
			<input id="shorten-link-result" type="text" readonly="" />
			</div>
		
			<div id="chartdiv" style="height:800px;width:95%;"></div>
			
		</div>
		
		<r:script>
			var response;
			var responseObject;
			var maxY;
			var params = []; 
			var ONE_DAY = 1000 * 60 * 60 * 24;

			var rawParams = $(location).attr('href');
			rawParams = rawParams.substring(rawParams.indexOf('?')+1, rawParams.length);
			params = rawParams.split('&');
			params.sort();
			
			function graph() {
				if (!responseObject) {
					console.log("response: " + response.responseText);
					responseObject = JSON.parse(response.responseText);
					responseObject.date = new Date();
					<g:if env="production">
						store.set(JSON.stringify(params), responseObject);
					</g:if>
				}
				
				if (responseObject.error) {
					console.log("error" + responseObject.error);
					$("#chartdiv").text(responseObject.error);
					return;
				}
				
				var chartdata = responseObject.chartdata;
				maxY = chartdata.maxY;
				var series = chartdata.series;
				var data = chartdata.data;

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
			
			function showShortened(data, textStatus) {
				$("#shorten-link-result").show();
				console.log("data: " + data);
				console.log(data);
				console.log("textStatus: " + textStatus);
				
				$("#shorten-link-result").val(data.url);
				$("#shorten-link-result").select();
				$("#shorten-link-result").attr('size', $("#shorten-link-result").val().length);
				
				$("#shorten-link-button").click( function() {
					$("#shorten-link-result").select();
				});	
			}
			
			function errorFunc(XMLHttpRequest,textStatus,errorThrown) {
				console.log("error: " + textStatus + ", " + errorThrown);	
			}

			$(window).load(function() {
				// deal with short link
				$("#shorten-link-result").hide();
				$("#shorten-link-button").click( function() {
					$("#shorten-link-button").unbind();
					$("#shorten-link-result").show;
					
					// ajax call to get shortened url
					$.ajax({type:'POST',data:{fullUrl: window.location.pathname + window.location.search}, url:'${createLink(controller: 'shortLink', action: 'ajaxShortenUrl')}',success:showShortened,error:errorFunc});
				});
			
				<g:if env="production">
					var storedData = store.get(JSON.stringify(params));
	
					// cache result for 1 day
					if (storedData && ((new Date()) - (new Date(storedData.date))) < ONE_DAY) {
						responseObject = storedData;
						graph();
						return
					}
				</g:if>

				response = ${remoteFunction(action: "ajaxGraphData", onComplete: "graph()", params: params)};
			});
		</r:script>
		
		
		
		
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