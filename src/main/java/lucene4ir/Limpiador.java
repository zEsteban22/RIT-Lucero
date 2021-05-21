package lucene4ir;

import java.text.Normalizer;

public class Limpiador {
    public static String limpiarGeneral(String cadena){ return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[^A-Za-zÁÉÍÓÚÜáéíóúüÑñ]", ""); }
    public static String limpiadorAcentos(String cadena) { return Normalizer.normalize(cadena, Normalizer.Form.NFD).replaceAll("[\u0301\u0308]", ""); }
}
