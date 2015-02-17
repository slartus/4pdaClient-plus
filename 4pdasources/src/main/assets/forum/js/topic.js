
function goToAnchor(a,d,e,y,o){
    try{
        d = document;
        o = 'offsetTop';
        e = d.getElementById(a) || d.getElementsByName(a);
        e = ('length' in e ? (e.length? e[0] : null) :e);
        if(e == null)
            return;
        y = e[o]||0;
        while(e && (e = e.offsetParent) != null){
            if(o in e)
                y += e[o];
            else
                break;
        }
        window.scrollTo(0,y);
    }catch(e){
        alert(e);
    }
};

