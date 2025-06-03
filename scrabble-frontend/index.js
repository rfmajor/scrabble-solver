const COLORS = {
    "doubleLetter": "#4aa1c7",
    "tripleLetter": "#0604c1",
    "doubleWord": "#e6c3a8",
    "tripleWord": "#e80d0d",
    "borderColor": "#000000",
    "empty": "#095e12",
    "board": "#043608",
    "coords": "#ffffff",
}
const INPUT_COLORS = {
    "INSERT": "#da2b2b",
    "VERTICAL_INSERT": "#193bf3",
    "NORMAL": "#ecdada"
}
const BOARD_LENGTH = 15
const CELL_SIZE_PX = 40
const BORDER_SIZE_PX = 1
const BOARD_PADDING_PX = 40
const BOARD_SIZE_PX = 16 * BORDER_SIZE_PX + 15 * CELL_SIZE_PX
let MAIN_CANVAS, INPUT_CANVAS, TILES_CANVAS
let CELLS = Array(BOARD_LENGTH).fill(0).map(_row => Array(BOARD_LENGTH).fill(''))
let CELLS_STACK = []
let MOVES = []
let BLANKS = new Set()
const ALPHABET = new Set("aąbcćdeęfghijklłmnńoóprsśtuwyzźż".split(""))
const U_ALPHABET = new Set("AĄBCĆDEĘFGHIJKLŁMNŃOÓPRSŚTUWYZŹŻ".split(""))

let currentCell = {x: 7, y: 7}
let inputMode = "INSERT"

function getCellCoordsAndWidth(x, y) {
    // needs to include the border size:
    // eg. x = 0 -> border + 0 * cell_size
    // x = 1 -> 2 * border + 1 * cell_size
    // x = 2 -> 3 * border + 2 * cell_size
    let cell_x = BOARD_PADDING_PX + (x + 1) * BORDER_SIZE_PX + x * CELL_SIZE_PX
    let cell_y = BOARD_PADDING_PX + (y + 1) * BORDER_SIZE_PX + y * CELL_SIZE_PX
    let cell_w = CELL_SIZE_PX
    let cell_h = CELL_SIZE_PX
    return {x: cell_x, y: cell_y, w: cell_w, h: cell_h}
}

function getCell(x, y) {
    let cellX = Math.floor(((x - BOARD_PADDING_PX) / BOARD_SIZE_PX) * BOARD_LENGTH)
    let cellY = Math.floor(((y - BOARD_PADDING_PX) / BOARD_SIZE_PX) * BOARD_LENGTH)

    return {x: cellX, y: cellY}
}

async function fillCell(x, y, color, canvas) {
    let ctx = canvas.getContext("2d")

    let cell = getCellCoordsAndWidth(x, y)

    // border_x = cell_x - border
    let border_x = cell.x - BORDER_SIZE_PX
    let border_y = cell.y - BORDER_SIZE_PX
    let border_w = cell.w + 2 * BORDER_SIZE_PX
    let border_h = cell.h + 2 * BORDER_SIZE_PX

    // border
    ctx.fillStyle = COLORS["borderColor"]
    ctx.fillRect(border_x, border_y, border_w, border_h)
    // ctx.clearRect(cell_x + CELL_BORDER_SIZE, cell_y + CELL_BORDER_SIZE, cell_inner_width, cell_inner_height)

    ctx.fillStyle = color
    ctx.fillRect(cell.x, cell.y, cell.w, cell.h)
}

function isEmpty(x, y) {
    if (x < 0 || y < 0 || x >= BOARD_LENGTH || y >= BOARD_LENGTH) {
        return true
    }

    // JS Canvas API coordinate system is transposed in relation to the scrabble coordinate system
    return CELLS[y][x] === ''
}

async function putLetter(x, y, letter, blank) {
    if (x < 0 || y < 0 || x >= BOARD_LENGTH || y >= BOARD_LENGTH) {
        return
    }
    let ctx = TILES_CANVAS.getContext("2d")
    let cell = getCellCoordsAndWidth(x, y)

    let imgName = letter.toLowerCase()
    if (blank) {
        imgName += '_blank'
        BLANKS.add(JSON.stringify({row: y, col: x}))
    } 
    let img = new Image()
    img.src = `./assets/letters/${imgName}.png`
    img.onload = async function () {
        ctx.drawImage(img, cell.x, cell.y, cell.w, cell.h)
    }

    // JS Canvas API coordinate system is transposed in relation to the scrabble coordinate system
    CELLS[y][x] = letter
    CELLS_STACK.push({x: x, y: y})
}

