let utf8Encoder = new TextEncoder()
let utf8Decoder = new TextDecoder()
let ws = new WebSocket("ws://localhost:8025/test");

ws.binaryType = "arraybuffer"
ws.onopen = function() {
};
ws.onmessage = function(e) {
    let message = decodePayload(e.data)
    alert(message);
};
ws.onclose = function() {
    alert("closed");
};

class Message {
    constructor(type, data) {
        this.type = type
        this.data = data
    }
}

function encodePayload(payload) {
    return utf8Encoder.encode(JSON.stringify(payload))
}

function decodePayload(payload) {
    return utf8Decoder.decode(payload)
}

function createRoom() {
    let payload = encodePayload(new Message("CREATE_ROOM", null));
    ws.send(payload)
}

function joinRoom(id) {
    let payload = encodePayload(new Message("JOIN_ROOM", id));
    ws.send(payload)
}
