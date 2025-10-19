import java.util.*;
//import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class visuals extends JFrame implements ActionListener {
    // Text area to represent the numbers
    JTextField[] arr = new JTextField[9];
    // integer representation of where the numbers are
    int[] arrNum = new int[arr.length+1];
    //button that will randomize
    JButton random = new JButton("Random");
    // button to use A*
    JButton solve = new JButton("Solve");
    // Number of moves taken
    int moves = 0;
    boolean active = false;
    PriorityQueue<int[]> open = new PriorityQueue<int[]>((i1, i2) -> Integer.compare(i1[9],i2[9]));
    ArrayList<int[]> closed = new ArrayList<int[]>(100);
    

    //Creates the board
    public visuals() {
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
        arrNum[9] = calculate();
        add(random);
        random.addActionListener(this);
        add(new JTextArea("<-Randomize Board\n\n\n\n\n                     Solve Board->"));
        add(solve);
        solve.addActionListener(this);
    }
    
    // Just makes the board visible
    public static void main(String[] args) {
        visuals a = new visuals();
        a.setVisible(true);
        while (true) {
            while (a.active) {
                a.solve();
            }
            if (a.moves>0) {
                break;
            }
            try{Thread.sleep(100);} catch(Exception e) {}
        }
    }

    // Recognizes button presses
    public void actionPerformed(ActionEvent e) {
        if (e.getSource()==random) {
            randomize();
        }
        else if (e.getSource()==solve) {
            active = true;
        }
    }

    // Randomizes the board
    public void randomize() {
        ArrayList<Integer> list = new ArrayList<Integer>(arr.length);
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
        arrNum[9] = calculate();
    }

    // [Helper Function] Swap two pieces with each other
    // Arguments: one is the index of the zero piece, two is the index of the piece you want to swap with it
    public void swap(int one, int two) {
        arr[two].setText(""); // Set piece at index two to "0"
        arr[one].setText("" + arrNum[two]); // Set the piece at index one to whatever was at index two
        arrNum[one] = arrNum[two]; // replace 0 with the piece at index two
        arrNum[two] = 0; // replace the piece at index two with 0
    }

    // [Helper Function] Find the index of a specific number
    public static int findIndex(int[] arr, int n) {
        for (int i=0; i<arr.length; i++) {
            if (arr[i]==n) {
                return i;
            }
        }
        return -1;
    }

    // [Helper Function] Find the smallest number in an array
    public static int findSmall(int[] arr) {
        int small = 100;
        for (int i=0; i<arr.length; i++) {
            if (arr[i]<small) {
                small = arr[i];
            }
        }
        return small;
    }

    // [Helper Function] Clone an array, and potentially add an extra variable at the end
    public static int[] cloning(int[] arr, int n, int tmp) {
        int[] clone = new int[arr.length];
        clone = arr.clone();
        clone[clone.length-1] = tmp;
        return clone;
    }

    public static String stringy(int[] arr) {
        String tmp = "";
        for (int i=0; i<arr.length-1; i++) {
            tmp += arr[i];
        }
        return tmp+":"+arr[arr.length-1];
    }

    public static int[] deString(String tmp) {
        int[] arr = new int[10];
        for (int i=0; i<9; i++) {
            arr[i] = tmp.charAt(i)-'0';
        }
        arr[9] = Integer.parseInt(tmp.substring(tmp.indexOf(":")+1));
        return arr;
    }

        // [Helper Function] Calculate how far away the solver is from solving the board completely
    /*
     * 0 1 2
     * 3 4 5
     * 6 7 8
     * (i%3, i/3)
     */
    public int calculate() {
        int total = 0;
        for (int i=0; i<arrNum.length-1; i++) {
            int n = Math.abs(i%3-arrNum[i]%3)+Math.abs(i/3-arrNum[i]/3);
            // System.out.println(arrNum[i]+": "+n);
            if (arrNum[i]==0) n=0;
            total += n*n;
        }
        return total;
    }

    // [Helper Function] Calculate the distance if 0 were to swap with one of its neighbors
    public int doCalc(int n, int index) {
        swap(index, index+(2*n-3));
        int hold = 600;
        hold = calculate();
        swap(index+(2*n-3), index);
        return hold;
    }

    // [Helper Function] get a list of all possible choices to swap with
    public int[] getChoices() {
        int max = 600;
        int index = findIndex(arrNum, 0);
        int[] totals = new int[] {max, max, max, max}; // Up, Left, Right, Down
        if (index/3!=0) {// not 0, 1, 2; Swapping 0 with index above it
            totals[0] = doCalc(0, index);
        }
        if (index%3!=0) {// not 0, 3, 6; Swapping 0 with index to the left of it, calculate that distance, then add back again
            totals[1] = doCalc(1, index);
        }
        if (index%3!=2) {// not 2, 5, 8; Swapping 0 with index to the right of it
            totals[2] = doCalc(2, index);
        }
        if (index/3!=2) {// not 6, 7, 8; Swapping 0 with index below it
            totals[3] = doCalc(3, index);
        }
        return totals;
    }

    // Uses A* to solve the board
    @SuppressWarnings("unused")
    public void solve() {
        for (int calc = calculate(); calc!=0; calc=calculate()) {
            int[] totals = getChoices();
            System.out.println(Arrays.toString(totals));
            try {Thread.sleep(1000);} catch(Exception e) {}
            int izero = findIndex(arrNum, 0);
            int choice = findIndex(totals, findSmall(totals))*2-3;
            swap(izero, choice+izero);
        }
        active = false;
    }
}