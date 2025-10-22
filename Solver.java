/* Solver.java
 * Solves the a random 8-Tile puzzle using the A-Star Algorithm
 * 
 * Not all states are solveable:
 * N is the length of a side of a puzzle
 * An inversion is TODO
 * If N is odd, the puzzle instance is solvable if the number of inversions is even in the input state
 * If N is even, the puzzle instance is solvable if:
 *      -The blank space is on an even row counting from the bottom and the number of inversions is odd
 *      -The blank space is on an odd row counting from the bottom and the number of inversions is even
 * For all cases, the puzzle instance is not solvable 
 * 
 * TODO: BUG - Currently pressing random while the program is solving breaks the game
 */
 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

public class Solver extends JFrame implements ActionListener {
    // Text area to represent the numbers
    JTextField[] arr = new JTextField[9];
    // integer representation of where the numbers are
    int[] arrNum = new int[arr.length];
    //button that will randomize
    JButton random = new JButton("Random");
    // button to use A*
    JButton solve = new JButton("Solve");
    // Prioritize which path to go down based on which path has the smallest calculate
    PriorityQueue<String[]> open = new PriorityQueue<>((i1, i2) -> Integer.compare(Integer.parseInt(i1[2]),Integer.parseInt(i2[2])));
    // Configurations already tried
    ArrayList<String> closed = new ArrayList<>(1000000);
    // Tells me that we've started solving
    boolean start = false;

    boolean started = false;
    //String test = "";

