
function getCookie(c_name) {
    var c_value = document.cookie;
    var c_start = c_value.indexOf(" " + c_name + "=");
    if (c_start == -1) {
        c_start = c_value.indexOf(c_name + "=");
    }
    if (c_start == -1) {
        c_value = null;
    } else {
        c_start = c_value.indexOf("=", c_start) + 1;
        var c_end = c_value.indexOf(";", c_start);
        if (c_end == -1) { 
            c_end = c_value.length;
        }
        c_value = unescape(c_value.substring(c_start, c_end));
    }
    return c_value;
}

function slideLeftHide(jquery) {
	jquery.animate({width: 'hide'});
}

function setCookie(c_name, value, exdays) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + exdays);
    var c_value = escape(value)
            + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
    document.cookie = c_name + "=" + c_value;
}

var templatesCache = {};

function getTemplate(ref){
	var template = templatesCache[ref];
	
	if(!template){
	  template = getText(ref);
	  templatesCache[ref] = template;
	}
	
    return $($.parseHTML(template));
}

function getText(ref){
    var result;
    $.ajax({
        type : "GET",
        url : ref,
        async : false,
        dataType:"text",
        success : function(data) {
            result = data;
        }
    })
    return result;
}

function getJson(ref){
    var result;
    $.ajax({
        type : "GET",
        url : ref,
        async : false,
        dataType:"json",
        success : function(data) {
            result = data;
        }
    })
    return result;
}
function listQuals(){
    return getJson("/api/quals");
}

function getSessionInfo(sessionId) {
    var result;
    $.ajax({
        type : "GET",
        url : "/api/sessions/" + sessionId,
        async : false,
        success : function(data) {
            result = data;
        }
    });
    return result;
}

var labels = {
        "video":"learn",
        "challenge":"Challenge"
};

function userHasMetChallenge(userInfo, theChallengeId){
	   var result = false;
	   $.each(userInfo.qualifications, function(idx, q){
		   $.each(q.challengesMet, function(idx, challengeId){
			   if(theChallengeId == challengeId){
				   result = true;
			   }
		   });
	   });
	   return result;
}
function userHasQualification(userInfo, qual){
	   var result = false;
	   var quals = userInfo.qualifications || [];
	   $.each(quals, function(idx, q){
		   if(q.id === qual.id && q.isQualified){
			   result = true;
		   }
	   });
	   return result;
}