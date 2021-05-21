package lucene4ir;

import lucene4ir.indexer.Tokenizador;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.tartarus.snowball.ext.SpanishStemmer;
//import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.CharArraySet;
//import org.apache.lucene.analysis.LowerCaseFilter;

public class Analizadores {
    public Analyzer analizadorSSW, analizadorSnowball;

    public Analizadores(CharArraySet stopWords){
        analizadorSSW = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String s) {
                Tokenizador tokenizador = new Tokenizador();
                TokenStream filtros = new LowerCaseFilter(tokenizador);
                filtros = new StopFilter(filtros, stopWords);
                return new TokenStreamComponents(tokenizador, filtros);
            }
        };

    }

}
