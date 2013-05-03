<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Options - Colors</title>
		<r:require modules="jquery, jqplot, spectrum, store_js" />
	</head>
	<body>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
			</ul>
		</div>
		
		<div id="options-colors" class="content" role="main">
			<g:form onsubmit="return setcolors()">
				<g:render template="colorpicker" />
				<div class="color-picker">
					<span class="color-button color-add last-add">+</span>
				</div>
				
				<g:actionSubmit value="Apply"/>
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
			
			function remove() {
				$(this).parent().remove();
			}
			
			 function add() {
				var thing = $("#color-picker-template div").clone(); 
				thing.insertBefore($(this).parent());
				thing.children(".spectrum-input").spectrum(options);
				thing.children(".color-add").click(add);
				thing.children(".color-remove").click(remove);
			}
			
			$(document).ready( function() {
				var colors = store.get('colors');
				var picker = $("#options-colors .spectrum-input");

				if (!colors) {
					colors = ["#FF0000", "#FFBA10", "#970CE8", "#0D4EFF", "#E87C15", 
								"#1ECC21", "#00E8C2", "#E232EA", "#4F826A", "#999999",
								"#333333", "#804000", "#FF6AD7", "#80002E", "#77AAFF"
								];
				}
				
				for(var i=0; i<colors.length-1; i++) {
					add.call(picker);
				}
				
				$("#options-colors .spectrum-input").spectrum(options);
				$(".color-remove").click(remove);
				$(".color-add").click(add);

				$("#options-colors .spectrum-input").each (function(i, val) {
					$(val).spectrum("set", colors[i]);
				});
				
			});

			function setcolors() {
				store.remove('colors');
				var colors = [];
				$("#options-colors .spectrum-input").each (function(i, val) {
					colors.push($(val).spectrum("get").toHexString());
				});
				//console.log("colors: " + colors);
				store.set('colors', colors);
				return false;
			}
		</script>
	<div id="color-picker-template">
		<g:render template="colorpicker" />
	</div>	
		
	</body>
</html>