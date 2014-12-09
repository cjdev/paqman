define([], function(){
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
        var c_value = escape(value) + ((exdays === null) ? "" : "; expires=" + exdate.toUTCString());
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
        });
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
        });
        return result;
    }
    function putJson(ref, data){
        var result;
        $.ajax({
            type : "PUT",
            url : ref,
            async : false,
            data:JSON.stringify(data),
            success : function(data) {
                result = data;
            }
        });
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
    
    
    function makeEditable(item, editLink, onchange){
        function hasFocus(){
            return item.get(0) === document.activeElement;
        }
        function takeFocusAway(){
            $(':focus').blur();//$('<div contenteditable="true"></div>').appendTo('body').focus().remove()
        }
        
        function stopEditing(){
            editLink.hide();
            takeFocusAway();
            item.removeAttr("contenteditable");
            takeFocusAway();
            
            onchange(item.text());
        }
        
        function handler(e){
            var which = e.which || e.keyCode;
            if(which == 13) {
                e.preventDefault();
                stopEditing();
            }
        }
        
        
        item.on('keypress',handler);
        item.mouseover(function(){
            item.attr("contenteditable", "true");
        });
        item.mouseout(function(){
            if(!hasFocus()){
                item.removeAttr("contenteditable"); // this removes the auto-correct squigglies in mozilla when you mouseover after editing once already
            }
        });
        item.click(function(){
            editLink.show();
        });
        editLink.text('done');
        editLink.hide();
        editLink.click(_.compose(stopEditing, takeFocusAway));
        item.blur(stopEditing);
	}

    
    return {
        getCookie:getCookie,
        slideLeftHide:slideLeftHide,
        setCookie:setCookie,
        getTemplate:getTemplate,
        getText:getText,
        getJson:getJson,
        putJson:putJson,
        listQuals:listQuals,
        getSessionInfo:getSessionInfo,
        userHasMetChallenge:userHasMetChallenge,
        userHasQualification:userHasQualification,
        labels:labels,
        makeEditable:makeEditable
    };
});