#!/usr/bin/bash

SESSION_NAME=scrabble-project
 
tmux has-session -t $SESSION_NAME > /dev/null 2>&1
if [ $? != 0 ]; then
    tmux new-session -d -s $SESSION_NAME -c $HOME/Git/Repos/scrabble-solver

    window=1
    tmux rename-window -t $SESSION_NAME:$window 'main'

    window=2
    tmux new-window -t $SESSION_NAME:$window -n detection -c \
        $HOME/Git/Repos/scrabble-solver/scrabble-detection
fi

tmux attach -t $SESSION_NAME:1
