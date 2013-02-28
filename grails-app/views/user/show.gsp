
<%@ page import="hipsterfm.User" %>
<%@ page import="hipsterfm.UserArtist" %>
<%@ page import="grails.converters.JSON" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-user" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
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
			
				<g:if test="${userInstance?.email}">
				<li class="fieldcontain">
					<span id="email-label" class="property-label"><g:message code="user.email.label" default="Email" /></span>
					
						<span class="property-value" aria-labelledby="email-label"><g:fieldValue bean="${userInstance}" field="email"/></span>
					
				</li>
				</g:if>
				
				<g:if test="${userInstance?.friends}">
				<li class="fieldcontain">
					<span id="friends-label" class="property-label"><g:message code="user.friends.label" default="Friends" /></span>
					
						<g:each in="${userInstance.friends}" var="a">
						<span class="property-value" aria-labelledby="friends-label"><g:link controller="user" action="show" id="${a.id}">${a?.toString()}</g:link></span>
						</g:each>
					
				</li>
				</g:if>
			
				<li class="fieldcontain">
					<span id="interval-label" class="property-label">Interval</span>
					<span id="interval" class="property-value" aria-labelledby="interval-label">3 month</span>
				</li>
				<div id="interval-container">
					<div id="interval-slider"></div>
				</div>
			
				<li class="fieldcontain">
					<span id="artists-label" class="property-label"><g:message code="user.artists.label" default="Artists" /></span>
					<span id="artists" class="property-value" aria-labelledby="artists-label">
						<ul>
							<g:render template="updateArtists" model="[artistList: artistList]" />
						</ul>
					</span>
				</li>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${userInstance?.id}" />
					<g:link class="edit" action="edit" id="${userInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
					<g:link class="getFriends" action="getFriends" id="${userInstance.id}">Get Friends</g:link>
					<g:remoteLink action="ajaxGetTopArtists" update="artists" id="${userInstance.id}">Get top artists</g:remoteLink>
				</fieldset>
			</g:form>
		</div>
		
		<div id="Artist Search">
			<g:form name="artistSearch" action="artistSearch">
				<g:hiddenField name="id" value="${userInstance?.id}" />
				<g:textField name="artist" />
				<g:submitButton name="search" value="Search"/>
			</g:form>
		</div>
		
		<script>
		var rankNames = ${UserArtist.humanRankNames as JSON}
		function updateArtists(interval) {
			$("#interval").text(rankNames[interval]);
			jQuery.ajax({type:'POST',data:{'interval': interval, 'id': ${userInstance.id}}, url:'${createLink(action:'ajaxGetTopArtists')}',success:function(data,textStatus){jQuery('#artists').html(data);},error:function(XMLHttpRequest,textStatus,errorThrown){}});
		}
		
		$(function() {
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
		});

		$(document).ready( function() {
			console.log("here");
			updateArtists(2);
		});
		</script>
		
	</body>
</html>
