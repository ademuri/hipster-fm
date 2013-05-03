<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Options - Colors</title>
		<r:require modules="jquery, jqplot, spectrum" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
			</ul>
		</div>
		
		<div id="options-colors" class="content" role="main">
			<g:form>
				<g:render template="colorpicker" />
				<g:render template="colorpicker" />
				<g:render template="colorpicker" />
				<div class="color-picker">
					<span class="color-button color-add last-add">+</span>
				</div>
			</g:form>
		</div>
		
		<script>
			var options = {
					showInput: true,
					showPalette: true,
					showSelectionPalette: true,
					clickoutFiresChange: true,
					localStorageKey: "spectrum.colors",
					showButtons: false
				}
			
			$(document).ready( function() {
				$("#options-colors .spectrum-input").spectrum(options);

				remove = function() {
					$(this).parent().remove();
				}
				$(".color-remove").click(remove);
				
				// add/remove colors
				$(".color-add").click(add = function() {
					var thing = $("#color-picker-template div").clone(); 
					thing.insertBefore($(this).parent());
					thing.children(".spectrum-input").spectrum(options);
					thing.children(".color-add").click(add);
					thing.children(".color-remove").click(remove);
				});
				
			});
		</script>
	<div id="color-picker-template">
		<g:render template="colorpicker" />
	</div>	
		
	</body>
</html>