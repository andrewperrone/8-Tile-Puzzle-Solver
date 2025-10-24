A* code: Solver.java

# 8-Tile-Puzzle-Solver
This project maps the state space of the well know 8 tile puzzle game and finds the optimal solution for any arbitrary starting puzzle state.

## How to run
### General Usage
If you want to see the 8-puzzle work on random inputs, this is the usage case for you

1.) In a terminal and in the same directory, write out "java Solver.java" in order to start the program

2.) You will see a solved board, you may press the random button in order to randomize the board into a solvable board state

3.) Once you have a non-solved board, pressing the Solve button will cause the program to attempt to solve the board

4.) The program will not accept button inputs while the board is being solved

5.) Once the program has found the solution, it will then replay it to the user in slow motion

6.) You may repeat starting from step 2


### Specific State
If you want to solve a specific 8-puzzle, then look no further, because this is the usage case for you

1.) In a terminal and in the same directory, write out "java Solver.java ", and then append the starting state of the board using numbers from left to right. Such as "java Solver.java 123876045"

2.) You will see the assigned board state, unless the board state you submitted isn't valid, in which case you will see the solved board state instead

3.) You may continue from step 3 in the General Usage
