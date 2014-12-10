define(["util", "AddHunkDialog", "HunkScreen"], function(util, AddHunkDialog, HunkScreen){
    return function(ref, userInfo, sessionCookie, where){
        var view = util.getTemplate("/QualScreen.html");
        var mainSection = view.find(".content-body"),
            editNameLink = view.find(".edit-name-link"),
            qual = util.getJson(ref),
            qualEditingControls = view.find(".qual-editing-controls"),
            addHunkButton = view.find(".add-hunk-button"),
            qualName = view.find(".qual-name"),
            qualDescription = view.find(".qual-description span"),
            userIsQualAdministrator = (qual.administrator === userInfo.email);
    
        var content = view.find(".content");
    
        console.log("There is " + content.length + " contents and " + where.length + " wheres");
    
        addHunkButton.click(function(){
            AddHunkDialog(qual, function(){
                userInfo = util.getSessionInfo(sessionCookie);
                refreshHunksList();
            });
        });
        
        function inputKeyUp(e) {
            e.which = e.which || e.keyCode;
            if(e.which == 13) {
                // submit
            }
        }
        
        if(userIsQualAdministrator){
            var qualAdminLabel = view.find(".qual-admin");
            qualAdminLabel.text(qual.administrator);
            util.makeEditable(qualAdminLabel, view.find(".edit-admin-control"), function(newEmail){
                if(qual.administrator !== newEmail){
                    qual.administrator = newEmail;
                    
                    $.ajax({
                        type : "PUT",
                        url : ref,
                        async : false,
                        data:JSON.stringify(qual),
                        success : function(data) {
                            window.location.reload();
                        },
                        error:function(xhr, textStatus, errorThrown ){
                            alert(xhr.responseText);
                        }
                    });
                    
                }
            });
            
            util.makeEditable(qualName, view.find(".edit-name-control"), function(newName){
                qual.name = newName;
                util.putJson(ref, qual);
                window.location = "/" + newName;
            });
            util.makeEditable(qualDescription, view.find(".edit-description-control"), function(newDescription){
                qual.description = newDescription;
                util.putJson(ref, qual);
            });
        }
        
        qualEditingControls.toggle(userIsQualAdministrator);
    
        view.find(".qual-title").text(qual.name);
        $(".user-details-area").fadeIn();
        
        qualName.text(qual.name);
        qualDescription.text(qual.description);
        
        content.html('');
        var hunksList = view.find(".qualification-hunks").find("ol");
    
        function showHunk(hunk){
    
            content.empty();
            mainSection.empty();
            HunkScreen(qual, hunk, userInfo, handleQualDeletion	, content);
            content.show();
            hunksList.find("li").removeClass("selected");
            view.find('li.' + hunk.hunkId).addClass("selected");
        }
        function handleQualDeletion(){
            content.empty();
            refreshHunksList();
        }
        function refreshHunksList(){
            qual = util.getJson(ref);
            hunksList.empty();
            $.each(qual.hunks, function(idx, hunk){
                var entry = $('<li class="' + hunk.hunkId + '" ><a href="#' + hunk.name + '">' + util.labels[hunk.kind] + ': ' + hunk.name + '</a></li>');
                if(util.userHasMetChallenge(userInfo, hunk.hunkId)){
                    entry.addClass("passed-hunk");
                }
                entry.click(function(){
                    showHunk(hunk);
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
    };
});