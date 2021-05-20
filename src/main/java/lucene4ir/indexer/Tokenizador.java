package lucene4ir.indexer;

import org.apache.lucene.analysis.util.CharTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizador extends CharTokenizer {
    Pattern characteres = Pattern.compile("[A-Za-zÁÉÍÓÚÜáéíóúüÑñ]");

    @Override
    protected boolean isTokenChar(int i){
        Matcher encuentros = characteres.matcher(String.valueOf((char) i));
        return encuentros.matches();
    }
}