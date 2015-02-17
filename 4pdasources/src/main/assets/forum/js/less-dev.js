(function(w,d){
	var
		e = d.getElementsByTagName ( 'script' )
	,	p = e [ e.length - 1 ].getAttribute ( 'src' )
	,	s = ['jquery.js','less.js','devbar.js','devless.js','devselector.js']
	,	i
	,	c = 0
	,	j = function(){
			if ( s.length > c ) {
				var n = d.createElement ( 'script' );
				n.type = 'text/javascript';
				n.src = p + s[c];
				n.onload = j;
				d.getElementsByTagName ( 'head' ) [ 0 ].appendChild ( n );
				c++;
			}
		}
	;
	p = p.substr ( 0, p.lastIndexOf('/') + 1 )+'dev/';
	j();
;
})(window,document);
