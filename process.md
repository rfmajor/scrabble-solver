The game process:

1. Request is sent to the game server with the board image as the payload
2. Detection service processes the image and forwards the results to the game
   service
3. The game service uses the completion service for the board state validation
   and applies corrections if neccessary (and possible)
4. The game server stores the board state in cache so that only the words
   which were recently added are checked. This way the rest of the board from
   the detection service doesn't need to be taken into account each time which
   would greatly reduce the chance of errors
- storing the board state as the session parameter
- invalidating the session after 30 minutes of inactivity

5.  


{
    "word": "żółwią",
    "points": 50,
    "possibilities": [
        {"x": 6, "y": 7, "position": "G8", "direction": "DOWN", "newBlankFields": [...]}, 
        {"x": 7, "y": 7, "position": "H8", "direction": "DOWN", "newBlankFields": [...]}, 
    ]
}
