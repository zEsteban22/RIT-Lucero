package view;

import javax.swing.*;

public class VistaBuscador extends JFrame{
    private JTextField textField1;
    private JButton consultarButton;
    private JButton volverButton;
    private JPanel _pane;

    public VistaBuscador(JFrame parent){
        super("Realizar consulta en un índice");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setContentPane(_pane);
        setSize(440,150);
        volverButton.addActionListener((e)->{
            this.dispose();
            parent.setVisible(true);
        });
    }
}
