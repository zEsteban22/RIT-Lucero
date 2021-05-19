package view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class View extends JFrame {
    private JButton buscadorButton;
    private JButton indexadorButton;
    private JPanel _pane;

    public View (){
        super("Information retrieval with Lucene.");
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(_pane);
        setSize(440,150);
        indexadorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new VistaIndexador(View.this);
                setVisible(false);
            }
        });
        buscadorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new VistaBuscador(View.this);
                setVisible(false);
            }
        });
    }
}
