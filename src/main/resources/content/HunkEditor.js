define(["util"], function(util){
    return function(qual, hunk, where, onSave, onDelete){
        var dialogView = util.getTemplate("/HunkEditor.html"),
        nameTextField = dialogView.find(".name-field"),
        contentField = dialogView.find(".content-field"),
        tabs = dialogView.find(".hunk-editor-tabs"),
        saveButton = dialogView.find(".save-button"),
        deleteButton = dialogView.find(".delete-button"),
        url = "/api/quals/" + qual.id + "/hunks/" + hunk.id;
    
        tabs.show();
    
        try{
            nameTextField.val(hunk.name);
            contentField.val(hunk.description);
        }catch(e){
    
        }
    
        deleteButton.click(function(){
            if(confirm("Are you sure you want to delete this?")){
                $.ajax(url, {
                    type:"DELETE",
                    success:function(){
                        onDelete();
                    }
                });
            }
        });
    
        saveButton.click(function(){
    
            hunk.name = nameTextField.val();
            hunk.description = contentField.val();
    
            $.ajax(url, {
                type:"PUT",
                data:JSON.stringify(hunk),
                success:function(d){
                    onSave(d);
                }
            });
        });
    
        dialogView.show();
        dialogView.appendTo(where);
    };
});