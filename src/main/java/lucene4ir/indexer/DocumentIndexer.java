package lucene4ir.indexer;

import lucene4ir.Analizadores;
import lucene4ir.Limpiador;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import lucene4ir.Lucene4IRConstants;
import lucene4ir.utils.TokenAnalyzerMaker;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;

import org.apache.lucene.analysis.*;
//import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.beans.Customizer;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

/**
 * Created by leifos on 21/08/2016.
 * Edited by kojayboy on 16/08/2017.
 * Edited by Leifos 10/9/2017
 * Added extra method openDocumentFile that can handle different input file types
 * i.e. compressed (gz, etc) and creates the appropriate input reader
 * probably should re-factor class to provide a templated method with the BufferredReader to process for each file
 * and not the file itself.
 */
public class DocumentIndexer {

    public IndexWriter writer;
    public Analizadores analizadores;

    public DocumentIndexer(){};

    public DocumentIndexer(String indexPath){

        analizadores = new Analizadores();

        try {
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            System.out.println("Indexing to directory '" + indexPath + "'...");

            IndexWriterConfig iwc = new IndexWriterConfig(analizadores.analizadorRef);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            //iwc.setCodec(new SimpleTextCodec());
            writer = new IndexWriter(dir, iwc);

        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void anadirDocumentoIndex(Document doc){
        try {
            writer.addDocument(doc);
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Document crearDocumento(String texto, String ref, String resumen, String title, String url) throws IOException{

        texto = Limpiador.limpiadorAcentos(texto);
        ref = Limpiador.limpiadorAcentos(ref);
        title = Limpiador.limpiadorAcentos(title);

        texto = analizadores.realizarStemming(texto);

        TextField titleField = new TextField("Titulo", title, Field.Store.YES);
        TextField textField = new TextField("Texto", texto, Field.Store.YES);
        TextField refField = new TextField("Ref", ref, Field.Store.YES);
        TextField resumenField = new TextField("Resumen", resumen, Field.Store.YES);
        TextField urlField = new TextField("Url", url, Field.Store.YES);

        Document doc = new Document();

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

            String titulo = constructorTitulo.toString();
            String texto = constructorBody.toString();
            String ref = constructorRef.toString();
            String resumen = texto.substring(0, 200);

            Document doc = crearDocumento(texto, ref, resumen, titulo, filename);
            anadirDocumentoIndex(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finished(){
        try {
            if (writer != null){
                writer.close();
            }
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

}
