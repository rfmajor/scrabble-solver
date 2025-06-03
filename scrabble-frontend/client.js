let utf8Encoder = new TextEncoder()
let utf8Decoder = new TextDecoder()
//let ws = new WebSocket("ws://localhost:8025/test");

// ws.binaryType = "arraybuffer"
// ws.onopen = function() {
// };
// ws.onmessage = function(e) {
//     let message = decodePayload(e.data)
//     alert(message);
// };
// ws.onclose = function() {
//     alert("closed");
// };

const MAX_MOVES = 8
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

function submitBoard() {
    let rack = encodeURIComponent(document.getElementsByName("rack")[0].value)
    const boardNotation = encodeURIComponent(getBoardNotation())
    const url = `http://localhost:8025/api/v1/moves?board=${boardNotation}&rack=${rack}`

    fetch(url)
        .then(response => response.json())
        .then(response => {
            const resultsList = document.getElementById("resultsList")
            resultsList.textContent = ''
            MOVES = []
            let i = 0
            for (const group of response) {
                const word = group['word']
                const points = group['points']
                const movePossibilities = group['movePossibilities']
                for (const move of movePossibilities) {
                    if (i >= MAX_MOVES) {
                        break
                    }

                    const moveObj = {
                        word: word,
                        x: move['x'],
                        y: move['y'],
                        direction: move['direction'],
                        points: points
                    }

                    MOVES.push(moveObj)

                    const moveText = `${word}, ${getHumanReadableCoords(move.x, move.y)} ${move.direction}, +${points}`
                    const li = document.createElement("li")
                    const textNode = document.createTextNode(moveText)
                    li.dataset.index = '' + i
                    li.onclick = async function (e) {
                        const id = li.dataset.index
                        await makeMove(MOVES[id])
                        document.getElementById("resultsList").innerText = ''
                        MOVES = []
                        CELLS_STACK = []
                    }
                    li.appendChild(textNode)
                    resultsList.appendChild(li)
                    i++
                }
            }
        })
}

function getHumanReadableCoords(x, y) {
    return `${String.fromCharCode('A'.charCodeAt(0) + y)}${x + 1}`
}

async function makeMove(move) {
    let x = move.x
    let y = move.y
    console.log(move)
    for (let i = 0; i < move.word.length; i++) {
        let c = move.word.charAt(i)
        if (isEmpty(y, x)) {
            putLetter(y, x, c)
        }
        if (move.direction === "ACROSS") {
            y++
        } else {
            x++
        }
    }
}
