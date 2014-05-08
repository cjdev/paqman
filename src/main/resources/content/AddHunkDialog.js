define(["util", "jqueryui"], function(util, jqueryui){
    return function (qual, onSave){
        var dialogView, createButton, kindSelect, nameField, urlField, urlBand, contentSelect,
        videoButton, htmlButton;
    
        dialogView = util.getTemplate("/AddHunkDialog.html").dialog({modal:true, title:"Add Hunk", width:600});
        createButton = dialogView.find(".create-button");
        kindSelect = dialogView.find(".kind-select");
        nameField = dialogView.find(".name-field");
        urlField = dialogView.find(".url-field");
        urlBand = dialogView.find(".url-band");
        contentSelect = dialogView.find(".content-select");
        videoButton = dialogView.find(".use-video-button");
        htmlButton = dialogView.find(".use-html-button");
    
        console.log("Add hunk");
    
        urlBand.hide();
    
        function handleContentSelect(){
            urlBand.toggle(videoButton.is(":checked"));
        }
    
        videoButton.change(handleContentSelect);
        htmlButton.change(handleContentSelect);
    
        kindSelect.change(function(){
            var kind = kindSelect.val();
            console.log(kind);
            var isInfo = kind === "video";
            dialogView.find(".content-select-band").toggle(isInfo);
            urlBand.toggle(isInfo);
            handleContentSelect();
        });
    
        createButton.click(function(){
            var ref = "";
    
            if(videoButton.is(":checked")){
                ref = urlField.val();
            }
    
            var url = "/api/quals/" + qual.id + "/hunks";
            var data = {
                    "description": "",
                    "kind": kindSelect.val(),
                    "name": nameField.val(),
                    "url": ref
            };
    
            $.post(url, JSON.stringify(data), function(){
                dialogView.dialog("close");
                onSave();
                dialogView.remove();
            });
        });
    };
});