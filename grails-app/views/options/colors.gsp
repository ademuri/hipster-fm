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
			<div id="options-color-pickers">
				<g:form onsubmit="return false">
					<g:render template="colorpicker" />
					<div class="color-picker">
						<span class="color-button color-add last-add">+</span>
					</div>
					
					<g:actionSubmit value="Apply" class="apply" />
					<g:actionSubmit value="Reset" class="reset" />
					<g:textField name="schemeName"/>
					<g:actionSubmit value="Save" class="save" />
				</g:form>
			</div>
			
			<div id="options-color-palettes">
				<g:form onsubmit="return false">
					<div id="options-color-palettes-label">Colors</div>
					<ul class="color-palette-list">
					</ul>
				</g:form>
			</div>
			
			
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

			var defaultColors = ["#FF0000", "#FFBA10", "#970CE8", "#0D4EFF", "#E87C15", 
									"#1ECC21", "#00E8C2", "#E232EA", "#4F826A", "#999999",
									"#333333", "#804000", "#FF6AD7", "#80002E", "#77AAFF"
									];

			var greyScale = ["#BBB", "#999", "#777", "#555", "#333", "#000"];

			var colorSchemes = [{name: "Default", colors: defaultColors}, {name: "Greyscale", colors: greyScale}];
			
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
					colors = defaultColors;
				}
				
				for(var i=0; i<colors.length-1; i++) {
					add.call(picker);
				}

				$("#options-colors .spectrum-input").spectrum(options);
				$(".color-remove").unbind();
				$(".color-add").unbind();
				$(".color-remove").click(remove);
				$(".color-add").click(add);

				$("#options-colors .spectrum-input").each (function(i, val) {
					$(val).spectrum("set", colors[i]);
				});

				$(".reset").click(reset);
				$(".apply").click(setcolors);
				$(".save").click(save);

				// display color schemes
				if (store.get('color-schemes')) {
					colorSchemes = store.get('color-schemes');
				}
				
				var items = [];
				$.each(colorSchemes, function(i, scheme) {
					var item = '<li id="' + i + '">' + scheme.name + ': <div class="color-swatch">';
					$.each(scheme.colors, function(j, color) {
						item += '<div class="color-block" style="background-color:' + color + ';"></div>';
					});
					item += '</div></li>';

					items.push(item);			
				});

				$("#options-color-palettes ul").append(items.join(''));
				$("#options-color-palettes li").click( function() {
					console.log("id: " + $(this).attr("id"));
					store.set('colors', colorSchemes[$(this).attr("id")].colors);
					location.reload();
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
				$(".apply").effect("highlight", {}, 1000);
				return false;
			}

			function reset() {
				store.set('colors', defaultColors);
				location.reload();
			}

			function save() {
				var colors = [];
				$("#options-colors .spectrum-input").each (function(i, val) {
					colors.push($(val).spectrum("get").toHexString());
				});

				colorSchemes.push({name: $("#schemeName").val(), colors: colors});
				store.set('color-schemes', colorSchemes);
			}
		</script>
	<div id="color-picker-template">
		<g:render template="colorpicker" />
	</div>	
		
	</body>
</html>