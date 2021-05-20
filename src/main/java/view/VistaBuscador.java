package view;

import lucene4ir.RetrievalApp;

import javax.swing.*;

public class VistaBuscador extends JFrame{
    private JTextField queryTextField;
    private JButton consultarButton;
    private JButton volverButton;
    private JPanel _pane;
    private JTextField indexTextField;

    private void consultar(){
        String consulta= queryTextField.getText();
        String[][]datos=RetrievalApp.run(indexTextField.getText(),consulta);
        new VistaResultadoConsulta(datos);
    }

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
        consultarButton.addActionListener((e)->{
            consultar();
        });
    }
}
