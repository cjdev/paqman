define(["jquery", "underscore", "jqueryui", "util"], function($, _, jqueryui, util){
    function AddQualificationDialog(userInfo, onSuccess){
        var view, nameField, createButton, descriptionField;

        view = util.getTemplate("/AddQualificationDialog.html").appendTo($("body"));
        nameField = view.find(".name-field");
        descriptionField = view.find(".description-field");
        createButton = view.find(".button");

        view.dialog({modal:true, title:"New Qualification Program", width:500});

        createButton.click(function(){
            var name = nameField.val(), description = descriptionField.val();

            function isNotBlank(v){return v && v.trim() !== "";}

            if(isNotBlank(name) && isNotBlank(description)){
                $.ajax({
                    type : "POST",
                    url : "/api/quals",
                    data : JSON.stringify({
                        name:name,
                        description:description,
                        administrator:userInfo.email
                    }),
                    success:function(){
                        view.dialog("close");
                        view.remove();
                        onSuccess();
                    }
                });
            }
        });
    }

    $(function() {

        var sessionCookie = util.getCookie("SessionId");

        var name = "paqman";
        var subtext = "capability aquisition and maintainance tool";

        $(".title").text(name);
        $(".subtext").text(subtext);

        function showUI(doShow, userInfo, sessionCookie) {
            $(".qualification-hunks, .content, .scoreboard, .quals-list-holder").toggle(doShow);
            $(".login").toggle(!doShow);

            function showQualsList(){
                var qualsListDiv, qualsList, contentHolder;

                contentHolder = $(".content-holder");
                qualsListDiv = $(".quals-list-holder");
                qualsList = $(".quals-list");
                qualsList.empty();

                $.each(util.listQuals(), function(idx, qual){
                    var entry = $('<li>' +
                            '<a class="qual-title" href="/' + qual.name + '">' + qual.name + '</a><span class="qual-description">' + qual.description +
                            '<span class="qual-status-links">[<a class="more-link" href="">certifications</a>]</span>' +
                            '<div class="users-list" style="display:none;border-top:2px solid grey;padding-top:5px;margin-top:5px;">People:</div>' +
                    '</li>');

                    var qualStatusLinks = entry.find(".qual-status-links");
                    var moreLink = entry.find(".more-link");

                    moreLink.click(function(){

                        $.get("/api/quals/" + qual.id + "/people", function(people){
                            var list = entry.find(".users-list");
                            $.each(people, function(idx, person){
                                var status;
                                var isCurrent = function(c){return c.isCurrent;};
                                var passedChallenges = _.filter(person.challenges, isCurrent);
                                console.log("negate is ", _.negate);
                                
                                var challengesYetToDo = _.filter(person.challenges, _.negate(isCurrent));
                                var statistics = passedChallenges.length + "/" + (passedChallenges.length + challengesYetToDo.length);
                                if(person.isAdministrator){
                                    status = "administrator";
                                } else if(person.isCurrent){
                                    status = "current";
                                }else if(person.wasCurrent){
                                    status = "lapsed (" + statistics + ")";
                                }else{
                                    status = "partial (" + statistics + ")";
                                }
                                
                                var userStatusListEntry = $('<div class="user-status-list-entry">' + person.email + ' | ' + status + '</div>');
                                if(!person.isAdministrator){
                                    var todo = $('<div class="challengeYetToDo"/>');
                                    $.each(passedChallenges, function(idx, challenge){
                                        todo.append('<div><div class="doneMarker"></div>' + challenge.name + "</div>");
                                    });
                                    $.each(challengesYetToDo, function(idx, challenge){
                                        todo.append('<div><div class="notDoneMarker"></div>' + challenge.name + "</div>");
                                    });
                                    todo.appendTo(userStatusListEntry);
                                    
                                }
                                userStatusListEntry.appendTo(list);
                            });
                            list.slideDown(function(){
                                qualStatusLinks.slideUp();
                            });
                        });

                        return false;
                    });

                    contentHolder.empty();

                    qualsList.append(entry);

                    if(util.userHasQualification(userInfo, qual)){
                        entry.append('<img src="/gold-star.png" class="star-emblem"/>');
                    }
                });

                $(".qualification-hunks, .content").toggle(false);
                qualsListDiv.slideDown();
            }

            if(userInfo){
                $(".userid").text(userInfo.email);

                $(".add-qual-button").click(function(){
                    AddQualificationDialog(userInfo, showQualsList);
                });

                showQualsList();
            }
        }

        var userInfo = util.getSessionInfo(sessionCookie);
        if (sessionCookie && userInfo) {
            showUI(true, userInfo, sessionCookie);
        } else {
            showUI(false);
        }

        $(".logout").click(function(){
            util.setCookie("SessionId", "");
            showUI(false);
        });

        var loginDiv = $(".login");
        var loginButton = loginDiv.find("button");

        loginButton.click(function() {
            var loginErrorsText = loginDiv.find(".errors");
            var emailField = loginDiv.find(".email");
            var passwordField = loginDiv.find(".password");

            loginErrorsText.empty();

            var request = {
                    email : emailField.val(),
                    password : passwordField.val()
            };

            $.ajax({
                type : "POST",
                url : "/api/sessions",
                data : JSON.stringify(request),
                success:function(msg) {
                    var token = msg.token;
                    util.setCookie("SessionId", token);

                    passwordField.val("");
                    showUI(true, util.getSessionInfo(token), token);
                },
                error:function(jqXHR, textStatus, errorThrown){
                    loginErrorsText.append("Invalid Credentials");
                }
            });
        });

        var clickLoginButton = function(event) {
            if(event.keyCode == 13){
                loginButton.click();
            }
        };
        loginDiv.find(".email").keyup(clickLoginButton);
        loginDiv.find(".password").keyup(clickLoginButton);
    });
});
