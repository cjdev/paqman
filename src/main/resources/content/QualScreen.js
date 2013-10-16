function QualScreen(ref, userInfo, sessionCookie, where){
	var view = getTemplate("/QualScreen.html");
    var mainSection = view.find(".content-body"),
	    qual = getJson(ref),
	   	qualEditingControls = view.find(".qual-editing-controls"),
	   	addHunkButton = view.find(".add-hunk-button"),
	   	userIsQualAdministrator = (qual.administrator === userInfo.email);
   	
    var content = view.find(".content");
    
    console.log("There is " + content.length + " contents and " + where.length + " wheres");
    
    addHunkButton.click(function(){
    	AddHunkDialog(qual, function(){
 		   userInfo = getSessionInfo(sessionCookie);
 		   refreshHunksList();
    	});
    });
    
    qualEditingControls.toggle(userIsQualAdministrator);
    
    view.find(".qual-title").text(qual.name);
    content.text(qual.description);
    var hunksList = view.find(".qualification-hunks").find("ol");
    
    function showHunk(hunk){

		content.empty();
		mainSection.empty();
		HunkScreen(qual, hunk, userInfo, handleQualDeletion	, content);
		content.show();
		hunksList.find("li").removeClass("selected");
		entry.addClass("selected");
    }
    function handleQualDeletion(){
		content.empty();
		refreshHunksList();
   }
    function refreshHunksList(){
    	qual = getJson(ref);
	    hunksList.empty();
	    $.each(qual.hunks, function(idx, hunk){
	    	

		       var entry = $('<li><a href="#' + hunk.name + '">' + labels[hunk.kind] + ': ' + hunk.name + '</a></li>');
		       
		       
		       if(userHasMetChallenge(userInfo, hunk.id)){
		    	   entry.addClass("passed-hunk");
		       }
		       
		       
		       
		       entry.click(function(){
					content.empty();
					mainSection.empty();
					HunkScreen(qual, hunk, userInfo, handleQualDeletion	, content);
					content.show();
					hunksList.find("li").removeClass("selected");
					entry.addClass("selected");
		       });
		       hunksList.append(entry);
	    });
    }
    refreshHunksList();
    view.appendTo(where);
    view.slideDown();
    
    return {
    	showHunk:function(name){
    		var hunk = _.find(qual.hunks, function(h){return h.name==name;});
    		if(hunk){
    			showHunk(hunk);
    		}
    	}
    };
}