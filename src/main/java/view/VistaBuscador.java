package view;

import javax.swing.*;

public class VistaBuscador extends JFrame{
    private JTextField textField1;
    private JButton consultarButton;
    private JButton volverButton;
    private JPanel _;
    private JFrame parent;
    public VistaBuscador(JFrame parent){
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