async function removeStackLetter() {
    if (CELLS_STACK.length === 0) {
        return
    }
    let ctx = TILES_CANVAS.getContext("2d")
    let cell = CELLS_STACK.pop()
    let actualCell = getCellCoordsAndWidth(cell.x, cell.y)

    ctx.clearRect(actualCell.x, actualCell.y, actualCell.w, actualCell.h)

    BLANKS.delete(JSON.stringify({row: cell.y, col: cell.x}))
    CELLS[cell.y][cell.x] = ''
    await moveCursor(cell.x, cell.y)
}

async function highlightLetter(x, y) {
    let ctx = INPUT_CANVAS.getContext("2d")
    let cell = getCellCoordsAndWidth(x, y)

    clearCanvas(INPUT_CANVAS)

    ctx.lineJoin = "bevel";
    ctx.lineWidth = 5;
    ctx.strokeStyle = INPUT_COLORS[inputMode]

    ctx.strokeRect(cell.x, cell.y, cell.w, cell.h)
}

function clearCanvas(canvas) {
    canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height)
}

async function interpretKeyCode(e) {
    // TODO: this is ugly and hacky (every input I add in the future will have to satisfy a similar condition), fix later
    if (e.target === document.getElementById("rack")) {
        return
    }

    let keyCode = e.key
    console.log(keyCode)
    await handleKeycode(keyCode)
}

async function interpretClick(e, canvas) {
    const rect = canvas.getBoundingClientRect()
    const x = e.clientX - rect.left
    const y = e.clientY - rect.top
    let cell = getCell(x, y)
    if (currentCell && currentCell.x === cell.x && currentCell.y === cell.y) {
        await changeInputDirection()
    }
    if (CELLS[cell.y][cell.x] === '') {
        await moveCursor(cell.x, cell.y)
    }
}

async function handleKeycode(key) {
    switch (key) {
        case 'ArrowDown':
            await moveCursorOneField("DOWN", true)
            break
        case 'ArrowUp':
            await moveCursorOneField("UP", true)
            break
        case 'ArrowLeft':
            await moveCursorOneField("LEFT", true)
            break
        case 'ArrowRight':
            await moveCursorOneField("RIGHT", true)
            break
        case 'Backspace':
            await removeStackLetter()
            break
        case 'Escape':
            await changeInputDirection()
            break
        case 'AltGraph':
            break
        default:
            if (ALPHABET.has(key) || U_ALPHABET.has(key)) {
                let blank = false
                if (U_ALPHABET.has(key)) {
                    key = key.toLowerCase()
                    blank = true
                }
                await handleLetter(key, blank)
            }
    }
}

async function changeInputDirection() {
    if (inputMode === "VERTICAL_INSERT") {
        inputMode = "INSERT"
    } else if (inputMode === "INSERT") {
        inputMode = "VERTICAL_INSERT"
    }
    if (currentCell) {
        await highlightLetter(currentCell.x, currentCell.y)
    }
}

async function handleLetter(key, blank) {
    if (!currentCell) {
        return
    }
    await putLetter(currentCell.x, currentCell.y, key, blank)
    if (inputMode === "INSERT") {
        await moveCursorOneField("RIGHT")
    } else if (inputMode === "VERTICAL_INSERT") {
        await moveCursorOneField("DOWN")
    }
}

function findNextEmptyCell(direction) {
    if (!currentCell) {
        return
    }
    let move = moveTowardsDirection(currentCell.x, currentCell.y, direction)
    while (move.x >= 0 && move.y >= 0 && move.x < BOARD_LENGTH && move.y < BOARD_LENGTH) {
        if (CELLS[move.y][move.x] === '')  {
            return move
        }
        move = moveTowardsDirection(move.x, move.y, direction)
    }
    return null
}

function moveTowardsDirection(x, y, direction) {
    switch (direction) {
        case 'UP':
            y--
            break
        case 'DOWN':
            y++
            break
        case 'LEFT':
            x--
            break
        case 'RIGHT':
            x++
            break
    }
    return {x: x, y: y}
}

async function moveCursor(x, y) {
    if (x < 0 || y < 0 || x >= BOARD_LENGTH || y >= BOARD_LENGTH) {
        return
    }
    currentCell = {x: x, y: y}
    await highlightLetter(x, y)
}

