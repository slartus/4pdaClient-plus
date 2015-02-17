(function($){
	var
		devbar = $('#devbar')
	,	tabs = devbar.find("#dev-tabs")
	,	content = devbar.find("#dev-content")
	,	styleElement = $('<style type="text/css">')
	;
	tabs.append ( '<button class="btn" data-tab="less">L</button><button class="btn" data-tab="edit">E</button>' );
	content.append ( '<div data-tab-name="less">\
	<h1>CSS/LESS</h1>\
'+( window["DEVOUT"] && window["DEVOUT"]["showChooseCssDialog"] ? '<button class="btn" id="dev-less-file-compile">Выбрать стиль</button>' : '<input type="file" class="btn" id="dev-less-file-select"/> <button class="btn" id="dev-less-file-compile">Применить</button>' )+'\
<br /><button id="dev-less-show" class="btn" data-toggle="hide-element" data-target="#dev-less-out"  disabled="disabled">Показать/скрыть итоговый код</button> \
<textarea id="dev-less-out" style="width:100%;" class="hide-element" rows="10" readonly="readonly"></textarea> \
<pre id="dev-less-error" style="color:red!important;"></pre><br /><br /> \
</div>\
<div data-tab-name="edit">\
	<h1>EDIT</h1>\
	<textarea id="dev-less-edit" style="width:100%;" rows="10"></textarea>\
	<button id="dev-less-edit-compile" class="btn">Применить изменения</button><br /><br />\
</div>' );
	$('head').append(styleElement);

	function parseLess ( str ) {
		var parser = less.Parser({
			env: "development",
			logLevel: 2,
			async: false,
			fileAsync: false,
			poll: 1000,
			functions: {},
			dumpLineNumbers: "comments",
		});
		$('#dev-less-out').val('');
		$('#dev-less-error').html('').hide();
		$('#dev-less-edit').val ( str );
		parser.parse ( str, function(error, result) {
			if ( error == null ) {
				try {
					var out = result.toCSS();
					styleElement.html(out);
					$('#dev-less-out').val ( out );
					$('#dev-less-show').removeAttr("disabled");
				}
				catch ( err ) {
					$('#dev-less-out').val ( '' );
					$('#dev-less-edit').val ( '' );
					$('#dev-less-error').show().html ( 'Line '+err.line+' Index '+err.index+' Message: '+err.message+"\n"+err.extract.join("\n") );
					$('#dev-less-show').attr ( "disabled", "disabled" );
				}
			}
			else {
				$('#dev-less-out').val ( '' );
				$('#dev-less-edit').val ( '' );
				$('#dev-less-error').show().html ( 'Line '+error.line+' Index '+error.index+' Message: '+error.message+"\n"+error.extract.join("\n") );
				$('#dev-less-show').attr ( "disabled", "disabled" );
			}
		} );
	}
	window["HtmlInParseLessContent"] = parseLess;

	$('body')
	.on ( 'click', '#dev-less-edit-compile', function ( ev ) {
		ev.preventDefault();
		parseLess ( $('#dev-less-edit').val() );
	} )
	.on ( 'click', '#dev-less-file-compile', function ( ev ) {
		ev.preventDefault();
		if ( window["DEVOUT"] && window["DEVOUT"]["showChooseCssDialog"] ) {
			DEVOUT.showChooseCssDialog();
		}
		else {
			if ( $('#dev-less-file-select')[0].files.length != 1 ) {
				alert('Выберите файл стиля!');
			}
			else {
				var file = $('#dev-less-file-select')[0].files[0];
				var reader;
				try { reader = new FileReader(); } catch(e){}
				if ( !reader ) {
					alert('Невозможно прочитать файл стиля, недостаточно прав.');
				}
				else {
					reader.onloadend = function ( evt ) {
						if ( evt.target.readyState == FileReader.DONE ) { 
							if ( evt.target.error ) {
								var n = false;
								for ( var m in evt.target.error ) {
									if ( evt.target.error [ m ] == evt.target.error.code ) {
										n = m;
									}
								}
								if ( !n ) {
									n = 'UNKNOWN ERROR CODE: '+evt.target.error.code;
								}
								alert ( n );
							}
							else {
								parseLess ( evt.target.result );
							}
						}
					};
					reader.readAsText ( file.webkitSlice ? file.webkitSlice ( 0, file.size ) : file.slice ( 0, file.size ), "UTF-8" );
				}
			}

		}
	} );
	parseLess('');
})(jQuery);
