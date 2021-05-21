package view;

import lucene4ir.IndexerApp;

import javax.swing.*;
import static javax.swing.JOptionPane.showMessageDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.regex.*;


public class VistaIndexador extends JFrame {
    private JTextField indexTextField;
    private JTextField textField2;
    private JButton indexarButton;
    private JButton volverButton;
    private JPanel _pane;
    private JButton borrarIndiceButton;

    private void indexar(){
        File directorioAIndexar=new File(textField2.getText());
        //Variable para validar que la ruta está bien y es directorio
        //Valida que la ruta contenga "Geografía" para limitarnos al repositorio dado
        if (directorioAIndexar.isDirectory()&&textField2.getText().contains("Geografia")){
            try{//Intenta correr el indexador
                IndexerApp.run(indexTextField.getText(),textField2.getText());
                showMessageDialog(this, "Operación finalizada con éxito.","Éxito",JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e){
                showMessageDialog(this,"Error inesperado en la indización.","Error",JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            showMessageDialog(this,"Error, ruta del directorio errónea.","Error",JOptionPane.ERROR_MESSAGE);
            textField2.setText("");
        }
    }

    public VistaIndexador(JFrame parent){
        super("Crear o incrementar un índice");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setContentPane(_pane);
        setSize(440,150);
        indexarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                indexar();
            }
        });
        volverButton.addActionListener((e)->{
            this.dispose();
            parent.setVisible(true);
        });
        indexTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(Pattern.compile("[a-zA-Z0-9_]").matcher(String.valueOf(e.getKeyChar())).find()){
                    super.keyTyped(e);
                } else {
                    e.consume();
                }
            }
        });
        borrarIndiceButton.addActionListener(new ActionListener() {
            void deleteDir(File file) {
                File[] contents = file.listFiles();
                if (contents != null) {
                    for (File f : contents) {
                        deleteDir(f);
                    }
                }
                file.delete();
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteDir(new File(indexTextField.getText()));
            }
        });
    }
}
