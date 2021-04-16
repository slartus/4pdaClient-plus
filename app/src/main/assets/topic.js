
function getSelectionPostInfo() {
    var text = "", containerElement = null;

    if (typeof window.getSelection != "undefined") {
        console.log("window");
        var sel = window.getSelection();
        if (sel.rangeCount) {
            var node = sel.getRangeAt(0).commonAncestorContainer;
            containerElement = node.nodeType == 1 ? node : node.parentNode;
            text = sel.toString();
        }
    } else if (typeof document.selection != "undefined" &&
        document.selection.type != "Control") {
        console.log("document");
        var textRange = document.selection.createRange();
        containerElement = textRange.parentElement();
        text = textRange.text;
    }
    text = text || getSelectedText();
    if (containerElement) {
        var postElement = containerElement.closest(".post_container");
        if (postElement) {
            return {
                postId: postElement.getAttribute("post-id"),
                postDate: postElement.getAttribute("post-date"),
                userId: postElement.getAttribute("post-author-id"),
                userNick: postElement.getAttribute("post-author"),
                selection: text
            };
        }
    }
    return {
        selection: text
    };
}

function htmlOutSelectionPostInfo() {
    try {
        var selectionInfo = getSelectionPostInfo();
        if (selectionInfo) {
            window.HTMLOUT.selectionPostInfo(
                selectionInfo.postId,
                selectionInfo.postDate,
                selectionInfo.userId,
                selectionInfo.userNick,
                selectionInfo.selection);
        } else {
            window.HTMLOUT.selectionPostInfo(
                {
                    selection: getSelectedText()
                });
        }
    } catch (err) {
        console.error(err);
    }
}