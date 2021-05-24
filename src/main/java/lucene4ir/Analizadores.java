package lucene4ir;

import lucene4ir.indexer.Tokenizador;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.CharArraySet;
//import org.apache.lucene.analysis.LowerCaseFilter;

public class Analizadores {
    public Analyzer analizadorBody, analizadorRef;

    public Analizadores(){
        ArrayList<String> stopWordsList = new ArrayList<String>();

        try {
            BufferedReader in = new BufferedReader(new FileReader("data\\stopwords.txt"));
            String str;

            while ((str = in.readLine())!= null) {
                stopWordsList.add(str);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }

        CharArraySet stopWords = new CharArraySet(stopWordsList, false);

        analizadorBody = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String s) {
                Tokenizador tokenizador = new Tokenizador();
                TokenStream filtros = new LowerCaseFilter(tokenizador);
                filtros = new StopFilter(filtros, stopWords);
                SpanishStemmer stemmerEspanol = new SpanishStemmer();
                filtros = new SnowballFilter(filtros, stemmerEspanol);
                return new TokenStreamComponents(tokenizador, filtros);
            }
        };

        analizadorRef = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                Tokenizador tokenizador = new Tokenizador();
                TokenStream filtros = new LowerCaseFilter(tokenizador);
                return new TokenStreamComponents(tokenizador, filtros);
            }
        };

    }

    public String realizarStemming(String texto){
        StringBuilder nuevoTexto = new StringBuilder();

        TokenStream streamRaices = analizadorBody.tokenStream("Texto", texto);

        OffsetAttribute offsetAttribute = streamRaices.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = streamRaices.addAttribute(CharTermAttribute.class);

        try {
            streamRaices.reset();

            while (streamRaices.incrementToken()) {
                int startOffset = offsetAttribute.startOffset();
                int endOffset = offsetAttribute.endOffset();
                nuevoTexto.append(charTermAttribute.toString() + ' ');
            }
            streamRaices.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nuevoTexto.toString();
    }

}
