package lucene4ir;


import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.indexer.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import jakarta.xml.bind.JAXB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * Created by leif on 21/08/2016.
 * Edited by kojayboy on 02/03/2017
 * Edited by Abdulaziz on 18/06/2019
 */

public class IndexerApp {

    public IndexParams p;

    public lucene4ir.indexer.DocumentIndexer di;



    private enum DocumentModel {
        CACM, CLUEWEB, TRECNEWS, TRECCC, TRECAQUAINT, TRECWEB, TRECTIPSTER, PUBMED , COMMONCORE
    }
    private final String DEFAULT_TOKENFILTERFILE="params/index/example_01.xml";

    private DocumentModel docModel;

    public IndexerApp(){
        System.out.println("Indexer");
    }

    private void setDocParser(String val){

        try {
            docModel = DocumentModel.valueOf(p.indexType.toUpperCase());
        } catch (Exception e){
            System.out.println("Document Parser Not Recognized - Setting to Default");
            System.out.println("Possible Document Parsers are:");
            for(DocumentModel value: DocumentModel.values()){
                System.out.println("<indexType>"+value.name()+"</indexType>");
            }
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void selectDocumentParser(DocumentModel dm){
        docModel = dm;
        di = null;
        switch(dm){
            case CACM:
                System.out.println("CACM Document Parser");
                di = new CACMDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            case CLUEWEB:
                System.out.println("CLUEWEB Document Parser");
                // TBA
                System.out.println("To be Implemented");
                // di = new CLUEWEBDocumentIndexer(p.indexName);
                System.exit(1);
                break;

            case TRECNEWS:
                System.out.println("TRECNEWS");
                di = new TRECNEWSDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            case TRECCC:

                System.out.println("TRECCC");
                di = new TRECCCDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            case TRECTIPSTER:
                System.out.println("TRECTIPSTER");
                di = new TRECTipsterDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            case TRECAQUAINT:
                System.out.println("TRECAQUAINT");
                di = new TRECAquaintDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            case TRECWEB:
                System.out.println("TRECWeb");
                di = new TRECWebDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            case PUBMED:
                System.out.println("PUBMED");
                di = new PubMedDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;
            case COMMONCORE:
                System.out.println("COMMON CORE");
                di = new CommonCoreDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
                break;

            default:
                System.out.println("Default Document Parser");

                break;
        }
    }




    public ArrayList<String> readFileListFromFile(){
        /*
            Takes the name of a file (filename), which contains a list of files.
            Returns an array of the filenames (to be indexed)
         */

        String filename = p.fileList;

        ArrayList<String> files = new ArrayList<String>();
        File aFile;
        CrossDirectoryClass cd = new CrossDirectoryClass(filename);

        aFile = new File(filename);
        if (aFile.isDirectory())
          files =  cd.crossDirectory();
        else
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {
                String line = br.readLine();
                while (line != null){
                    files.add(line);
                    line = br.readLine();
                }

            } finally {
                br.close();
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return files;
    }

    public void readIndexParamsFromFile(String indexParamFile){
        try {
            p = JAXB.unmarshal(new File(indexParamFile), IndexParams.class);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        if(p.recordPositions==null)
            p.recordPositions=false;

        System.out.println("Index type: " + p.indexType);
        System.out.println("Path to index: " + p.indexName);
        System.out.println("List of files to index: " + p.fileList);
        System.out.println("Record positions in index: " + p.recordPositions);

    }

    public IndexerApp(String indexParamFile){
        System.out.println("Indexer App");
        readIndexParamsFromFile(indexParamFile);
        setDocParser(p.indexType);
        selectDocumentParser(docModel);
    }

    public IndexerApp(String indexName, String fileList){
        p=new IndexParams();
        p.fileList=fileList;
        p.indexName=indexName;
        p.indexType="TRECWEB";
        p.tokenFilterFile=DEFAULT_TOKENFILTERFILE;
        p.recordPositions=true;
        setDocParser(p.indexType);
        selectDocumentParser(docModel);
    }

    public void indexDocumentsFromFile(String filename){
        di.indexDocumentsFromFile(filename);
    }

    public void finished(){
        try {
            di.writer.forceMerge(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        di.finished();
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(p.indexName)));
            long numDocs = reader.numDocs();
            System.out.println("Cantidad de documentos indexados: " + numDocs);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void run(String indexName,String fileList)throws Exception{
        IndexerApp app=new IndexerApp(indexName,fileList);
        try {
            for (String f : app.readFileListFromFile()) {
                System.out.println("Indexando archivo: " +  f);
                app.indexDocumentsFromFile(f);
            }
        } catch (Exception e){
            throw e;
        }
        app.finished();
    }

    public static void main(String []args) {


        String indexParamFile = "";

        try {
            indexParamFile = args[0];
        } catch(Exception e){
            e.printStackTrace();
            System.exit(1);
        }

        IndexerApp indexer = new IndexerApp(indexParamFile);

        try {
            ArrayList<String> files = indexer.readFileListFromFile();
            for (String f : files) {
                System.out.println("About to Index Files in: " +  f);
                indexer.indexDocumentsFromFile(f);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        indexer.finished();

        //File indexdir = new File("index") ; // location of my index
        try {
            Directory directory = FSDirectory.open(Paths.get("index"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        IndexReader ireader = null; //ERROR NoSuchMethodError
        try {
            ireader = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
        } catch (IOException e) {
            e.printStackTrace();
        }
//IndexReader ireader = IndexReader.open(directory); //variation ERROR NoSuchMethodError
        IndexSearcher isearcher = new IndexSearcher(ireader);

        System.out.println("Done building Index");


    }

}


class IndexParams {
    public String indexName;
    public String fileList;
    public String indexType; /** trecWeb, trecNews, trec678, cacm **/
    //public Boolean compressed;
    public String tokenFilterFile;
    public Boolean recordPositions;

}

