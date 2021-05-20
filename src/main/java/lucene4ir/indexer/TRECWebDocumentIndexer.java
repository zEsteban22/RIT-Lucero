package lucene4ir.indexer;

import lucene4ir.Analizadores;
import lucene4ir.Limpiador;
import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

/**
 * Indexer for TRECWEB test collections relying on JSOUP.
 *
 * Created by kojayboy 28/07/2017.
 */
public class TRECWebDocumentIndexer extends DocumentIndexer {

    private Analyzer analizador;

    private TextField textField;
    private TextField refField;
    private TextField titleField;
    private TextField resumenField;
    private TextField urlField;



    private Document doc;

    private static String [] contentTags = {
            "HTML"
    };
    private static String [] titleTags = {
            "title"
    };

    public TRECWebDocumentIndexer(String indexPath, String tokenFilterFile, boolean pos){
        super(indexPath, tokenFilterFile, pos);
        doc = new Document();
        initFields();
        initWebDoc();

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
        analizador = new Analyzer() {
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
    }

    private void initFields() {
        /*
    }
        docnumField = new StringField(Lucene4IRConstants.FIELD_DOCNUM, "", Field.Store.YES);
        if(indexPositions){
            titleField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_TITLE, "", Field.Store.YES);
            textField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_CONTENT, "", Field.Store.YES);
            allField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_ALL, "", Field.Store.YES);
            urlField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_URL, "", Field.Store.YES);
            dochdrField = new TermVectorEnabledTextField(Lucene4IRConstants.FIELD_DOCHDR, "", Field.Store.YES);
        }
        else {*/
        titleField = new TextField("Titulo", "", Field.Store.YES);
        textField = new TextField("Texto", "", Field.Store.YES);
        refField = new TextField("Ref", "", Field.Store.YES);
        urlField = new TextField("Url", "", Field.Store.YES);
        resumenField = new TextField("Resumen", "", Field.Store.YES);
        //}
    }

    private void initWebDoc() {
        doc.add(titleField);
        doc.add(textField);
        doc.add(refField);
        doc.add(resumenField);
        doc.add(urlField);
    }

    public Document createTRECWebDocument(String texto, String ref, String resumen, String title, String url) throws IOException{
        doc.clear();

        texto = Limpiador.limpiadorAcentos(texto);
        ref = Limpiador.limpiadorAcentos(ref);
        title = Limpiador.limpiadorAcentos(title);

        StringBuilder nuevoTexto = new StringBuilder();

        TokenStream streamRaices = analizador.tokenStream("Texto", texto);

        OffsetAttribute offsetAttribute = streamRaices.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = streamRaices.addAttribute(CharTermAttribute.class);

        streamRaices.reset();
        while (streamRaices.incrementToken()) {
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            nuevoTexto.append(charTermAttribute.toString() + ' ');
        }
        streamRaices.close();
        texto = nuevoTexto.toString();

        titleField.setStringValue(title);
        textField.setStringValue(texto);
        refField.setStringValue(ref);
        resumenField.setStringValue(resumen);
        urlField.setStringValue(url);

        doc.add(titleField);
        doc.add(textField);
        doc.add(refField);
        doc.add(resumenField);
        doc.add(urlField);
        return doc;
    }

    public void indexDocumentsFromFile(String filename){

        File input = new File(filename);
        try {
            org.jsoup.nodes.Document documentoHTML = Jsoup.parse(input, "UTF-8", "");

            StringBuilder constructorBody = new StringBuilder();
            StringBuilder constructorRef = new StringBuilder();
            StringBuilder constructorTitulo = new StringBuilder();

            ListIterator<Element> iteradorBody = documentoHTML.getElementsByTag("p").listIterator();
            while (iteradorBody.hasNext())
                constructorBody.append(iteradorBody.next().text()).append(" ");

            ListIterator<Element> iteradorRef = documentoHTML.getElementsByTag("a").listIterator();
            while (iteradorRef.hasNext())
                constructorRef.append(iteradorRef.next().text()).append(" ");

            ListIterator<Element> iteradorTitulo = documentoHTML.getElementsByTag("title").listIterator();
            while (iteradorTitulo.hasNext())
                constructorTitulo.append(iteradorTitulo.next().text()).append(" ");

            String title = constructorTitulo.toString();
            String texto = constructorBody.toString();
            String ref = constructorRef.toString();
            String resumen = texto.substring(0, 200);

            title = Limpiador.limpiadorAcentos(title);

            createTRECWebDocument(texto, ref, resumen, title, filename);
            addDocumentToIndex(doc);
            //text = new StringBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}