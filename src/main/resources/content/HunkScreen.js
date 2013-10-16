function HunkScreen(qual, hunk, userInfo, onDelete, where){
	var view = getTemplate("/HunkScreen.html");
	
	function refreshData(){
		var url = "/api/quals/" + qual.id + "/hunks/" + hunk.id;
		$.ajax(url, {
			   type:"get",
			   async:false,
			   success:function(data){
				   console.log("type:" + (typeof data));
			   }
		   });
	}
	
	function showViewer(h){
		hunk = h
		refreshData();
		console.log("showing");
		view.empty();
		HunkViewer(qual, hunk, userInfo, showEditor, view);
		view.show();
		view.css("display", "block");
	}
	
	function showEditor(){
		refreshData();
		view.empty();
		HunkEditor(qual, hunk, view, showViewer, onDelete);
		console.log("yay!");
	}

	showViewer(hunk);
	
	view.appendTo(where);
	view.show();
}