    //Creates the board
    public Solver() {
        super("8 Puzzle Solver");
        setLocation(300,200);
        setSize(500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Row, Col, HorGap, VerGap
        setLayout(new GridLayout(4,3,15,15));
        for (int i=0; i<arr.length; i++) {
            arr[i] = new JTextField("");
            arr[i].setFont(new Font("Serif",Font.BOLD,30));
            arr[i].setHorizontalAlignment(JTextField.CENTER);
            arrNum[i] = i;
            if (i!=0) {
                arr[i].setText(""+i);
            }
            add(arr[i]);
        }
        add(random);
        random.addActionListener(this); //TODO: Remove Leaking Constructor. Best way would be to have a method that adds buttons after board creation
        add(new JTextArea("<-Randomize Board\n\n\n\n\n                     Solve Board->"));
        add(solve);
        solve.addActionListener(this);
    }

    public static void main(String[] args) {
        //Set up the board
        Solver a = new Solver();
        a.setVisible(true);
        String answer = ""; // Solution to the randomized board


        while (true) {
            if (a.start) {
                answer = a.solve(); // Start the solving proccess
                a.started = true; // Code will now begin the replay
                a.start=false; // No longer trying to solve
            }
            else if (a.started) {
                a.slowmo(answer); // Replay the solve
                a.started=false; // Replay has ended
                a.closed.clear(); // clear out the old states-used data
                a.open.clear(); // clear out the old states-to-use data
            }
            else {
                wait(1.0); // Keeps the while loop and JFrame in sync
            }
        }
    }

    // Randomizes the board, and re-runs if the board isn't solvable
    public void randomize() {
        // randomize
        ArrayList<Integer> list = new ArrayList<>(arr.length);
        for (int i=0; i<arr.length; i++) {
            list.add(i);
        }
        for (int i=0; i<arr.length; i++) {
            arrNum[i] = list.remove((int)(Math.random()*list.size()));
            if (arrNum[i]!=0) {
                arr[i].setText(""+arrNum[i]);
            }
            else {
                arr[i].setText("");
            }
        }
        //check if it's solvable
        int count = 0;
        for (int i=0; i<arrNum.length; i++) {
            if (arrNum[i]!=0) {
                for (int j=i+1; j<arrNum.length; j++) {
                    if (arrNum[j]!=0) {
                        if (arrNum[i]>arrNum[j]) {
                            count++;
                        }
                    }
                }
            }
        }
        if (count%2==1) { // Re-randomize if it's unsolvable
            randomize();
        }
    }

    // [Helper Function] Swap two pieces with each other
    // Arguments: one is the index of the zero piece, two is the index of the piece you want to swap with it
    public void swap(int one, int two) {
        arrNum[one] = arrNum[two]; // replace 0 with the piece at index two
        arrNum[two] = 0; // replace the piece at index two with 0
    }

    // [Helper Function] Swap two pieces with each other, and show the user the swap
    public void swapDisplay(int one, int two) {
        arr[two].setText(""); // Set piece at index two to "0"
        arr[one].setText("" + arrNum[two]); // Set the piece at index one to whatever was at index two
        arrNum[one] = arrNum[two]; // replace 0 with the piece at index two
        arrNum[two] = 0; // replace the piece at index two with 0
    }

    // Swap all pieces with each other
    public void completeSwap(int[] array) {
        arrNum = array; //removed manual array copy
        // for (int i=0; i<array.length; i++) {
        //     arrNum[i] = array[i];
        // }
    }

    //set the display to the current board
    public void changeDisplay() {
        for (int i=0; i<arrNum.length; i++) {
            arr[i].setText(""+arrNum[i]);
            if (arrNum[i]==0) arr[i].setText("");
        }
    }

    // interact with the buttons here
    @Override public void actionPerformed(ActionEvent e) { //add @Overide notation because that's how events work I think
        if (started || start) {
            return;
        }
        if (e.getSource()==random) {
            randomize();
        }
        else {
            start = true;
        }
    }

    // [Helper Function] Calculate how far away the solver is from solving the board completely
    /*
     * 0 1 2
     * 3 4 5
     * 6 7 8
     * (i%3, i/3)
     */
    public static int calculate(int[] array) {
        int total = 0;
        for (int i=0; i<array.length-1; i++) {
            int heightDif = Math.abs(i/3-array[i]/3);
            int widthDif = Math.abs(i%3-array[i]%3);

            int n = heightDif+widthDif; // Manhattan distance
            if (array[i]==0) n=0;
            total += n;
        }

        int num = 2; // numerical advantage given to states with solved sides
        if (array[6]==6 && array[7]==7 && array[8]==8) {// prioritize states where the bottom 3 pieces are solved
            total-=num;
        }
        if (array[2]==2 && array[5]==5 && array[8]==8) {// prioritize states where the right three are solved 
            total-=num;
        }
        return total;
    }

    // turn arrays into a string 
    // changed stringy -> toString
    public static String toString(int[] array) {
        String tmp = "";
        for (int i=0; i<array.length; i++) {
            tmp+=array[i];
        }
        return tmp;
    }

    // Helps find the index of a specific number in an array
    public static int findIndex(int[] array, int num) {
        for (int i=0; i<array.length; i++) {
            if (array[i]==num) {
                return i;
            }
        }
        return -1;
    }

    // Calculates each path's value, keeping track of the path required to get there, and adds it to the open list if it isn't already in play
    // val makes sure that the solver doesn't try to move out of bounds
    // index is the index of the 0
    // addIndex is how much do you have to add to get to the piece 0 swaps to in a 1D array [-3,-1,1,0] = [up,left,right,down]
    // moves is the previous moves used to get to this state
    public void doCalc(int val, int index, int addIndex, String moves) {
        if (val!=0) {
            String[] choices = new String[] {"u", "l", "r", "d"};
            moves+=choices[(addIndex+3)/2];

            swap(index, index+addIndex);

            String tmp = toString(arrNum);

            if (!closed.contains(tmp)) {
                String[] toAdd = new String[] {tmp,moves, (calculate(arrNum)+moves.length())+""};
                open.add(toAdd);
            }
            swap(index+addIndex,index);
        }
    }

    // Simple wait command to slow down the replay
    public static void wait(double time) {
        try {
            Thread.sleep((int)(time*1000));
        }
        catch(Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }

    // convert a string back into an array
    public static int[] unString(String tmp) {
        int[] arr = new int[9];
        for (int i=0; i<arr.length; i++) {
            arr[i] = tmp.charAt(i)-'0';
        }
        return arr;
    }

    // determine if the solver should keep trying to solve
    public static boolean keepGoing(int[] arr) {
        for (int i=0; i<arr.length; i++) {
            if (arr[i]-i!=0) {
                return true;
            }
        }
        return false;
    }

    // solves the board
    public String solve() {
        String[] choice = {toString(arrNum), "", "0"}; // Initial State
        while (keepGoing(arrNum)) { // Calculate how far away the board state is, stop if it's correct, and calculate each time
            int index = findIndex(arrNum, 0);

            doCalc(index/3, index, -3, choice[1]); // up
            doCalc(index%3, index, -1, choice[1]); // left
            doCalc(index%3-2, index, 1, choice[1]); // right
            doCalc(index/3-2, index, 3, choice[1]); // down

            closed.add(choice[0]); // Adds the state that is currenty being viewed

            choice = open.poll(); // gets the new most efficient state
            int[] newArr = unString(choice[0]); // the array of the next state
            completeSwap(newArr); // swap the current array with the new one

            changeDisplay(); // cool visual effects
        }

        start = false;
        return choice[1];
    }

    // shows the user how the board was solved
    public void slowmo(String answer) {
        String choice = closed.get(0);
        int[] deStringed = unString(choice);
        completeSwap(deStringed);
        changeDisplay();

        int index = findIndex(arrNum, 0);
        String mult = "ulrd";

        System.out.println(answer + ": " + answer.length());
        wait(1.0);
        for (int i=0; i<answer.length(); i++) {
            int swapper = index + mult.indexOf(answer.charAt(i))*2-3;
            // System.out.println(index + ": " + swapper + ": " + answer.charAt(i));
            swapDisplay(index, swapper);
            index = swapper;
            wait(0.75); //changed time 1.75 -> 0.75
        }
    }
}