package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

/**
 * Indexer for TRECWEB test collections relying on JSOUP.
 *
 * Created by kojayboy 28/07/2017.
 */
public class TRECWebDocumentIndexer extends DocumentIndexer {


    private Field textField;
    private Field refField;
    private Field titleField;
    private Field resumenField;
    private Field urlField;
    //private Field dochdrField;
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
        /*
        StringBuilder constructor = new StringBuilder();
        String palabra = "";
        TokenStream tSTexto = analyzer.tokenStream("cuerpo", texto);
        CharTermAttribute caracter = tSTexto.addAttribute(CharTermAttribute.class);
        tSTexto.reset();
        //while (tSTexto.incrementToken()){
        //    constructor.append(caracter.toString()).append(" ");
        //}
        //List<String> palabra = analyze();
        //String resultado = constructor.toString();
         */


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
        //System.out.println("Adding page: "+ url + " #"  + docid + " Title: " + title);
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

            createTRECWebDocument(texto, ref, resumen, title, filename);
            addDocumentToIndex(doc);
            //text = new StringBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}