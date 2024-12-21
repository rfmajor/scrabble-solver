const COLORS = {
    "doubleLetter": "#4aa1c7",
    "tripleLetter": "#0604c1",
    "doubleWord": "#e6c3a8",
    "tripleWord": "#e80d0d",
    "borderColor": "#000000",
    "letterBackground": "#ea924a",
    "default": "#095e12"
}
const BOARD_LENGTH = 15
const CELL_SIZE = 40
const CELL_BORDER_SIZE = 1
const BOARD_SIZE_PX = 16 * CELL_BORDER_SIZE + 15 * CELL_SIZE
const CELLS = new Set()

function getCellCoordsAndWidth(x, y) {
    // needs to include the border size:
    // eg. x = 0 -> border + 0 * cell_size
    // x = 1 -> 2 * border + 1 * cell_size
    // x = 2 -> 3 * border + 2 * cell_size
    let cell_x = (x + 1) * CELL_BORDER_SIZE + x * CELL_SIZE
    let cell_y = (y + 1) * CELL_BORDER_SIZE + y * CELL_SIZE
    let cell_w = CELL_SIZE
    let cell_h = CELL_SIZE
    return {x: cell_x, y: cell_y, w: cell_w, h: cell_h}
}

async function fillCell(x, y, color, canvas) {
    let ctx = canvas.getContext("2d")

    let cell = getCellCoordsAndWidth(x, y)

    // border_x = cell_x - border
    let border_x = cell.x - CELL_BORDER_SIZE
    let border_y = cell.y - CELL_BORDER_SIZE
    let border_w = cell.w + 2 * CELL_BORDER_SIZE
    let border_h = cell.h + 2 * CELL_BORDER_SIZE

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
    CELLS.add(cell.x, cell.y)
}

window.onload = async function() {
    const CANVAS = document.getElementById("scrabble-canvas")

    let specialFields = await fetch('./assets/specialFields.json')
        .then(response => response.json())

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
                await fillCell(i, j, COLORS["default"], CANVAS)
            }
        }
    }

    await putLetter(7, 7, 'w', CANVAS)
    await putLetter(8, 7, 'e', CANVAS)
    await putLetter(9, 7, 'ź', CANVAS)
    await putLetter(10, 7, 'blank', CANVAS)
    await putLetter(11, 7, 'e', CANVAS)
}
