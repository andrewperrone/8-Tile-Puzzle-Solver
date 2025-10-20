import java.util.*;
//import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
    PriorityQueue<String[]> open = new PriorityQueue<String[]>((i1, i2) -> Integer.compare(Integer.parseInt(i1[2]),Integer.parseInt(i2[2])));
    // Configurations already tried
    ArrayList<String> closed = new ArrayList<>(1000000);
    // Tells me that we've started solving
    boolean start = false;

    String test = "";

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
        random.addActionListener(this);
        add(new JTextArea("<-Randomize Board\n\n\n\n\n                     Solve Board->"));
        add(solve);
        solve.addActionListener(this);
    }

    public static void main(String[] args) {
        Solver a = new Solver();
        a.setVisible(true);
        boolean started = false;
        String answer = "";
        while (true) {
            if (a.start==true) {
                answer = a.solve();
                started = true;
                System.out.println("Hi");
                a.start=false;
            }
            else if (started==true) {
                a.slowmo(answer);
                started=false;
                a.closed.clear();
                a.open.clear();
            }
            else {
                wait(1.0);
            }
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
        if (count%2==1) {
            randomize();
        }
    }

    // [Helper Function] Swap two pieces with each other
    // Arguments: one is the index of the zero piece, two is the index of the piece you want to swap with it
    public void swap(int one, int two) {
        arrNum[one] = arrNum[two]; // replace 0 with the piece at index two
        arrNum[two] = 0; // replace the piece at index two with 0
    }

    public void swapDisplay(int one, int two) {
        arr[two].setText(""); // Set piece at index two to "0"
        arr[one].setText("" + arrNum[two]); // Set the piece at index one to whatever was at index two
        arrNum[one] = arrNum[two]; // replace 0 with the piece at index two
        arrNum[two] = 0; // replace the piece at index two with 0
    }

    public void completeSwap(int[] array) {
        for (int i=0; i<array.length; i++) {
            arrNum[i] = array[i];
        }
    }

    public void changeDisplay() {
        for (int i=0; i<arrNum.length; i++) {
            arr[i].setText(""+arrNum[i]);
            if (arrNum[i]==0) arr[i].setText("");
        }
    }

    public void actionPerformed(ActionEvent e) {
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
        int num = 2;
        for (int i=0; i<array.length-1; i++) {
            int heightDif = Math.abs(i/3-array[i]/3);
            // heightDif*=heightDif;
            int widthDif = Math.abs(i%3-array[i]%3);
            // widthDif*=widthDif;
            int n = heightDif+widthDif;
            if (array[i]==0) n=0;
            total += n;
        }

        if (array[6]==(array[7]-1) && (array[8]-2)==6 && array[6]==array[8]-2) {
            total-=num;
        }
        if (array[2]==2 && array[5]==5 && array[8]==8) {
            total-=num;
        }
        return total;
    }

    public static String stringy(int[] array, int n) {
        String tmp = "";
        for (int i=0; i<array.length; i++) {
            tmp+=array[i];
        }
        if (n!=-1) {
            tmp+=(calculate(array)+n);
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

    public void doCalc(int val, int index, int addIndex, String moves) {
        if (val!=0) {
            String[] choices = new String[] {"u", "l", "r", "d"};
            moves+=choices[(addIndex+3)/2];

            swap(index, index+addIndex);
            String tmp = stringy(arrNum, -1);
            if (!closed.contains(tmp)) {
                String[] toAdd = new String[] {tmp,moves, (calculate(arrNum)+moves.length())+""};
                // test += "[ " + tmp.substring(0, 9) + ":" + tmp.substring(9) + ", " + moves + " ]\n";
                open.add(toAdd);
            }
            swap(index+addIndex,index);
        }
    }

    public static void wait(double time) {
        try {
            Thread.sleep((int)(time*1000));
        }
        catch(Exception e) {

        }
    }

    public static int[] unString(String tmp) {
        int[] arr = new int[9];
        for (int i=0; i<arr.length; i++) {
            arr[i] = tmp.charAt(i)-'0';
        }
        return arr;
    }

    public static boolean keepGoing(int[] arr) {
        for (int i=0; i<arr.length; i++) {
            if (arr[i]-i!=0) {
                return true;
            }
        }
        return false;
    }

    public String solve() {
        String[] choice = {stringy(arrNum,-1), "", "0"}; // Initial State
        // int i = 0;
        while (keepGoing(arrNum)) { // Calculate how far away the board state is, stop if it's correct, and calculate each time
            int index = findIndex(arrNum, 0);

            doCalc(index/3, index, -3, choice[1]);
            doCalc(index%3, index, -1, choice[1]);
            doCalc(index%3-2, index, 1, choice[1]);
            doCalc(index/3-2, index, 3, choice[1]);

            closed.add(choice[0]);

            choice = open.poll();
            // System.out.println(choice[0].substring(9));
            int[] newArr = unString(choice[0]);
            completeSwap(newArr);
            changeDisplay();
            // System.out.println(closed);
            // System.out.println(test);
            // wait(.5);
            // i++;
            // System.out.println(i +":"+ choice[0].substring(9));
            // System.out.println(calc+choice[1].length());
        }
        // System.out.println(choice[1]);
        start = false;
        return choice[1];
    }

    public void slowmo(String answer) {
        String choice = closed.get(0);
        int[] deStringed = unString(choice);
        completeSwap(deStringed);
        changeDisplay();

        int index = findIndex(arrNum, 0);
        String mult = "ulrd";

        System.out.println(answer + ": " + answer.length());
        wait(3.0);
        for (int i=0; i<answer.length(); i++) {
            int swapper = index + mult.indexOf(answer.charAt(i))*2-3;
            // System.out.println(index + ": " + swapper + ": " + answer.charAt(i));
            swapDisplay(index, swapper);
            index=swapper;
            wait(1.75);
        }
    }
}