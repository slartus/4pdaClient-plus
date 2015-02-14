(function (p, n, k) {
    var h = {
        ":happy:": ["happy.gif", 1],
        ";)": ["wink.gif", 1],
        ":P": ["tongue.gif", 1],
        ":-D": ["biggrin.gif"],
        ":lol:": ["laugh.gif", 1],
        ":rolleyes:": ["rolleyes.gif", 1],
        ":)": ["smile_good.gif", 1],
        ":beee:": ["beee.gif", 1],
        ":rofl:": ["rofl.gif", 1],
        ":sveta:": ["sveta.gif", 1],
        ":thank_you:": ["thank_you.gif", 1],
        "}-)": ["devil.gif", 1],
        ":girl_cray:": ["girl_cray.gif", 1],
        ":D": ["biggrin.gif", 1],
        "o.O": ["blink.gif", 1],
        ":blush:": ["blush.gif", 1],
        ":yes2:": ["yes.gif", 1],
        ":mellow:": ["mellow.gif"],
        ":huh:": ["huh.gif"],
        ":o": ["ohmy.gif"],
        "B)": ["cool.gif"],
        "-_-": ["sleep.gif"],
        "<_<": ["dry.gif"],
        ":wub:": ["wub.gif"],
        ":angry:": ["angry.gif"],
        ":(": ["sad.gif"],
        ":unsure:": ["unsure.gif"],
        ":wacko:": ["wacko.gif"],
        ":blink:": ["blink.gif"],
        ":ph34r:": ["ph34r.gif"],
        ":banned:": ["banned.gif"],
        ":antifeminism:": ["antifeminism.gif"],
        ":beta:": ["beta.gif"],
        ":boy_girl:": ["boy_girl.gif"],
        ":butcher:": ["butcher.gif"],
        ":bubble:": ["bubble.gif"],
        ":censored:": ["censored.gif"],
        ":clap:": ["clap.gif"],
        ":close_tema:": ["close_tema.gif"],
        ":clapping:": ["clapping.gif"],
        ":coldly:": ["coldly.gif"],
        ":comando:": ["comando.gif"],
        ":congratulate:": ["congratulate.gif"],
        ":dance:": ["dance.gif"],
        ":daisy:": ["daisy.gif"],
        ":dancer:": ["dancer.gif"],
        ":derisive:": ["derisive.gif"],
        ":dinamo:": ["dinamo.gif"],
        ":dirol:": ["dirol.gif"],
        ":diver:": ["diver.gif"],
        ":drag:": ["drag.gif"],
        ":download:": ["download.gif"],
        ":drinks:": ["drinks.gif"],
        ":first_move:": ["first_move.gif"],
        ":feminist:": ["feminist.gif"],
        ":flood:": ["flood.gif"],
        ":fool:": ["fool.gif"],
        ":friends:": ["friends.gif"],
        ":foto:": ["foto.gif"],
        ":girl_blum:": ["girl_blum.gif"],
        ":girl_crazy:": ["girl_crazy.gif"],
        ":girl_curtsey:": ["girl_curtsey.gif"],
        ":girl_dance:": ["girl_dance.gif"],
        ":girl_flirt:": ["girl_flirt.gif"],
        ":girl_hospital:": ["girl_hospital.gif"],
        ":girl_hysterics:": ["girl_hysterics.gif"],
        ":girl_in_love:": ["girl_in_love.gif"],
        ":girl_kiss:": ["girl_kiss.gif"],
        ":girl_pinkglassesf:": ["girl_pinkglassesf.gif"],
        ":girl_parting:": ["girl_parting.gif"],
        ":girl_prepare_fish:": ["girl_prepare_fish.gif"],
        ":good:": ["good.gif"],
        ":girl_spruce_up:": ["girl_spruce_up.gif"],
        ":girl_tear:": ["girl_tear.gif"],
        ":girl_tender:": ["girl_tender.gif"],
        ":girl_teddy:": ["girl_teddy.gif"],
        ":girl_to_babruysk:": ["girl_to_babruysk.gif"],
        ":girl_to_take_umbrage:": ["girl_to_take_umbrage.gif"],
        ":girl_triniti:": ["girl_triniti.gif"],
        ":girl_tongue:": ["girl_tongue.gif"],
        ":girl_wacko:": ["girl_wacko.gif"],
        ":girl_werewolf:": ["girl_werewolf.gif"],
        ":girl_witch:": ["girl_witch.gif"],
        ":grabli:": ["grabli.gif"],
        ":good_luck:": ["good_luck.gif"],
        ":guess:": ["guess.gif"],
        ":hang:": ["hang.gif"],
        ":heart:": ["heart.gif"],
        ":help:": ["help.gif"],
        ":helpsmilie:": ["helpsmilie.gif"],
        ":hemp:": ["hemp.gif"],
        ":heppy_dancing:": ["heppy_dancing.gif"],
        ":hysterics:": ["hysterics.gif"],
        ":indeec:": ["indeec.gif"],
        ":i-m_so_happy:": ["i-m_so_happy.gif"],
        ":kindness:": ["kindness.gif"],
        ":king:": ["king.gif"],
        ":laugh_wild:": ["laugh_wild.gif"],
        ":4PDA:": ["love_4PDA.gif"],
        ":nea:": ["nea.gif"],
        ":moil:": ["moil.gif"],
        ":no:": ["no.gif"],
        ":nono:": ["nono.gif"],
        ":offtopic:": ["offtopic.gif"],
        ":ok:": ["ok.gif"],
        ":papuas:": ["papuas.gif"],
        ":party:": ["party.gif"],
        ":pioneer_smoke:": ["pioneer_smoke.gif"],
        ":pipiska:": ["pipiska.gif"],
        ":protest:": ["protest.gif"],
        ":popcorm:": ["popcorm.gif"],
        ":rabbi:": ["rabbi.gif"],
        ":resent:": ["resent.gif"],
        ":roll:": ["roll.gif"],
        ":rtfm:": ["rtfm.gif"],
        ":russian_garmoshka:": ["russian_garmoshka.gif"],
        ":russian:": ["russian.gif"],
        ":russian_ru:": ["russian_ru.gif"],
        ":scratch_one-s_head:": ["scratch_one-s_head.gif"],
        ":scare:": ["scare.gif"],
        ":search:": ["search.gif"],
        ":secret:": ["secret.gif"],
        ":skull:": ["skull.gif"],
        ":shok:": ["shok.gif"],
        ":sorry:": ["sorry.gif"],
        ":smoke:": ["smoke.gif"],
        ":spiteful:": ["spiteful.gif"],
        ":stop_flood:": ["stop_flood.gif"],
        ":suicide:": ["suicide.gif"],
        ":stop_holywar:": ["stop_holywar.gif"],
        ":superman:": ["superman.gif"],
        ":superstition:": ["superstition.gif"],
        ":tablet_za:": ["tablet_protiv.gif"],
        ":tablet_protiv:": ["tablet_za.gif"],
        ":this:": ["this.gif"],
        ":tomato:": ["tomato.gif"],
        ":to_clue:": ["to_clue.gif"],
        ":tommy:": ["tommy.gif"],
        ":tongue3:": ["tongue3.gif"],
        ":umnik:": ["umnik.gif"],
        ":victory:": ["victory.gif"],
        ":vinsent:": ["vinsent.gif"],
        ":wallbash:": ["wallbash.gif"],
        ":whistle:": ["whistle.gif"],
        ":wink_kind:": ["wink_kind.gif"],
        ":yahoo:": ["yahoo.gif"],
        ":yes:": ["yes.gif"],
        ":[": ["confusion.gif"],
        "]-:{": ["girl_devil.gif"],
        ":*": ["kiss.gif"],
        "@}-'-,-": ["give_rose.gif"],
        ":'(": ["cry.gif"],
        ":-{": ["mad.gif"],
        "=^.^=": ["kitten.gif"],
        "(-=": ["girl_hide.gif"],
        "(-;": ["girl_wink.gif"],
        ")-:{": ["girl_angry.gif"],
        "*-:": ["girl_chmok.gif"],
        ")-:": ["girl_sad.gif"],
        ":girl_mad:": ["girl_mad.gif"],
        "(-:": ["girl_smile.gif"],
        ":acute:": ["acute.gif"],
        ":aggressive:": ["aggressive.gif"],
        ":air_kiss:": ["air_kiss.gif"],
        o_O: ["blink.gif"],
        ":-[": ["confusion.gif"],
        ":'-(": ["cry.gif"],
        ":lol_girl:": ["girl_haha.gif"],
        ")-':": ["girl_cray.gif"],
        "(;": ["girl_wink.gif"],
        ":-*": ["kiss.gif"],
        ":laugh:": ["laugh.gif"],
        ":ohmy:": ["ohmy.gif"],
        ":-(": ["sad.gif"],
        "8-)": ["rolleyes.gif"],
        ":-)": ["smile.gif"],
        ":smile:": ["smile.gif"],
        ":-P": ["tongue.gif"],
        ";-)": ["wink.gif"]
    }, l, d = {
            b: "http://s.4pda.to/img/emot/",
            a: 0,
            d: function (c, a) {
                l = {};
                a = [];
                for (c in h) {
                    c += "";
                    var b = d.c(c);
                    l[b] = c;
                    ":" != b.substr(0, 1) || ":" != b.substr(-1) ?
                        a.push("(?:^| |\\n|\\r)" + b.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&") + "(?: |\\n|\\r|$)") : a.push(b.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"))
                }
                a.sort(function (b, a) {
                    return (b + "").length > (a + "").length ? -1 : 1
                });
                d.a = RegExp(a.join("|"), "g")
            },
            e: function (c, a) {
                c.className = c.className.replace(RegExp("(^|\\s+)" + a + "(\\s+|$)", "g"), "$1")
            },
            c: function (c) {
                return c.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;")
            },
            getEmots: function () {
                function c(a) {
                    var b, d;
                    if (null ===
                        a || "object" !== typeof a) return a;
                    b = a.constructor();
                    for (d in a) b[d] = c(a[d]);
                    return b
                }
                return c(h)
            },
            parse: function (c, a) {
                if (!a && c) a = c;
                else if (a && c) d.b = c;
                else return;
                var b = a.firstChild,
                    e = k,
                    f = d.a,
                    g, m;
                for (f || d.d(); b;) {
                    if (3 !== b.nodeType) - 1 == (" " + b.className + " ").replace(/[\n\t]/g, " ").toLowerCase().indexOf(" noemoticons ") ? d.parse(b) : d.e(b, "noemoticons");
                    else if (g = d.c(b.nodeValue)) g = g.replace(d.a, function (b, a, c) {
                        c = b.replace(/^\s+|\s+$/g, "");
                        a = h[c];
                        return a || (a = h[l[c]], a) ? ' <img alt="' + c + '" title="' + c + '" src="' +
                            d.b + a[0] + '" /> ' : b
                    }), g != d.c(b.nodeValue) && (m = n.createElement("span"), m.innerHTML = g, e = b.nextSibling, b.parentNode.replaceChild(m, b));
                    b = b.nextSibling || e;
                    e = k
                }
                f || (d.a = 0)
            },
            parseAll: function (c,path) {
				d.b = path;
                var a = n.querySelectorAll(".emoticons"),
                    b = a.length,
                    e = 0,
                    f = d.a;
				
                f || d.d();
                c != k && (d.b = c + "");
                for (; e < b; e++) d.parse(a[e]), d.e(a[e], "emoticons");
                f || (d.a = 0)
            }
        };
	
    p.jsEmoticons = d
})(window, document);