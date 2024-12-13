# The game process

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

{
    "word": "żółwią",
    "points": 50,
    "possibilities": [
        {"x": 6, "y": 7, "position": "G8", "direction": "DOWN", "newBlankFields": [...]}, 
        {"x": 7, "y": 7, "position": "H8", "direction": "DOWN", "newBlankFields": [...]}, 
    ]
}

# Class layout

Class layout needs to be re-implemented. All classes which hold the calculation
logic should be stateless if possible. There are too many interdependencies
between different types of classes in the current architecture.

1. MoveGenerator has 2 class members, both of type MoveAlgorithmExecutor. Both
   of them hold the reference to the Board object which is not good because we
   want to be able to provide the calculation logic with our own Board object
   with each method invocation. Also there shouldn't be any distinction made
   between these types of executors, they should perform their computation
   based on the play direction declared as a method argument, not a class
   field.
2. All classes should only hold a reference to the Gaddag object.
3. SpecialFields file should be included in the common module.
4. MoveGenerator should return all possible moves with the points already
   calculated (the point calculator invocation should be hidden).
