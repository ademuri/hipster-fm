<%@ page import="com.ademuri.hipster.User" %>

<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Find User</title>
		<r:require modules="jquery, irex, store_js" />
	</head>
	<body>
		<div id="find-user" class="content" role="main">
			<h1>Find User</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			
			<div id="user-form">
				<g:form method="post" >
				<fieldset class="form">
					<span class="fieldcontain">
						<label for="username">
							Search for user
						</label>
						<g:textField name="username"/>
					</span>
					<g:actionSubmit class="submit" action="find" value="Search" />
					<r:img dir="images" file="spinner.gif" width="16" height="16" id="user-spinner" />
				</fieldset>
				</g:form>
			</div>
		</div>
		<script>
		$(document).ready( function() {
			$("#spinner").offset({top: -100, left: -100});
			$("#username").autocomplete({
				source: [],
				delay: 0,
				length: 2
			});
			getCache('username_cache', '#user-spinner', '${createLink(controller: 'user', action: 'ajaxGetUserList')}', 
				function(data) {
					$("#username").autocomplete("option", "source", data);
				});
		});	
		</script>
	</body>
</html>