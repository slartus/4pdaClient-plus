function scrollPageQMS() {
    try {
        if (document.body.id == "qms"){
            window.scrollTo(0, document.body.scrollHeight);
        }else if(document.body.id == "qms_more"){
            var elements = document.getElementsByClassName("date");
            if (elements.length > 7){
                var el = elements[Math.max(elements.length % 7,7)];
                var x = 0;
                var y = 0;
                while (el != null) {
                    x += el.offsetLeft;
                    y += el.offsetTop;
                    el = el.parent;
                }
                window.scrollTo(0, y);
            }
        }
	}catch(err){
	    console.error(err);
	}
}
window.addEventListener('load', scrollPageQMS);


/**
 *		==================
 *		QMS SELECT MESSAGE
 *		==================
 */

document.addEventListener("click", checkedQmsMessage);

function checkedQmsMessage() {
	var target = event.target;
	var messForDeleteCount = 0;
	while (target != this) {
		if (target.nodeName == 'A') return;
		if (target.classList.contains('list-group-item')) {
			var checkbox = target.getElementsByTagName('input')[0];
			if (checkbox.checked) {
				checkbox.checked = false;
				target.classList.remove('selected');
				messForDeleteCount--;
			} else {
				checkbox.checked = true;
				target.classList.add('selected');
				messForDeleteCount++;
			}
			if (messForDeleteCount > 0) {
				HTMLOUT.startDeleteModeJs(messForDeleteCount);
			} else {
				HTMLOUT.stopDeleteModeJs();
			}
			return;
		}
		target = target.parentNode;
	}
}


function deleteMessages(formId) {
	try {
		//var f=elem(formId);
		var checkboxes = document.body.getElementsByTagName('input');

		var checkboxesChecked = [];
		// loop over them all
		for (var i = 0; i < checkboxes.length; i++) {
			// And stick the checked ones onto an array...
			if (checkboxes[i].checked) {
				checkboxesChecked.push(checkboxes[i].name);
			}
		}

		// Return the array if it is non-empty, or null
		window.HTMLOUT.deleteMessages(checkboxesChecked.length > 0 ? checkboxesChecked : null);
	} catch (err) {
		window.HTMLOUT.showMessage(err.toString());
	}
}
