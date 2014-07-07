(function($){
	$('body').prepend ( $( '<div id="devbar">\
	<div id="dev-tabs">\
		<button class="btn" data-toggle="open" data-target="#devbar">D</button>\
	</div>\
	<div id="dev-content"></div>\
</div>' ) );
	less.Parser().parse ( '\
#devbar{\
	position:fixed;width:2em;right:-1px;top:-1px;z-index:9999;background:#FFF!important;color:#000!important;padding:0!important;margin:0!important;border:solid 1px #000;\
	font-size:16px!important;\
	max-height:100%;\
	textarea,input,button{\
		box-sizing:border-box;-moz-box-sizing:border-box;-webkit-box-sizing:border-box;\
	}\
	.hide-element {\
		display:none;\
	}\
	#dev-content {\
		overflow:hidden;display:none;max-height:100%;overflow-y:auto;\
		> *{\
			display:none;\
		}\
		> .show {\
			display:block;\
		}\
	}\
	#dev-tabs {\
		margin:0;float:left;\
		.btn {\
			font-size:1em;\
			width:100%;\
		}\
		> * {\
			display:none;\
			&:first-child {\
				display:block;\
			}\
		}\
	}\
	&.open{\
		width:auto;left:-1px;\
		display:block;\
		#dev-content {\
			display:block;\
			h1 {\
				font-size:1.3em;font-weight:bold;text-align:center;margin:0;padding:0;\
			}\
		}\
		#dev-tabs {\
			margin: 0 .5em 0 0;\
			> * {\
				display:block;\
			}\
		}\
	}\
	.btn {\
		font-size:.8em;margin:0;padding:.5em;min-width:2em;line-height:1;\
	}\
}', function ( error, result ) { $('head').append ( '<style type="text/css">'+result.toCSS()+'</style>' ); } );

	$('[data-toggle="open"][data-target="#devbar"]').click ( function () {
		$(this).next().click();
	} );
	$('body').on ( 'click', '[data-toggle]', function ( ev ) {
		var
			$this = $(this)
		,	toggle = $this.data ( 'toggle' )
		,	target = $this.data ( 'target' ) || $this.attr ( 'target' )
		;
		if ( toggle && target && $(target).size() ) {
			ev.preventDefault();
			$(target).toggleClass ( toggle );
		}
	} );

	$('body').on ( 'click', '[data-tab]', function ( ev ) {
		var
			$this = $(this)
		,	tab = $this.data ( 'tab' )
		;
		if ( tab ) {
			ev.preventDefault();
			$('#devbar #dev-content > *').hide();
			$('#devbar #dev-content [data-tab-name="'+tab+'"]').show();
		}
	} );
})(jQuery);
