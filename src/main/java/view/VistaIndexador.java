package view;

import javax.swing.*;

public class VistaIndexador extends JFrame {
    private JFrame parent;
    private JTextField indexTextField;
    private JTextField textField2;
    private JButton indexarButton;
    private JButton volverButton;
    private JPanel _;

    public VistaIndexador(JFrame parent){
        this.parent=parent;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setContentPane(_);
        setSize(440,150);
        volverButton.addActionListener((e)->{
            this.dispose();
            parent.setVisible(true);
        });
    }
}