async function moveCursorOneField(direction, bounded) {
    if (!currentCell) {
        return
    }
    let x = currentCell.x
    let y = currentCell.y
    if (x < 0 || y < 0 || x >= BOARD_LENGTH || y >= BOARD_LENGTH) {
        return
    }
    let nextEmptyCell = findNextEmptyCell(direction)
    if (nextEmptyCell === null) {
        if (!bounded) {
            currentCell = null
            clearCanvas(INPUT_CANVAS)
        }
        return
    }
    currentCell = {x: nextEmptyCell.x, y: nextEmptyCell.y}
    await highlightLetter(currentCell.x, currentCell.y)
}

function putHorizontalCoords(canvas) {
    let ctx = canvas.getContext("2d")
    ctx.font = "bold 20px arial"
    ctx.fillStyle = COLORS["coords"]
    ctx.textAlign = "left"
    ctx.textBaseline = "bottom"
    const beginning = 'A'.charCodeAt(0)
    for (let i = 0; i < BOARD_LENGTH; i++) {
        let letter = String.fromCharCode(beginning + i)
        let cell = getCellCoordsAndWidth(i, 0)
        let text = ctx.measureText(letter);
        ctx.fillText(letter, cell.x + ((cell.w / 2) - (text.width / 2)), cell.y - CELL_SIZE_PX / 4)
    }
}

function putVerticalCoords(canvas) {
    let ctx = canvas.getContext("2d")
    ctx.font = "bold 20px arial"
    ctx.fillStyle = COLORS["coords"]
    ctx.textAlign = "left"
    ctx.textBaseline = "top"
    for (let i = 0; i < BOARD_LENGTH; i++) {
        let letter = (i + 1) + ''
        let cell = getCellCoordsAndWidth(0, i)
        let text = ctx.measureText(letter);
        ctx.fillText(letter, cell.x - CELL_SIZE_PX + ((cell.w / 2) - (text.width / 2)), cell.y + CELL_SIZE_PX / 4)
    }
}

function getBoardNotation() {
    let notation = ''
    for (let row of CELLS) {
        let counter = 0
        let rowNotation = ''
        for (let col of row) {
            if (col === '') {
                counter++
            } else {
                if (counter !== 0) {
                    rowNotation += counter
                }
                rowNotation += col
                counter = 0
            }
        }
        if (counter !== 0) {
            rowNotation += counter
        }
        notation += `${rowNotation}/`
    }
    // trim trailing "/"
    notation = notation.substring(0, notation.length - 1)

    if (BLANKS.size > 0) {
        notation += " b:"
    }
    for (let blank of BLANKS) {
        const coords = JSON.parse(blank)
        notation += `${coords.col};${coords.row},`
    }
    // trim trailing ","
    notation = notation.substring(0, notation.length - 1)
    return notation
}

window.onload = async function() {
    MAIN_CANVAS = document.getElementById("main-canvas")
    INPUT_CANVAS = document.getElementById("input-canvas")
    TILES_CANVAS = document.getElementById("tiles-canvas")

    let specialFields = await fetch('./assets/specialFields.json')
        .then(response => response.json())

    // board padding
    let ctx = MAIN_CANVAS.getContext("2d")
    ctx.fillStyle = COLORS["board"]
    ctx.fillRect(0, 0, BOARD_SIZE_PX + 2 * BOARD_PADDING_PX, BOARD_SIZE_PX + 2 * BOARD_PADDING_PX)

    let specialFieldsPopulated = new Set()

    for (const key of Object.keys(specialFields)) {
        for (let field of specialFields[key]) {
            await fillCell(field[0], field[1], COLORS[key], MAIN_CANVAS)
            specialFieldsPopulated.add(`${field[0]},${field[1]}`)
        }
    }

    for (let i = 0; i < BOARD_LENGTH; i++) {
        for (let j = 0; j < BOARD_LENGTH; j++) {
            if (!(specialFieldsPopulated.has(`${i},${j}`))) {
                await fillCell(i, j, COLORS["empty"], MAIN_CANVAS)
            }
        }
    }

    putHorizontalCoords(MAIN_CANVAS)
    putVerticalCoords(MAIN_CANVAS)

    window.addEventListener('keydown', interpretKeyCode, false);
    INPUT_CANVAS.addEventListener('mousedown', function (e) {
        interpretClick(e, INPUT_CANVAS)
    })
}
