define(["jquery", "util", "underscore", "QualScreen"], 
      function($, util,   _,             QualScreen) {

    var sessionCookie = util.getCookie("SessionId");

    var name = "paqman";
    var subtext = "capability aquisition and maintainance tool";

    $(".title").text(name);
    $(".subtext").text(subtext);

    function showUI(doShow, userInfo, sessionCookie) {
        $(".qualification-hunks, .content, .scoreboard, .quals-list-holder").toggle(doShow);
        $(".login").toggle(!doShow);

        if(userInfo){

            var userDetailsArea = $(".user-details-area");
            $(".userid").text(userInfo.email);

            var name = decodeURIComponent(window.location.pathname.substring(1));
            var qual = _.find(util.listQuals(), function(a){return a.name == name;});
            console.log("qual: ", qual);

            var contentHolder = $(".content-holder");
            var qualsListDiv = $(".quals-list-holder");
            var qualsList = $(".quals-list");
            qualsList.empty();

            console.log("Whatever: " + qual.name);
            $(".subtext").hide();
            $(".title").text(qual.name);
            var screen = QualScreen(qual.ref, userInfo, sessionCookie, contentHolder);

            var hash = window.location.hash;
            if(hash.length>0){
                var hunkName = hash.substring(1);
                screen.showHunk(hunkName);
            }
        }
    }

    var userInfo = util.getSessionInfo(sessionCookie);
    if (sessionCookie && userInfo) {
        showUI(true, userInfo, sessionCookie);
    } else {
        showUI(false);
    }

    $(".logout").click(function(){
        setCookie("SessionId", "");
        window.location = "/";
    });

    var loginDiv = $(".login");
    loginDiv.find("button").click(function() {
        var loginErrorsText = loginDiv.find(".errors");
        var emailField = loginDiv.find(".email");
        var passwordField = loginDiv.find(".password");

        loginErrorsText.empty();

        var request = {
                email : emailField.val(),
                password : passwordField.val()
        };

        console.log(request);

        $.ajax({
            type : "POST",
            url : "/api/sessions",
            data : JSON.stringify(request),
            success:function(msg) {
                var token = msg.token;
                setCookie("SessionId", token);                

                passwordField.val("");
                showUI(true, util.getSessionInfo(token), token);
            },
            error:function(jqXHR, textStatus, errorThrown){
                loginErrorsText.append("Invalid Credentials");
            }
        });
    });
});
