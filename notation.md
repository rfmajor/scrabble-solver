Plays are usually notated in the form "WORD xy +score" where WORD indicates the
main word played, xy is the coordinate of the first letter of the main word,
and score is the score for the play.

If the main word reads left-to-right, the row number precedes the column
letter, and if the main word reads top-to-bottom, the column letter precedes
the row number. All letters (except for blank tiles) are capitalized.

Finally, if a word is played through one or more letters, the letters that were
already on the board are surrounded by parentheses ().
Examples:

- QUAY 8E +32
- PRE(Q)UeLS E5 +122


Scrabble FEN (Forsyth-Edwards Notation)-like Format:

<row1>/<row2>/.../<row15> b:<x1>;<y1>;<L1>,<x2>;<y2>;<L2>,...

Components:

- Each row encodes 15 columns.
- Letters (A-Z) represent actual tiles.
- Digits (1–15) represent consecutive empty spaces (like FEN in chess).
- Slash / separates rows.
- After the board, optional " b:" section indicates blank tiles, listing their positions and the letter they represent.

Example:
Let's say the board has:

- "HELLO" placed horizontally starting at (H8) – middle row, middle column.
- A blank tile used for "O" in that word.

It would look like:

15/15/15/15/15/15/7HELLO3/15/15/15/15/15/15/15/15 b:11;7;O

Explanation:
7HELLO3 is row 8: 7 empty squares, then "HELLO", then 3 empty.

All other rows are empty: 15.

b:11;7;O → a blank tile at column 11 (L), row 7, representing "O".
