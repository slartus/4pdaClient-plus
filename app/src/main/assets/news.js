
function kek(postId, logined) {
	window.onload = function() {
		var anchors = document.querySelectorAll('.karma');
		var data = JSON.parse(getCommentsData())[postId];
		console.log("DATA "+data+"\n"+ JSON.parse(getCommentsData())+"\n"+postId);
		for (var i = 0; i < anchors.length; i++) {
			var found = anchors[i].getAttribute("data-karma").match(/([\d]*)-([\d]*)/);
			console.log(found);
			anchors[i].innerHTML = '<b class="icon-karma-up" title="Мне нравится" data-karma-act="1-264127-2745153"></b><span class="num-wrap"><span class="num" title="Понравилось"></span></span>';
			anchors[i].querySelector(".num-wrap .num").innerHTML = data[found[2]][3];
			if (logined) {
				anchors[i].onclick = function() {
					found = this.getAttribute("data-karma").match(/([\d]*)-([\d]*)/);
					this.querySelector(".num-wrap .num").innerHTML = data[found[2]][3] + 1;
					HTMLOUT.likeComment(found[1], found[2]);
				};
			}
		}
	};
}