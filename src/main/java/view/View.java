package view;

import javax.swing.*;

public class View extends JFrame {
    private JButton button1;
    private JButton button2;
    private JPanel _;

    public View (){
        super("Information retrieval with Lucene.");
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(_);
        setSize(300,300);
    }
}
