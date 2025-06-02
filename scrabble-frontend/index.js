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
const VI_COLORS = {
    "INSERT": "#da2b2b",
    "VERTICAL_INSERT": "#193bf3",
    "NORMAL": "#ecdada"
}
const BOARD_LENGTH = 15
const CELL_SIZE_PX = 40
const BORDER_SIZE_PX = 1
const BOARD_PADDING_PX = 40
const BOARD_SIZE_PX = 16 * BORDER_SIZE_PX + 15 * CELL_SIZE_PX
let CELLS = new Map()
let CELLS_ARR = Array(BOARD_LENGTH).fill(0).map(_row => Array(BOARD_LENGTH).fill(''))
let MOVES = []
let BLANKS = new Set()
const CELLS_SNAPSHOTS = []
const ALPHABET = new Set("aąbcćdeęfghijklłmnńoóprsśtuwyzźż".split(""))

let currentCell = {x: 7, y: 7}
let viMode = "NORMAL"
let blankMode = false

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
    let cell = getCellCoordsAndWidth(x, y)
    
    // JS Canvas API coordinate system is transposed in relation to the scrabble coordinate system
    return CELLS_ARR[y][x] === ''
}
async function putLetter(x, y, letter, canvas, blank) {
    if (x < 0 || y < 0 || x >= BOARD_LENGTH || y >= BOARD_LENGTH) {
        return
    }
    let ctx = canvas.getContext("2d")
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
    CELLS_ARR[y][x] = letter
}

async function removeLetter(x, y, canvas) {
    let ctx = canvas.getContext("2d")
    let cell = getCellCoordsAndWidth(x, y)

    ctx.clearRect(cell.x, cell.y, cell.w, cell.h)

    BLANKS.delete(JSON.stringify({row: y, col: x}))
    CELLS_ARR[y][x] = ''
}

async function highlightLetter(x, y, color, canvas, clearFirst) {
    let ctx = canvas.getContext("2d")
    let cell = getCellCoordsAndWidth(x, y)

    if (clearFirst) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
    }

    ctx.shadowColor = "#000000";
    ctx.shadowBlur = 100;
    ctx.lineJoin = "bevel";
    ctx.lineWidth = 5;
    ctx.strokeStyle = color

    ctx.strokeRect(cell.x, cell.y, cell.w, cell.h)
}

function recordSnapshot() {
    CELLS_SNAPSHOTS.push(new Map(CELLS))
}

async function restoreSnapshot() {
    if (CELLS_SNAPSHOTS.length > 0) {
        CELLS = CELLS_SNAPSHOTS.pop()
        await redrawFromCells(document.getElementById("tiles-canvas"))
    }
}

async function redrawFromCells(tileCanvas) {
    clearCanvas(tileCanvas)
    for (let x = 0; x < BOARD_LENGTH; x++) {
        for (let y = 0; y < BOARD_LENGTH; y++) {
            let letter = CELLS_ARR[y][x]
            if (letter !== '') {
                let cell = getCellCoordsAndWidth(x, y)
                await putLetter(cell.x, cell.y, letter, tileCanvas)
            }
        }
    }
}

function clearCanvas(canvas) {
    canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height)
}

async function interpretKeyCode(e) {
    // TODO: this is ugly and hacky (every input I add in the future will have to satisfy a similar condition), fix later
    if (e.target === document.getElementById("rack")) {
        return
    }

    const INPUT_CANVAS = document.getElementById("input-canvas")
    const MAIN_CANVAS = document.getElementById("main-canvas")
    const TILES_CANVAS = document.getElementById("tiles-canvas")

    let keyCode = e.key
    console.log(keyCode)

    let modeSwitched = await checkForModeSwitch(keyCode)
    await highlightLetter(currentCell.x, currentCell.y, VI_COLORS[viMode], INPUT_CANVAS, true)
    if (modeSwitched) {
        return
    }

    switch (viMode) {
        case "NORMAL":
            await handleNormalMode(keyCode, TILES_CANVAS, INPUT_CANVAS)
            break
        case "INSERT":
            await handleInsertMode(keyCode, TILES_CANVAS, INPUT_CANVAS)
            break
        case "VERTICAL_INSERT":
            await handleVerticalInsertMode(keyCode, TILES_CANVAS, INPUT_CANVAS)
            break
    }
}

