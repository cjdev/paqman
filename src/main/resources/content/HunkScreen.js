function HunkScreen(qual, hunk, userInfo, onDelete, where){
	var view = getTemplate("/HunkScreen.html");
	
	function showViewer(){
		console.log("showing");
		view.empty();
		HunkViewer(qual, hunk, userInfo, showEditor, view);
		view.show();
		view.css("display", "block");
	}
	
	function showEditor(){
		view.empty();
		HunkEditor(qual, hunk, view, showViewer, onDelete);
		console.log("yay!");
	}

	showViewer();
	
	view.appendTo(where);
	view.show();
}