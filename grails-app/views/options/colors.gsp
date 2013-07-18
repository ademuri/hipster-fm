<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>Options - Colors</title>
		<r:require modules="jquery, jquery-ui, spectrum, store_js" />
	</head>
	<body>
		<div id="tabs">
			<ul>
				<li><a href="#tabs-graph">Graph</a>
				<li><a href="#tabs-heatmap">Heatmap</a>
			</ul>
			
			<div id="tabs-graph"></div>
			<div id="tabs-heatmap"></div>
		</div>
		
		<div id="options-colors" class="content" role="main">
			<div id="options-color-pickers">
				<g:form onsubmit="return false">
					<g:render template="colorpicker" />
					<div class="color-picker">
						<span class="color-button color-add last-add">+</span>
					</div>
					
					<br>
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

			var greyScaleLong = ['#EEE', '#DDD', '#CCC', '#BBB', '#AAA', '#999', '#888', '#777', '#666', '#555', '#444', '#333', '#222', '#111', '#000'];

			var deut = ["#30F", "#F0C", "#F90", "#3F0", "#0CF", "#F03", "#930", "#060"
						];

			var deut2 = ["#F00", "#900", "#F36", "#F60", "#C06", "#C6C", "#909", "#6F3"
			 			];

 			var prot = ["#6F0", "#690", "#366", "#900", "#C09", "#606", "#F39", "#333"];

 			var deutanopia = ["#33F", "#F0F", "#009", "#000", "#600", "#933", "#F33", "#F39"];

 			var protanomaly = ["#03F", "#C3F", "#F06", "#F60", "#FC0", "#3F0", "#090", "#900", "#09F" ];

			var defaultColorSchemes = [{name: "Default", colors: defaultColors}, {name: "Greyscale", colors: greyScale},
			                    {name: "Greyscale long", colors: greyScaleLong},
			        			{name: "Deutanomaly", colors: deut}, {name: "Deutanamoly II", colors: deut2},
			        			{name: "Protanopy", colors: prot}, {name: "Deutanopy", colors: deutanopia},
			        			{name: "Protanomaly", colors: protanomaly}];
			        			
			var colorSchemes;
			        			
			var colorsFor = "graph";
			var colorsName = "colors-graph";
			var colorSchemesName = "color-schemes-graph";
			
			function remove() {
				$(this).parent().remove();
			}
			
			 function add() {
				var thing = $("#color-picker-template div").clone(); 
				thing.insertBefore($(this).parent());
				thing.children(".spectrum-input").spectrum(options);
				$(".color-remove").unbind();
				$(".color-add").unbind();
				$(".color-remove").click(remove);
				$(".color-add").click(add);
			}

			function displaySchemes(schemes) {
				var items = [];
				var maxId = $("#options-color-palettes li").length;
				
				$.each(schemes, function(i, scheme) {
					var item = '<li><span class="color-click" id="' + (i+maxId) + '">' + scheme.name + ': <div class="color-swatch">';
					$.each(scheme.colors, function(j, color) {
						item += '<div class="color-block" style="background-color:' + color + ';"></div>';
					});
					item += '</div></span><span class="color-scheme-delete" id="' + (i+maxId) + '">(X)</div></li>';

					items.push(item);			
				});

				$("#options-color-palettes ul").append(items.join(''));
				$("#options-color-palettes .color-click").click( function() {
					store.set(colorsName, colorSchemes[$(this).attr("id")].colors);
					displayScheme(colorSchemes[$(this).attr("id")].colors);
				});
				
				$("#options-color-palettes .color-scheme-delete").unbind();
				$("#options-color-palettes .color-scheme-delete").click( function() {
					colorSchemes.splice([$(this).attr("id")], 1);
					store.set(colorSchemesName, colorSchemes);
					$("#options-color-palettes li").remove();
					displaySchemes(colorSchemes);
				});
				
			}

			function displayScheme(colors) {
				// remove all but 1 color picker first
				var toRemove = $("#options-color-pickers .color-picker");
				toRemove.splice(0, 1);
				toRemove.remove();
			
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
			}
			
			function tabClicked() {
				var colors = store.get(colorsName);
				displayScheme(colors);
				
				// remove old schemes first
				$("ul.color-palette-list li").remove();

				// display color schemes
				if (store.get(colorSchemesName)) {
					colorSchemes = store.get(colorSchemesName);
				} else {
					colorSchemes = defaultColorSchemes.slice(0);
				}
				
				$("#schemeName").keyup(function(event){
				    if(event.keyCode == 13){
				        $(".save").click();
				    }
				});
				displaySchemes(colorSchemes);
			}
			
			$(document).ready( function() {
				$("#tabs").tabs();
				$("a[href=#tabs-graph]").click( function() {
					colorsName = "colors-graph";
					colorSchemesName = "color-schemes-graph";
					tabClicked();
				});
				$("a[href=#tabs-heatmap]").click( function() {
					colorsName = "colors-heatmap";
					colorSchemesName = "color-schemes-heatmap";
					tabClicked();
				});
				$("a[href=#tabs-graph]").click();
				
				
				$(".reset").click(reset);
				$(".apply").click(setcolors);
				$(".save").click(save);
			});

			function flash(el) {
				$("." + el).effect("highlight", {}, 1000);
			}
			
			function setcolors() {
				store.remove(colorsName);
				var colors = [];
				$("#options-colors .spectrum-input").each (function(i, val) {
					colors.push($(val).spectrum("get").toHexString());
				});
				store.set(colorsName, colors);
				flash("apply");
				return false;
			}

			function reset() {
				$("ul.color-palette-list li").remove();
				store.set(colorSchemesName, defaultColorSchemes);
				displaySchemes(defaultColorSchemes);
				
				store.set(colorsName, defaultColors);
				displayScheme(defaultColors);
				flash("reset");
				return false;
			}

			function save() {
				var colors = [];
				$("#options-colors .spectrum-input").each (function(i, val) {
					colors.push($(val).spectrum("get").toHexString());
				});

				var scheme = {name: $("#schemeName").val(), colors: colors};
				colorSchemes.push(scheme);
				store.set(colorSchemesName, colorSchemes);
				displaySchemes([scheme]);
				flash("save");
			}
		</script>
	<div id="color-picker-template">
		<g:render template="colorpicker" />
	</div>	
		
	</body>
</html>