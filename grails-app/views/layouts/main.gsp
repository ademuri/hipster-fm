<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="Hipster"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
		<r:require modules="jquery-ui" />
		<g:layoutHead/>
		<r:layoutResources />
		
		<!-- show spinner automatically when Ajax is running -->
		<r:script>
			$(document).ready(function() {
				$("#spinner").bind("ajaxSend", function() {
					$(this).show();
				}).bind("ajaxStop", function() {
					$(this).hide();
				}).bind("ajaxError", function() {
					$(this).hide();
				});
			});
		</r:script>
	</head>
	<body>
		
			
		
		<div id="header" role="banner">Hipster</div>
		<div class="nav" role="navigation">
			<ul>
				<li><g:link class="setup" controller="graph" action="setup">Graph</g:link></li>
				<li><g:link class="setup" controller="graph" action="setupHeatmap">Heatmap</g:link></li>
				<li><g:link class="find" controller="user" action="find">Find user</g:link></li>
				<li><g:link class="setup" controller="options" action="colors">Colors</g:link></li>
				<g:if env="development">
				<li><g:link class="fetchTopArtists" controller="graph" action="fetchTopArtists">Fetch top artists</g:link></li>
				</g:if>
			</ul>
		</div>
		
		<g:layoutBody/>
		<div class="footer" role="contentinfo">
			<span class="footer-links">
				<span><g:link controller="about" action="licenses">license</g:link></span> | 
				<span><g:link controller="about" action="source">source</g:link></span>
			</span>
			<span class="last-fm-logo">
				Powered by <a href="last.fm"><r:img class="last-fm-logo-img" dir="images/last-fm" file="badge_black_small.gif" width="80" height="28" /></a>
			</span>
		</div>
		<div id="spinner" class="spinner" style="display:none;">
			<r:img dir="images" file="spinner.gif" width="16" height="16" />
		</div>
		<g:javascript library="application"/>
		<r:layoutResources />
	</body>
</html>