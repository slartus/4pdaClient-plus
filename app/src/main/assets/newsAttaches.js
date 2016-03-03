function getNewsAttaches() {
    var jsonArr = [];
    var anchorList = document.querySelectorAll(".content-box a[data-lightbox]:not([data-notrack])");

    var obj = [];
    for (var i = 0; i < anchorList.length; i++) {
        obj.push(anchorList[i].getAttribute('href'));
    }
    jsonArr.push(obj);
    obj = [];
    anchorList = document.querySelectorAll(".content-box .sc-content");

    for (var i = 0; i < anchorList.length; i++) {
        var post = anchorList[i];
        var attachList = post.querySelectorAll("a[data-lightbox]");
        var obj = [];
        for (var j = 0, count = 0; j < attachList.length; j++) {
            var att = attachList[j].getAttribute('href');
            obj.push(att);
        }
        if (!obj[0]) continue;
        jsonArr.push(obj);
    }
    return jsonArr;
}
HTMLOUT.sendNewsAttaches(JSON.stringify(getNewsAttaches()));