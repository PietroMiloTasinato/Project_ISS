const ws = new WebSocket("ws://"+ window.location.host +"/webgui");
const pushButton = document.getElementById("push_button")
pushButton.addEventListener('click', () => {
	ws.send("request_to_load")
})
const requestStatusSpan = document.getElementById("request-state")
const slotAssignedSpan = document.getElementById("slot-assigned")
ws.onopen = () => {
	console.log("Web Socket opened")
}
ws.onmessage = (m) => {
	try {
		const message = JSON.parse(m.data)
		slotAssignedSpan.innerText = message.slot
		requestStatusSpan.innerText = message.state
		console.log(m.data)
	} catch(e){
		console.error(e)
	}
}