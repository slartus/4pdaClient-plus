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
})(window, document);