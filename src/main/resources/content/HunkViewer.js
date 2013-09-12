function HunkViewer(qual, hunk, userInfo, onEdit, where){
	var view = getTemplate('/HunkViewer.html'),
	    confirmationSection = view.find('.certification-section'),
	    challengeMetEmblem = view.find(".already-met-challenge-emblem"),
	    hunkEditingControls = view.find(".hunk-editing-controls"),
        contentTitle = view.find(".content-title"),
    	editHunkButton = view.find(".edit-hunk-button"),
	    userIsQualAdministrator = (qual.administrator === userInfo.email);

	view.find(".user-name").text(userInfo.email);
	
	   var canProctor = userIsQualAdministrator || userHasQualification(userInfo, qual);
    var contentHandlers = {
            "video":function(hunk){
            	console.log("Yo: " + hunk.url);
            	if(hunk.url && hunk.url.trim() !== ""){
            		return '<video controls="controls" height="240" src="' + hunk.url + '"/>';
            	}else{
            		return hunk.description;
            	}
            },
            "challenge":function(){
            	var proctorsText = "";
            	
            	if(!canProctor){
            		proctorsText = "<hr>Have one of the following people administer this challenge:<ul>";
            		$.each(qual.proctors, function(idx, proctor){
            			proctorsText += "<li>" + proctor + "</li>";
            		});
            		proctorsText += "</ul>";
            	}
            	
                return hunk.description + proctorsText;
            }
    };
	
	editHunkButton.click(onEdit);
	
	hunkEditingControls.toggle(userIsQualAdministrator);
	
	contentTitle.text(hunk.name);
	
	
	function showProctoringMessage(){
		   if(canProctor && hunk.kind === "challenge"){
	 	   (function(){
	 		   var confirmationMessage = confirmationSection.find('.certification-message'),
	 		       messagesDiv = view.find('.messages'),
	 		       confirmationScreen = view.find('.certification-band'),
	 		       passwordField = view.find('.password-field'),
	 		       challengerNameField = view.find('.challenger-name-field');
	 		   
	 		   passwordField.val("");
	 		   challengerNameField.val("");
		       confirmationScreen.hide();
	 		   messagesDiv.hide();
	 		   challengeMetEmblem.hide();
	 		   confirmationMessage.show();
	 		   confirmationSection.show();
	 		   console.log("Showed", confirmationSection);
	 		   confirmationSection.find('a').click(function(){
	 			   
				   confirmationMessage.hide();
				   
				   confirmationSection.slideUp();
				   confirmationScreen.slideDown();
				   confirmationScreen.find("button").click(function(){
					   var emailEntered = view.find(".my-email-field").val();
					   if(emailEntered !== userInfo.email){
						   alert("Bad Input :(");
					   }else{
						   confirmationScreen.hide(function(){
							   var challengerName = challengerNameField.val();
							   console.log("Challenger is " + challengerName + " (" + challengerNameField.length + ")");
							   var url = "/api/quals/" + qual.id + "/challenges/" + hunk.id + "/people";
							   $.post(url, JSON.stringify(challengerName), function(){
								   messagesDiv.empty().text("Action Successful!").show();
								   setTimeout(function(){
									   messagesDiv.fadeOut();
								   }, 5000);
							   });
						   });
					   }
					   
				   });
	 		   });
	 	   }());
	    }else{
	 	   challengeMetEmblem.toggle(userHasMetChallenge(userInfo, hunk.id));
	        confirmationSection.hide();
	    }
	}
	
	showProctoringMessage();
	
	var contentHandler = contentHandlers[hunk.kind];
	if(contentHandler){
		view.find(".content-body").append(contentHandler(hunk));
	}

    view.css("display", "block");
	view.appendTo(where);
	view.show();
	console.log("Showing at ", where);
}