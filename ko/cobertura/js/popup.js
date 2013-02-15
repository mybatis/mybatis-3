var newwindow;
function popupwindow(url)
{
	newwindow=window.open(url,'name','height=500,width=500,resizable=yes,scrollbars=yes');
	if (window.focus) {
		newwindow.focus()
	}
}
