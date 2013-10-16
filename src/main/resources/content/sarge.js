
function AddQualificationDialog(userInfo, onSuccess){
	var view, nameField, createButton, descriptionField;
	
	view = getTemplate("/AddQualificationDialog.html").appendTo($("body"));
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
	
    var sessionCookie = getCookie("SessionId");

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
            
            $(".add-qual-button").click(function(){
            	AddQualificationDialog(userInfo, showQualsList);
            });
            
			function showQualsList(){
				var qualsListDiv, qualsList, contentHolder;

				contentHolder = $(".content-holder");
				qualsListDiv = $(".quals-list-holder"), 
				qualsList = $(".quals-list");
				qualsList.empty();
			    
				$.each(listQuals(), function(idx, qual){
				    var entry = $('<li>' + 
				    		           '<a class="qual-title" href="/' + qual.name + '">' + qual.name + '</a><span class="qual-description">' + qual.description + 
				    		           '<span class="qual-status-links">[<a class="more-link" href="#">more</a>]</span>' +
				    		           '<div class="users-list" style="display:none;border-top:2px solid grey;padding-top:5px;margin-top:5px;">People:</div>' + 
				    		      '</li>');
				    
				    var qualStatusLinks = entry.find(".qual-status-links");
				    var moreLink = entry.find(".more-link");
				    
				    moreLink.click(function(){
				    	
				    	$.get("/api/quals/" + qual.id + "/people", function(people){
				    		var list = entry.find(".users-list");
					    	$.each(people, function(idx, person){
					    		var status = person.isCurrent ? "current" : "lapsed";
					    		list.append('<div class="user-status-list-entry">' + person.email + ' | ' + status + '</div>');
					    	});
					    	list.slideDown(function(){
					    		qualStatusLinks.slideUp();
					    	});
				    	});
				    	
				    	
				    });
				    
				    contentHolder.empty();
				    
				    qualsList.append(entry);
				    
				    if(userHasQualification(userInfo, qual)){
			    	   entry.append('<img src="/gold-star.jpg" class="star-emblem"/>');
			       }
				});
				
			    $(".qualification-hunks, .content").toggle(false);
			    qualsListDiv.slideDown();
			}
			
			showQualsList();
        }
    }

    var userInfo = getSessionInfo(sessionCookie);
    if (sessionCookie && userInfo) {
        showUI(true, userInfo, sessionCookie);
    } else {
        showUI(false);
    }
	
    $(".logout").click(function(){
        setCookie("SessionId", "");
        showUI(false);
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
        }

        console.log(request);

        $.ajax({
            type : "POST",
            url : "/api/sessions",
            data : JSON.stringify(request),
            success:function(msg) {
	            var token = msg.token;
	            setCookie("SessionId", token);                
	
	            passwordField.val("");
	            showUI(true, getSessionInfo(token), token);
            },
            error:function(jqXHR, textStatus, errorThrown){
            	loginErrorsText.append("Invalid Credentials");
            }
        });
    });
});
