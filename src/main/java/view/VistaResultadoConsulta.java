package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.awt.Desktop;
import java.net.URI;

class VistaResultadoConsulta extends JFrame{
    private JTable table1;
    private JButton volverButton;
    private JButton páginaAnteriorButton;
    private JPanel _pane;
    private JComboBox numeroPágina;
    private JButton siguientePáginaButton;
    private String[][]datos;
    private String[]headers={"Pos","Titulo","Resumen","Similitud","URL"};
    private final int TAMAÑO_PAGINA=20;

    private class TableModel extends DefaultTableModel{
        public TableModel (String[][]d,String[]h){
            super(d,h);
        }
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
    }

    public VistaResultadoConsulta(String[][]data){
        super("Resultado de la consulta");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
        setContentPane(_pane);
        setSize(540,465);
        volverButton.addActionListener((e)->this.dispose());
        datos=data;
        numeroPágina.setModel(new DefaultComboBoxModel(IntStream.range(1,datos.length/TAMAÑO_PAGINA+2).boxed().toArray()));
        numeroPágina.addActionListener(e->{
            int from=numeroPágina.getSelectedIndex()*TAMAÑO_PAGINA;
            table1.setModel(new TableModel(Arrays.copyOfRange(datos,from,from+TAMAÑO_PAGINA),headers));
        });
        siguientePáginaButton.addActionListener(e -> {
            int paginaActual=numeroPágina.getSelectedIndex();
            if (paginaActual<numeroPágina.getItemCount()){
                numeroPágina.setSelectedIndex(paginaActual+1);
            }
        });
        páginaAnteriorButton.addActionListener(e -> {
            int paginaActual=numeroPágina.getSelectedIndex();
            if (paginaActual>0){
                numeroPágina.setSelectedIndex(paginaActual-1);
            }
        });
        table1.setModel(new TableModel(Arrays.copyOf(data,TAMAÑO_PAGINA), headers));
        table1.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent evnt) {
                if (evnt.getClickCount() > 1)
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(new File(table1.getValueAt(table1.getSelectedRow(), 4).toString()).toURI());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        });
    }
}
