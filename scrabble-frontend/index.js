const COLORS = {
    "doubleLetter": "#4aa1c7",
    "tripleLetter": "#0604c1",
    "doubleWord": "#e6c3a8",
    "tripleWord": "#e80d0d",
    "borderColor": "#000000",
    "letterBackground": "#ea924a",
    "default": "#095e12"
}

window.onload = async function() {
    const CANVAS = document.getElementById("scrabble-canvas")
    const BOARD_SIZE_PX = CANVAS.width
    const BOARD_LENGTH = 15
    const CELL_SIZE = BOARD_SIZE_PX / BOARD_LENGTH
    const CELL_BORDER_SIZE = 1

    async function fillCell(x, y, color) {
        let ctx = CANVAS.getContext("2d")
        ctx.fillStyle = COLORS["borderColor"]

        let cell_x = CELL_SIZE * x
        let cell_y = CELL_SIZE * y
        let cell_inner_width = CELL_SIZE - 2 * CELL_BORDER_SIZE
        let cell_inner_height = CELL_SIZE - 2 * CELL_BORDER_SIZE

        ctx.fillRect(cell_x, cell_y, CELL_SIZE, CELL_SIZE)
        ctx.clearRect(cell_x + CELL_BORDER_SIZE, cell_y + CELL_BORDER_SIZE, cell_inner_width, cell_inner_height)

        ctx.fillStyle = color
        ctx.fillRect(cell_x + CELL_BORDER_SIZE, cell_y + CELL_BORDER_SIZE, cell_inner_width, cell_inner_height)
    }

    async function putLetter(x, y, letter) {
        let ctx = CANVAS.getContext("2d")
        let cell_x = CELL_SIZE * x
        let cell_y = CELL_SIZE * y

        let img = new Image()
        img.src = `./assets/letters/${letter.toLowerCase()}.gif`
        img.onload = async function () {
            ctx.drawImage(img, cell_x, cell_y, CELL_SIZE, CELL_SIZE)
        }
    }

    let specialFields = await fetch('./assets/specialFields.json')
        .then(response => response.json())

    let specialFieldsPopulated = new Set()

    for (const key of Object.keys(specialFields)) {
        for (let field of specialFields[key]) {
            await fillCell(field[0], field[1], COLORS[key])
            specialFieldsPopulated.add(`${field[0]}, ${field[1]}`)
        }
    }

    for (let i = 0; i < BOARD_LENGTH; i++) {
        for (let j = 0; j < BOARD_LENGTH; j++) {
            if (!(specialFieldsPopulated.has(`${i}, ${j}`))) {
                await fillCell(i, j, COLORS["default"])
            }
        }
    }

    await putLetter(7, 7, 'w')
    await putLetter(8, 7, 'e')
    await putLetter(9, 7, 'ź')
    await putLetter(10, 7, 'blank')
    await putLetter(11, 7, 'e')
}
