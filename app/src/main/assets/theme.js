function getIds() {
    var p = document.documentElement ? document.documentElement : document.body;
    var c = p.getElementsByTagName('input');
    var result = [];
    for (i = 0; i < c.length; ++i){
        if ('checkbox' == c[i].type){
            if(c[i].checked){
                result.push(c[i].getAttribute('value'));
            }
        }
    }
    HTMLOUT.showCuratorDialog(result.join())
}
function invertCheckboxes() {
    var p = document.documentElement ? document.documentElement : document.body;
    var c = p.getElementsByTagName('input');
    for (i = 0; i < c.length; ++i)
        if ('checkbox' == c[i].type) c[i].checked = !c[i].checked;
}
function invertCheckboxes() {
    var p = document.documentElement ? document.documentElement : document.body;
    var c = p.getElementsByTagName('input');
    for (i = 0; i < c.length; ++i)
        if ('checkbox' == c[i].type) c[i].checked = !c[i].checked;
}
//Уникальная переменная
var checkedCheckboxInCheckboxesPostCurator = false;
function setCheckedAll() {
    var p = document.documentElement ? document.documentElement : document.body;
    var c = p.getElementsByTagName('input');
    for (i = 0; i < c.length; ++i){
        if ('checkbox' == c[i].type){
            if(checkedCheckboxInCheckboxesPostCurator)
                c[i].checked = true;
            else
                c[i].checked = false;
        }
    }
    checkedCheckboxInCheckboxesPostCurator = !checkedCheckboxInCheckboxesPostCurator;
}



function copySelection(){
    var selObj = window.getSelection();
    var selectedText = selObj.toString();
    if(selectedText!=null&&selectedText!='')
        alert(selectedText);
}

function deleteMessages(formId){
    try{
        var f=elem(formId);
        var checkboxes = f.getElementsByTagName('input');

        var checkboxesChecked = [];
         // loop over them all
        for (var i=0; i<checkboxes.length; i++) {
            // And stick the checked ones onto an array...
            if (checkboxes[i].checked) {
               checkboxesChecked.push(checkboxes[i].name);
            }
        }

         // Return the array if it is non-empty, or null
        window.HTMLOUT.deleteMessages(checkboxesChecked.length > 0 ? checkboxesChecked : null);
    }catch(err){
        window.HTMLOUT.showMessage(err.toString());
    }
}

function toggleSpoilerVisibility(e){var t=e.parentNode.parentNode.getElementsByTagName("div")[1];"none"==t.style.display?(t.style.display="",e.parentNode.setAttribute("class","hidetop open")):(t.style.display="none",e.parentNode.setAttribute("class","hidetop close"))};

function openHat(e){var t=e.parentNode.getElementsByTagName("div")[1];"none"==t.style.display?(t.style.display="",e.setAttribute("class","hidetop open")):(t.style.display="none",e.setAttribute("class","hidetop close"))};

function changeStyle(cssFile) {
    var newlink = document.createElement("link");
    newlink.setAttribute("rel", "stylesheet");
    newlink.setAttribute("type", "text/css");
    newlink.setAttribute("href", cssFile);
    document.getElementsByTagName("head").item(0).replaceChild(newlink, document.getElementsByTagName("link").item(0));
}

function scrollToElement(id) {

    var el = elemByName(id);
    var x = 0;
    var y = 0;
    while (el != null) {
        x += el.offsetLeft;
        y += el.offsetTop;
        el = el.parent;
    }
    window.scrollTo(0, y);

};

function areaPlus(){
   var textarea_obj = elem ( "Post" );
   textarea_obj.rows+=2;
}

function areaMinus(){
   var textarea_obj = elem ( "Post" );
   if(textarea_obj.rows<=8)return;
   textarea_obj.rows-=2;
}

function getSctollPosition(id){
    var elem = document.getElementById(id);
    var x = 0;
    var y = 0;

    while (elem != null) {
        x += elem.offsetLeft;
        y += elem.offsetTop;
        elem = elem.parent;
    }
    window.HTMLOUT.getSctollPosition(y);
}

function getPostBody(){
   var textarea_obj = elem ( "Post" );

   window.HTMLOUT.setPostBody(textarea_obj.value);

};

function clearPostBody(){
   var textarea_obj = elem ( "Post" );

   textarea_obj.value=null;

};

function preparePost(){
   var textarea_obj = elem ( "Post" );

   window.HTMLOUT.post(textarea_obj.value);

};

function advPost(){
   var textarea_obj = elem ( "Post" );

   window.HTMLOUT.advPost(textarea_obj.value);

};

function insertText(text)
{
try{
     window.HTMLOUT.insertTextToPost(text);
}catch(err){

}

//	var textarea_obj = elem ( "Post" );
//	textarea_obj.value+=text;
//	return false;
}

function getDivInnerText(msgId){
	var textarea_obj = elem ( msgId );
	return textarea_obj.innerText;

}

function postQuote(postId, date, userNick){
	var textarea_obj = elem ("msg" + postId );
	var text=textarea_obj.innerText;
	return insertText("[quote name='" + userNick + "' date='" + date + "' post='" + postId + "']\n" + text + "\n[/quote]" );
}


function elem ( id )
{
    if ( isdef ( typeof ( document.getElementById ) ) ) return document.getElementById(id);
    else if ( isdef ( typeof ( document.all ) ) ) return document.all [ id ];
    else if ( isdef ( typeof ( document.layers ) ) ) return document [ id ];
    else return null;
}


function elemByName ( name )
{
	if ( isdef ( typeof ( document.getElementsByName ) ) ) return document.getElementsByName(name)[0];
	else if ( isdef ( typeof ( document.all ) ) ) return document.all [ name ];
	else if ( isdef ( typeof ( document.layers ) ) ) return document [ name ];
	else return null;
}

function isdef ( typestr )
{
	return ( ( typestr != "undefined" ) && ( typestr != "unknown" ) ) ? true : false;
}
