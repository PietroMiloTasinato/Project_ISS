let ws = new WebSocket("ws://"+ window.location.host +"/webgui");
let pushButton = document.getElementById("push_button")
pushButton.addEventListener('click', () => {
	ws.send("push_button")
})
ws.onopen = () => {
	console.log("Web Socket opened")
}