async function checkForModeSwitch(key) {
    switch (key) {
        case 'i':
            if (viMode === "NORMAL") {
                viMode = "INSERT"
                return true
            }
            return false
        case 'I':
            if (viMode === "NORMAL") {
                viMode = "VERTICAL_INSERT"
                return true
            }
            return false
        case 'Escape':
            if (viMode === "INSERT" || viMode === "VERTICAL_INSERT") {
                viMode = "NORMAL"
                return true
            }
            return false
        default:
            return false
    }
}

async function handleNormalMode(key, tilesCanvas, inputCanvas) {
    switch (key) {
        case 'j':
            await moveCursor(currentCell.x, currentCell.y + 1, VI_COLORS["NORMAL"], inputCanvas)
            break
        case 'k':
            await moveCursor(currentCell.x, currentCell.y - 1, VI_COLORS["NORMAL"], inputCanvas)
            break
        case 'h':
            await moveCursor(currentCell.x - 1, currentCell.y, VI_COLORS["NORMAL"], inputCanvas)
            break
        case 'l':
            await moveCursor(currentCell.x + 1, currentCell.y, VI_COLORS["NORMAL"], inputCanvas)
            break
        case 'i':
            await moveCursor(currentCell.x + 1, currentCell.y, VI_COLORS["NORMAL"], inputCanvas)
            break
        case 'x':
            await removeLetter(currentCell.x, currentCell.y, tilesCanvas)
            break
    }
}

async function handleInsertMode(key, tilesCanvas, inputCanvas) {
    if (key === "Enter") {
        blankMode = true
        return
    }
    if (key === "Backspace") {
        await removeLetter(currentCell.x, currentCell.y, tilesCanvas)
        await moveCursor(currentCell.x - 1, currentCell.y, VI_COLORS["INSERT"], inputCanvas)
    } else if (ALPHABET.has(key) || key === "blank") {
        await putLetter(currentCell.x, currentCell.y, key, tilesCanvas, blankMode)
        await moveCursor(currentCell.x + 1, currentCell.y, VI_COLORS["INSERT"], inputCanvas)
    }
    if (key !== "AltGraph") {
        blankMode = false
    }
}

async function handleVerticalInsertMode(key, tilesCanvas, inputCanvas) {
    if (key === "Enter") {
        blankMode = true
        return
    }
    if (key === "Backspace") {
        await removeLetter(currentCell.x, currentCell.y, tilesCanvas)
        await moveCursor(currentCell.x, currentCell.y - 1, VI_COLORS["VERTICAL_INSERT"], inputCanvas)
    } else if (ALPHABET.has(key) || key === "blank") {
        await putLetter(currentCell.x, currentCell.y, key, tilesCanvas, blankMode)
        await moveCursor(currentCell.x, currentCell.y + 1, VI_COLORS["VERTICAL_INSERT"], inputCanvas)
    }
    if (key !== "AltGraph") {
        blankMode = false
    }
}

async function moveCursor(x, y, color, canvas) {
    if (x < 0 || y < 0 || x >= BOARD_LENGTH || y >= BOARD_LENGTH) {
        return
    }
    currentCell = {x: x, y: y}
    await highlightLetter(x, y, color, canvas, true)
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
    for (let row of CELLS_ARR) {
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
    const MAIN_CANVAS = document.getElementById("main-canvas")
    const INPUT_CANVAS = document.getElementById("input-canvas")
    const TILES_CANVAS = document.getElementById("tiles-canvas")

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
}
