How to map the old states ids to the new ones given an already compressed
GADDAG (but the 'destinationState' field in all arcs hasn't been mapped yet)
in a way that we don't use any more memory.

Process:
- iterate over all arcs in the array
- for each arc somehow get the destination state using the old state id 
- how does the id map to the new id? we need to somehow store this mapping
  before we finish processing each state in the first loop 

Ideas:
1. Re-use the 'arcs[][]' array <br><br>
    Each arc in the original array currently stores the state id in the first 
    32 bits and the letter set id in the other 32 bits (32,64). However, we 
    know the exact number of states in the end (15293316), as well as the 
    letter sets count (9404). They correspond to 24 and 14 bits for required
    storage space in each arc, respectively. <br><br>

    This means that we can change the thresholds in our arc a bit, using the 
    first 24 bits for storing the destination state id and using 14 bits 
    (24,38) for storing the letter set id. The rest of the bits 
    (38,64 - 26 bits) can be used for holding other data.<br><br>

    The idea is to check each state's new id and set the corresponding 
    38-64 bits in the 'arcs[oldStateIdx][0]' to this new id. Then we can
    iterate over each arc in the compressed array and lookup its new value 
    of 'destinationState' in the 'arcs[oldStateIdx][0]', then set it to be the
    new destinationState in the arc itself. <br><br>
   
    Also, even though the new state id grows significantly faster than the old 
    one (because the array consists of both states and arcs), we can still fit 
    every state id - the maximum target array size should be:

    `all non-empty arcs in 'arcs' array + all states in 'arcs'` \
    which is \
    `18251543 + 15293316 = 33544859` \
    which corresponds to 25 bits of required storage space for the largest id.<br><br>
2. Same idea as above, but instead re-use the target array we're currently
   operating on<br><br>
    In this approach we also modify the thresholds in the original arcs, but 
    instead of writing new state ids to the 'arcs' array, we write to the new
    one (particularly 'arcsAndStates[oldStateIdx]'). If the array is not large
    enough to write the value, we resize it to 2^n (n is the first value for 
    which the array length will be enough). <br><br>

    We can also be sure that the array won't grow more than it's expected 
    to - since the largest state id in the old array is 15293316, and the 
    target size of the new array is going to be at least 
    12511920+18251543=30763463 (non-empty states + non-empty arcs), it's going 
    to require the size of the array to be at least 2^25, which is enough to 
    store all of the states ids - 15293316 (empty or non-empty) even if none 
    of the originally empty states is eliminated from the final array.<br><br>

    Even though we store both states and arcs in the array, it's okay if we 
    happen to write a new id to the state instead of the arc, because the state 
    only uses its first 33 bits for a bit map (the polish alphabet requires 32 
    characters + a delimiter char). Therefore we can use the other 26 bits in 
    the same way.<br><br>

Preference: I would choose option 2, because it doesn't modify the 
original 'arcs', allowing it to be cleaned up by the GC a little bit earlier.
It's also more intuitive to use a single index for lookups, instead of storing 
the new state id in the first arc of every state in the original array and 
accessing it in this way: arcs[oldStateIdx][0].<br><br>

3. How to iterate over the arcs only? <br><br>
In scenario 2. we have 1 unused bit in each of the arcs and 5 unused bits in 
the states. We can use the last bit as a flag indicating the type of record. 
The only issue is that with the current polish alphabet (as of 01.11.2024) 
we're only 9573 + 1 arcs away from needing to use another bit, making it 26
bits in total, which would possibly leave no space for adding the flag if the 
polish dictionary is expanded. In that case it would probably be better to use
the approach no. 1. <br><br>

Better idea: count the number of set bits in the current state, then iterate 
from stateIdx + 1 to stateIdx + numOfSetBits - these will be the arcs. 
Then stateIdx + numOfSetBits + 1 is our next state. Repeat the process until
we reach the end of the array. This way we can use scenario no. 2.<br><br>
