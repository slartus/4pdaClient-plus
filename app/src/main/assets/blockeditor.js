function customScript(file) {
    var url = document.getElementsByTagName('link')[0].href;
    url = url.substring(0, url.length - 4);
    var script = document.createElement('SCRIPT');
    script.type = 'text/javascript';
    script.src = url + '/' + file;
    script.async = false;
    document.body.appendChild(script);
}
window.onload = function () {
    customScript('script.js');
}

var theSelection = false;

var clientPC = navigator.userAgent.toLowerCase();
var clientVer = parseInt(navigator.appVersion);

var is_ie = ((clientPC.indexOf("msie") != -1) && (clientPC.indexOf("opera") == -1));
var is_win = ((clientPC.indexOf("win") != -1) || (clientPC.indexOf("16bit") != -1));

bbcode = new Array();
bbtags = new Array(
    '[B]', '[/B]',
    '[I]', '[/I]',
    '[U]', '[/U]',
    '[S]', '[/S]',
    '[SUB]', '[/SUB]',
    '[SUP]', '[/SUP]',
    '[LEFT]', '[/LEFT]',
    '[CENTER]', '[/CENTER]',
    '[RIGHT]', '[/RIGHT]',
    '[QUOTE]', '[/QUOTE]',
    '[OFFTOP]', '[/OFFTOP]',
    '[CODE]', '[/CODE]',
    '[SPOILER]', '[/SPOILER]',
    '[HIDE]', '[/HIDE]'
);

function getarraysize(thearray) {
    for (i = 0; i < thearray.length; i++) {
        if ((thearray[i] == "undefined") || (thearray[i] == "") || (thearray[i] == null)) return i;
    }
    return thearray.length;
}

function arraypush(thearray, value) {
    thearray[ getarraysize(thearray) ] = value;
}

function arraypop(thearray) {
    thearraysize = getarraysize(thearray);
    retval = thearray[thearraysize - 1];
    delete thearray[thearraysize - 1];
    return retval;
}

function bbplace(text) {
    var txtarea = document.f1.Post;
    var scrollTop = (typeof(txtarea.scrollTop) == 'number' ? txtarea.scrollTop : -1);
    if (txtarea.createTextRange && txtarea.caretPos) {
        var caretPos = txtarea.caretPos;
        caretPos.text = caretPos.text.charAt(caretPos.text.length - 1) == ' ' ? caretPos.text + text + ' ' : caretPos.text + text;
        txtarea.focus();
    }
    else if (txtarea.selectionStart || txtarea.selectionStart == '0') {
        var startPos = txtarea.selectionStart;
        var endPos = txtarea.selectionEnd;
        txtarea.value = txtarea.value.substring(0, startPos) + text + txtarea.value.substring(endPos, txtarea.value.length);
        txtarea.focus();
        txtarea.selectionStart = startPos + text.length;
        txtarea.selectionEnd = startPos + text.length;
    }
    else {
        txtarea.value += text;
        txtarea.focus();
    }
    if (scrollTop >= 0) {
        txtarea.scrollTop = scrollTop;
    }
}

function bbstyle(bbnumber) {
    var txtarea = document.f1.Post;
    txtarea.focus();
    donotinsert = false;
    theSelection = false;
    bblast = 0;
    if (bbnumber == -1) { //Закрыть все теи
        while (bbcode[0]) {
            butnumber = arraypop(bbcode) - 1;
            txtarea.value += bbtags[butnumber + 1];
        }
        txtarea.focus();
        return;
    }
    if ((clientVer >= 4) && is_ie && is_win) {
        theSelection = document.selection.createRange().text; //Получить выделение для IE
        if (theSelection) { //Добавить теги вокруг непустого выделения
            document.selection.createRange().text = bbtags[bbnumber] + theSelection + bbtags[bbnumber + 1];
            txtarea.focus();
            theSelection = '';
            return;
        }
    }
    else if (txtarea.selectionEnd && (txtarea.selectionEnd - txtarea.selectionStart > 0)) {
        //Получить выделение для Mozilla
        mozWrap(txtarea, bbtags[bbnumber], bbtags[bbnumber + 1]);
        return;
    }
    for (i = 0; i < bbcode.length; i++) {
        if (bbcode[i] == bbnumber + 1 ) {
            bblast = i;
            donotinsert = true;
        }
    }
    if (donotinsert) {

        while (bbcode[bblast]) {
            butnumber = arraypop(bbcode) - 1;
            bbplace(bbtags[butnumber + 1]);
        }
        txtarea.focus();

    }
    else { //Открыть тег
        bbplace(bbtags[bbnumber]);
        arraypush(bbcode, bbnumber + 1);
        txtarea.focus();
    }
}

function mozWrap(txtarea, open, close) {
    if (txtarea.selectionEnd > txtarea.value.length) {
        txtarea.selectionEnd = txtarea.value.length;
    }
    var oldPos = txtarea.scrollTop;
    var oldHght = txtarea.scrollHeight;
    var selStart = txtarea.selectionStart;
    var selEnd = txtarea.selectionEnd + open.length;
    txtarea.value = txtarea.value.slice(0, selStart) + open + txtarea.value.slice(selStart);
    txtarea.value = txtarea.value.slice(0, selEnd) + close + txtarea.value.slice(selEnd);
    txtarea.selectionStart = selStart + open.length;
    txtarea.selectionEnd = selEnd;
    var newHght = txtarea.scrollHeight - oldHght;
    txtarea.scrollTop = oldPos + newHght;
    txtarea.focus();
}

function storeCaret(textEl) { //Вставка в позицию каретки - патч для IE
    if (textEl.createTextRange) textEl.caretPos = document.selection.createRange().duplicate();
    document.getElementById('helpbox').innerHTML = "Всего: " + document.f1.Post.value.length;
}
