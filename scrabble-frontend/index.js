const COLORS = {
    "doubleLetter": "#4aa1c7",
    "tripleLetter": "#0604c1",
    "doubleWord": "#e6c3a8",
    "tripleWord": "#e80d0d",
    "borderColor": "#000000",
    "empty": "#095e12",
    "board": "#043608",
    "coords": "#ffffff"
}
const BOARD_LENGTH = 15
const CELL_SIZE_PX = 40
const BORDER_SIZE_PX = 1
const BOARD_PADDING_PX = 40
const BOARD_SIZE_PX = 16 * BORDER_SIZE_PX + 15 * CELL_SIZE_PX
const CELLS = new Set()

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

async function putLetter(x, y, letter, canvas) {
    let ctx = canvas.getContext("2d")
    let cell = getCellCoordsAndWidth(x, y)

    let img = new Image()
    img.src = `./assets/letters/${letter.toLowerCase()}.gif`
    img.onload = async function () {
        ctx.drawImage(img, cell.x, cell.y, cell.w, cell.h)
    }
    CELLS.add(`${cell.x},${cell.y}`)
}

function putHorizontalCoords(canvas) {
    let ctx = canvas.getContext("2d")
    ctx.font = "bold 20px arial"
    ctx.fillStyle = COLORS["coords"]
    ctx.textAlign = "left"
    ctx.textBaseline = "bottom"
    const beginning = 'A'.charCodeAt(0)
    for (let i = 0; i < 15; i++) {
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
    for (let i = 0; i < 15; i++) {
        let letter = (i + 1) + ''
        let cell = getCellCoordsAndWidth(0, i)
        let text = ctx.measureText(letter);
        ctx.fillText(letter, cell.x - CELL_SIZE_PX + ((cell.w / 2) - (text.width / 2)), cell.y + CELL_SIZE_PX / 4)
    }
}

window.onload = async function() {
    const CANVAS = document.getElementById("scrabble-canvas")

    let specialFields = await fetch('./assets/specialFields.json')
        .then(response => response.json())

    // board padding
    let ctx = CANVAS.getContext("2d")
    ctx.fillStyle = COLORS["board"]
    ctx.fillRect(0, 0, BOARD_SIZE_PX + 2 * BOARD_PADDING_PX, BOARD_SIZE_PX + 2 * BOARD_PADDING_PX)

    let specialFieldsPopulated = new Set()

    for (const key of Object.keys(specialFields)) {
        for (let field of specialFields[key]) {
            await fillCell(field[0], field[1], COLORS[key], CANVAS)
            specialFieldsPopulated.add(`${field[0]}, ${field[1]}`)
        }
    }

    for (let i = 0; i < BOARD_LENGTH; i++) {
        for (let j = 0; j < BOARD_LENGTH; j++) {
            if (!(specialFieldsPopulated.has(`${i}, ${j}`))) {
                await fillCell(i, j, COLORS["empty"], CANVAS)
            }
        }
    }

    putHorizontalCoords(CANVAS)
    putVerticalCoords(CANVAS)

    await putLetter(8, 7, 'e', CANVAS)
    await putLetter(7, 7, 'w', CANVAS)
    await putLetter(9, 7, 'ź', CANVAS)
    await putLetter(10, 7, 'blank', CANVAS)
    await putLetter(11, 7, 'e', CANVAS)

    await putLetter(7, 8, 'e', CANVAS)
    await putLetter(7, 9, 's', CANVAS)
    await putLetter(7, 10, 'z', CANVAS)
    await putLetter(7, 11, 'ł', CANVAS)
    await putLetter(7, 12, 'o', CANVAS)
}
