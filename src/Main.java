import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
void main() {
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    IO.println(String.format("Hello and welcome!"));

    JFrame jFrame = new JFrame("Mario DFA Animation");
    jFrame.add(new MarioFSM(WIDTH, HEIGHT));
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jFrame.setSize(WIDTH, HEIGHT);
    jFrame.setVisible(true);
}

        public static final int WIDTH = 600;
        public static final int HEIGHT = 400;
