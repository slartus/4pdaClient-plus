(function($){
	var
		devbar = $('#devbar')
	,	tabs = devbar.find("#dev-tabs")
	,	content = devbar.find("#dev-content")
	;
	tabs.append ( '<button class="btn" data-tab="query">Q</button>' );
	content.append ( '<div data-tab-name="query">\
	<h1>Query</h1>\
<button class="btn" id="dev-query-select">Выбрать элемент</button><br />\
<div id="dev-query-path"></div>\
<div id="dev-query-info"></div>\
<textarea id="dev-query-desc" style="width:100%" rows="7"></textarea>\
</div>' );

	var
		path = $('#dev-query-path')
	,	info = $('#dev-query-info')
	;

	function getSelInfo ( el, deep ) {
		var info = el.tagName;
		if ( el.id ) {
			info += '#'+el.id;
		}
		if ( el.className ) {
			info += '.'+el.className;
		}
		if ( deep ) {
			for ( var i = 0 ; i < el.attributes.length ; i++ ) {
				if ( el.attributes[i].name == 'class' || el.attributes[i].name == 'id' ) {
					continue;
				}
				info += '['+el.attributes[i].name+'="'+el.attributes[i].value+'"]';
			}
		}
		return info;
	}

	function showInfoAboutElement ( ev ) {
		ev.preventDefault();
		var obj = this.object;
		var info = getSelInfo ( obj, 1 )+"\n";
		var cs = window.getComputedStyle ( obj, null ), i;
		info += "\nATTRIBUTES:\n";
		for ( i = 0 ; i < obj.attributes.length ; i ++ ) {
			info += "  "+obj.attributes[i].name+" = "+obj.attributes[i].value+"\n";
		}
		info += "\nSTYLES:\n";
		for ( i = 0 ; i < cs.length ; i ++ ) {
			info += "  "+cs[i]+" : "+cs.getPropertyValue(cs[i])+"\n";
		}
		$('#dev-query-desc').val ( info );
	}

	function selectElement ( ev ) {
		$('body').off ( 'click', selectElement );
		ev.preventDefault();
		ev.stopPropagation();
		$('#devbar').show();
		$('#dev-query-info').html('');
		$('#dev-query-path').html('');
		var el = document.createElement ( 'a' );
		el.href = "#";
		el.object = ev.target;
		$('#dev-query-info').append ( $(el).click(showInfoAboutElement).text ( getSelInfo ( ev.target, 1 ) ) );
		var p = $(ev.target).parent();
		while ( p.size() && p[0] != document ) {
			( el = document.createElement ( 'a' ) ).href = "#";
			el.object = p[0];
			$('#dev-query-path').prepend ( ' > ' ).prepend ( $(el).click(showInfoAboutElement).text ( getSelInfo ( p[0] ) ) );
			p = p.parent();
		}
	}

	$('body').on ( 'click', '#dev-query-select', function ( ev ) {
		ev.preventDefault();
		$('#devbar').hide();
		$('body').on ( 'click', selectElement );
	} );
})(jQuery);
