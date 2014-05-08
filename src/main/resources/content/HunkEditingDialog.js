define(["jquery", "text!HunkEditingDialog.html"], function($, template){
    return function(successHandler){
        var view = $(template);
        view.dialog({
            title:"",
            modal:true,
            width:400
        });
        
        view.find("button").click(function(){
            view.dialog("close");
            var radioButton = view.find('input[value="significant"]');
            
            var isSignificant = radioButton.is(":checked");
            
            successHandler(isSignificant);
        });
    };
});