
<%@ page import="com.ademuri.hipster.User" %>
<%@ page import="com.ademuri.hipster.UserArtist" %>
<%@ page import="grails.converters.JSON" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
		<r:require modules="jquery, jquery-ui" />
	</head>
	<body>
		<a href="#show-user" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="setup" controller="graph" action="setup">Setup graph</g:link></li>
				<li><g:link class="setup" controller="graph" action="setupHeatmap">Setup Heatmap</g:link></li>
				<li><g:link class="find" action="find">Find user</g:link></li>
			</ul>
		</div>
		<div id="show-user" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list user">
			
				<g:if test="${userInstance?.username}">
				<li class="fieldcontain">
					<span id="username-label" class="property-label"><g:message code="user.username.label" default="Username" /></span>
					
						<span class="property-value" aria-labelledby="username-label"><g:fieldValue bean="${userInstance}" field="username"/></span>
					
				</li>
				</g:if>
			
				<li class="fieldcontain">
					<span id="friends-label" class="property-label"><g:message code="user.friends.label" default="Friends" /></span>
					
						<g:each in="${userInstance.friends}" var="a">
						<span class="property-value" aria-labelledby="friends-label"><g:link controller="user" action="show" id="${a.id}">${a?.toString()}</g:link></span>
						</g:each>
				</li>
			
				<li class="fieldcontain">
					<span id="interval-label" class="property-label">Interval</span>
					<span id="interval" class="property-value" aria-labelledby="interval-label">3 month</span>
				</li>
				<div id="interval-container">
					<div id="interval-slider"></div>
					<div id="interval-7day">7 Days</div>
					<div id="interval-overall">Overall</div>
				</div>
				
				<br>
				<li class="fieldcontain">
					<g:link class="graph property-value" controller="graph" action="show" elementId="graphInterval">Graph</g:link>
				</li>
			
				<li class="fieldcontain">
					<span id="artists-label" class="property-label"><g:message code="user.artists.label" default="Artists" /></span>
					<g:each in="${0..5}" var="i"> 
						<span id="artists${i}" class="property-value" aria-labelledby="artists-label">
							<ul>
								<g:render template="updateArtists" model="[artistList: artistList, user: user]" />
							</ul>
						</span>
					</g:each>
				</li>
			
			</ol>
			<g:if env="development">
				<g:form>
					<fieldset class="buttons">
						<g:hiddenField name="id" value="${userInstance?.id}" />
						<g:link class="edit" action="edit" id="${userInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
						<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					</fieldset>
				</g:form>
			</g:if>
		</div>
		
		<r:script>
		var rankNames = ${UserArtist.humanRankNames as JSON}
		var requested = new Array();	// whether we've synced each interval
		var received = new Array();
		var interval = 2;
		var users;		// html fragment from template

		function showHide() {
			if (!received[interval]) {
				return;
			}
			
			for (var i=0; i<=5; i++) {
				if (i != interval) {
					$("#artists" + i).hide();
					$("#artists" + i).addClass("hidden");
					$("#artists" + i).removeClass("shown");
				} else {
					$("#artists" + i).show();
					$("#artists" + i).addClass("shown");
					$("#artists" + i).removeClass("hidden");
				}
			}

			// update Graph button
			var link = $("#graphInterval");
			var href = link.prop("href");
			var to = href.indexOf("?");
			if (to > 0) {
				href = href.substring(0, to);
			}
			href += "?";

			$.each($("span.shown li"), function(i, val) {
				href += "a_" + i + "=" + val.id + "&";
				if (i > 8) {
					return false;	// don't graph 20 artists!
				}
			}); 

			href += "by=1&u_0=" + ${userInstance.id};

			link.prop("href", href);
		}
		
		function updateArtists(newInterval) {
			interval = newInterval;
			$("#interval").text(rankNames[interval]);
			if (!requested[interval]) {
			 	requested[interval] = true;
				$.ajax({type:'POST',data:{'interval': newInterval, 'id': ${userInstance.id}}, url:'${createLink(action:'ajaxGetTopArtists')}',success:function(data,textStatus){jQuery('#artists' + newInterval).html(data); received[newInterval] = true; showHide();},error:function(XMLHttpRequest,textStatus,errorThrown){}});
			} else {
				showHide();
			}
		}
		
		function updateUsers() {
			console.log(users)
			if (users.responseText != '["empty"]') {
				$("#friends-label").parent().html(users.responseText);
			}
		}
		
		$(document).ready( function() {
			for (var i=0; i<=5; i++) {
				requested[i] = false;
				received[i] = false;
			}
			
			users = ${remoteFunction(action: "ajaxGetFriends", onComplete: "updateUsers()", params: params)}
			
			$("#interval-slider").slider({
				value:2,
				min:0,
				max:5,
				step:1,
				animate: "fast",
				slide: function (event, ui) {
					updateArtists(ui.value);
				}
			});
			
			updateArtists(2);
		});
		</r:script>
		
	</body>
</html>
