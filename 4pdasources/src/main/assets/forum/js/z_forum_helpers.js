(function (n, h, r) {
    function e(b, a) {
        var c = b.className,
            d = RegExp("\\b" + a + "\\b", "gi");
        if (c) return d.test(c) ? b.className = c.replace(d, "") : b.className = c + " " + a, b
    }

    function p(b, a) {
        b.addEventListener ? b.addEventListener("click", a, !1) : b.attachEvent && b.attachEvent("onclick", a)
    }

    function q(b) {
        var a;
        return (a = l(b, a)) && a.length ? a[0] : r
    }

    function l(b, a) {
        a && a.querySelectorAll || (a = h);
        return a.querySelectorAll(b)
    }(function (b, a) {
        function c(a) {
            var b = e()[1];
            b.innerHTML = d("file:///android_asset/forum/style_images/1/qr_code.gif");
            b.innerHTML =
                d("http://qrcode.kaywa.com/img.php?s=10&d=" + encodeURIComponent(a));
            e()[0].setAttribute("style", "position:fixed;left:0;top:0;right:0;bottom:0;display:-webkit-flex;display:-ms-flex:display:none;background:rgba(0,0,0,.5);-ms-flex-align:center;-webkit-align-items:center;align-items:center;z-index:65533")
        }

        function d(a) {
            return '<img src="' + a + '" border="0" style="' + m(0, 0) + '" align="middle">'
        }

        function e() {
            var b = q("#qrPopupWindowBackground"),
                c = q("#qrPopupWindowContent");
            b || (b = a.createElement("div"), b.id = "qrPopupWindowBackground",
                b.setAttribute("style", "display:none;"), b.onclick = function () {
                    this.style.display = "none";
                    return !1
                }, b.title = "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0434\u043b\u044f \u0437\u0430\u043a\u0440\u044b\u0442\u0438\u044f", a.body.appendChild(b));
            c || (c = a.createElement("div"), c.id = "qrPopupWindowContent", c.setAttribute("style", "width:auto;" + m("8px", "0 auto") + "background:#FFF;z-index:65534;-webkit-border-radius:8px;border-radius:8px;-webkit-box-shadow:0 0 10px 0 #000;box-shadow:0 0 10px 0 #000;"), b.appendChild(c));
            return [b, c]
        }

        function m(b, a) {
            return "padding:" + b + ";margin:" + a + ";"
        }
        b.jsShowQr = c;
        b.jsParseQrForum = function () {
            for (var d = l(".borderwrap .postcolor a"), e = 0, h, g, k, f; e < d.length; e++)(g = (h = d[e]).getAttribute("href")) && "" != g && "#" != g.substr(0, 1) && "java" != g.substr(0, 4) && (k = a.createElement("img"), k.alt = k.title = "QR", k.setAttribute("style", "border:0;vertical-align:middle;" + m(0, "0 3px") + "cursor:pointer"), k.src = "file:///android_asset/forum/style_images/1/qr_code.gif", f = g.split(":")[0], "http" != f && "https" != f && "market" != f &&
                "magnet" != f && (f = b.location.pathname, f = "/" == g.substr(0, 1) ? "" : -1 == f.lastIndexOf("/") ? "/" : f.substr(0, f.lastIndexOf("/") + 1), g = b.location.protocol + "//" + b.location.host + f + g), k.a = g, k.onclick = function () {
                    c(this.a);
                    return !1
                }, h.parentNode.insertBefore(k, h))
        }
    })(n, h);
    (function (b) {
        b.initPostBlock = function () {
            var a, c;
            a = l(".post-block.close>.block-title,.post-block.open>.block-title");
            for (c = 0; c < a.length; c++) 1 !== parseInt(a[c].getAttribute("data-block-init")) && (a[c].setAttribute("data-block-init", 1), p(a[c], function (a) {
                a ||
                    b.event;
                a.preventDefault ? a.preventDefault() : a.returnValue = !1;
                e(e(this.parentNode, "close"), "open")
            }));
            a = l(".post-block.box>.block-title,.post-block.unbox>.block-title");
            for (c = 0; c < a.length; c++)
                if (1 !== parseInt(a[c].getAttribute("data-block-init"))) {
                    a[c].setAttribute("data-block-init", 1);
                    try {
                        var d = a[c].nextSibling;
                        if (d && !1 !== d.className.indexOf("post-body") && d.scrollHeight <= d.offsetHeight) {
                            e(d.parentNode, "box");
                            continue
                        }
                    } catch (h) {}
                    p(a[c], function (a) {
                        a || b.event;
                        a.preventDefault ? a.preventDefault() : a.returnValue = !1;
                        e(e(this.parentNode, "box"), "unbox")
                    })
                }
        }
    })(n, h)

    (function ( window, document ) {
    		c_post_block = "post-block"
    	,	c_block_title = "block-title"
    	,	c_open = "open"
    	,	c_close = "close"
    	,	c_box = "box"
    	,	c_unbox = "unbox"
    	,	c_quote = "quote"
    	,	c_hidden = "hidden"
    	,	hasClass = function ( e, c ) {
    		 return ( e.className && ( (' '+e.className+' ').indexOf ( ' '+c+' ' ) > -1 ) );
    		}
    	,	cl_enable = 1
    	,	b_touch = !1
    	,	fn_ev = function (ev) {
    			if ( ev.isDefaultPrevented() ) {
    				return;
    			}
    			var
    				_t = ev.target
    			,	_p
    			;
    			// check for click in block-title
    			while ( _t && _t != document && !hasClass ( _t, c_block_title ) ) {
    				_t = _t.parentNode;
    			}
    			if ( _t && _t != document && ( _p = _t.parentNode ) ) {
    				if ( hasClass ( _t, c_block_title ) && hasClass ( _p, c_post_block ) && cl_enable ) {
    					cl_enable = 0;
    					setTimeout ( function () { cl_enable = 1 }, 300 );
    					if ( hasClass ( _p, c_open ) || hasClass ( _p, c_close ) ) {
    						toggleClass ( toggleClass ( _p, c_close ), c_open );
    						return false;
    					}
    					if ( hasClass ( _p, c_unbox ) || hasClass ( _p, c_box ) ) {
    						toggleClass ( toggleClass ( _p, c_unbox ), c_box );
    						return false;
    					}
    				}
    			}
    		}
    	;
    	addEvent ( document, 'click', fn_ev );
    	addEvent ( document, 'touchstart', function ( ev ) {
    		if ( ev.isDefaultPrevented() || b_touch || ev.touches.length != 1 ) {
    			b_touch = !1;
    			return;
    		}
    		var
    			_t = ev.target
    		,	_p
    		;
    		// check for click in block-title
    		while ( _t && _t != document && !hasClass ( _t, c_block_title ) ) {
    			_t = _t.parentNode;
    		}
    		if ( _t && _t != document && ( _p = _t.parentNode ) ) {
    			if ( hasClass ( _t, c_block_title ) && hasClass ( _p, c_post_block ) && cl_enable ) {
    				b_touch = {	t : ev.touches[0], e : ev.target };
    				setTimeout ( function() { b_touch = !1 }, 300 );
    			}
    		}
    	} );
    	addEvent ( document, 'touchend', function ( ev ) {
    		if ( ev.isDefaultPrevented() || !b_touch || ( ev.changedTouches.length != 1 && ev.touches.length != 1 ) || ev.target != b_touch.e ) {
    			b_touch = !1;
    			return;
    		}
    		var t = ev.changedTouches[0] || ev.touches[0],
    		d = Math.sqrt((b_touch.t.screenY-b_touch.t.screenX)^2)+((t.screenY-t.screenX)^2),
    		r1 = (b_touch.t.radiusX + b_touch.t.radiusY) / 2,
    		r2 = (t.radiusX + t.radiusY) / 2,
    		el = ev.target;
    		if ( d > r1+r2 ) {
    			if ( el.fireEvent ) {
    				el.fireEvent ( 'onclick' );
    			}
    			else {
    				var evObj = document.createEvent('Events');
    				evObj.initEvent ( 'click', true, false );
    				el.dispatchEvent(evObj);
    			}
    		}
    	} );
    	window["initPostBlock"] = function() {
    		var els,p,e,pc,i=0;
    		try{
    			els=document.getElementsByName(location.hash.replace(/^#/,''));
    			for( i = 0 ; i < els.length ; i++ ) {
    				e=[];
    				pc = 0;
    				if ( els[i].tagName=="A" ) {
    					p = els[i].parentNode;
    					while ( p && p != document.body ) {
    						if ( ( pc = /^post-main-\d+$/.test ( p.id ) ) ) {
    							break;
    						}
    						p = p.parentNode;
    						e.push(p);
    					}
    				}
    				if ( !pc ) {
    					continue;
    				}
    				while ( e.length ) {
    					p = e.pop();
    					if ( hasClass ( p, c_post_block ) && hasClass ( p, c_close ) ) {
    						toggleClass ( toggleClass ( p, c_close ), c_open );
    					}
    				}
    			}
    		}catch(e){}
    		if ( !(function () {
    			var
    				className = 'checkPseudoSupport'
    			,	css = '.'+className+'{position:fixed;left:-100px;top:-100px;display:block;width:auto;height:auto} .'+className+':before{content:"";display:block;height:50px}'
    			,	body = document.body || document.getElementsByTagName('body')[0]
    			,	style = document.createElement('style')
    			,	div = document.createElement ( 'div' )
    			,	ret = false
    			;
    			style.type = 'text/css';
    			div.className = className;
    			if ( style.styleSheet ){
    				style.styleSheet.cssText = css;
    			} else {
    				style.appendChild(document.createTextNode(css));
    			}
    			body.appendChild ( style );
    			body.appendChild ( div );
    			ret = ( div.offsetHeight > 40 );
    			body.removeChild ( div );
    			body.removeChild ( style );
    			return ret;
    		})() ) {
    			var needhtml,addevent, blockTitle = "BLOCK";
    			els = querySelectorAll("."+c_post_block+">."+c_block_title)
    			for(i=0;i<els.length;i++){
    				addevent = 0;
    				p = (e = els[i]).parentNode;
    				needhtml = !(e.innerHTML+"").replace( /(^\s+)|(\s+$)/g, "");
    				if ( hasClass ( p, c_close ) || hasClass ( p, c_open ) ) {
    					blockTitle = "SPOILER";
    					addevent = 1;
    				}
    				else if ( hasClass ( p, c_box ) || hasClass ( p, c_unbox ) ) {
    					blockTitle = "CODE";
    					addevent = 1;
    				}
    				else if ( hasClass ( p, c_quote ) ) {
    					blockTitle = "QUOTE";
    				}
    				else if ( hasClass ( p, c_hidden ) ) {
    					blockTitle = "HIDE";
    				}
    				needhtml && (e.innerHTML = blockTitle);
    				addevent && addEvent(els[i],'click',fn_ev);
    			}
    		}
    	};
    })(window,document);
})(window, document);