function skipNprogress(attributes) {
	if(attributes.ep == null)
		return false;
	
	var arrayLength = attributes.ep.length;
	for (var i = 0; i < arrayLength; i++) {
		if(attributes.ep[i].name == "skipNProgress") {
			return true;
		}
	}
	return false;
}

var startProgress = function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
	if(!skipNprogress(attributes)) {
		NProgress.start();
	}
}
var endProgress = function(jqEvent, attributes, jqXHR, errorThrown, textStatus) {
	if(!skipNprogress(attributes)) {
		NProgress.done();
	}	
}
Wicket.Event.subscribe('/ajax/call/before', startProgress)
Wicket.Event.subscribe('/ajax/call/complete', endProgress)
