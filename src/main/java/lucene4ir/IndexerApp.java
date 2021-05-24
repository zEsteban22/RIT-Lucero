package lucene4ir;


import lucene4ir.utils.CrossDirectoryClass;
import lucene4ir.indexer.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
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

    public IndexerApp(){
        System.out.println("Indexer");
    }


    public void selectDocumentParser(){
        di = new DocumentIndexer(p.indexName);//TRECWebDocumentIndexer(p.indexName, p.tokenFilterFile, p.recordPositions);
    }

    public ArrayList<String> readFileListFromFile(String filename){
        /*
            Takes the name of a file (filename), which contains a list of files.
            Returns an array of the filenames (to be indexed)
         */

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

    public IndexerApp(String indexName, String fileList){
        p=new IndexParams();
        p.fileList=fileList;
        p.indexName=indexName;
    }

    public void indexDocumentsFromFile(String filename){
        di.indexDocumentsFromFile(filename);
    }

    public static void run(String indexName,String fileList)throws Exception{
        IndexerApp app=new IndexerApp(indexName,fileList);
        try {
            for (String f : app.readFileListFromFile(fileList)) {
                System.out.println("Indexando archivo: " +  f);
                app.indexDocumentsFromFile(f);
            }
        } catch (Exception e){
            throw e;
        }
        app.di.finished();
    }
/*
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

        QueryParser parser = new QueryParser("texto", new StandardAnalyzer());
        try {
            Query query = parser.parse("y");
            try {
                TopDocs resultados = isearcher.search(query, 1);
                int i = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            int i = 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("Done building Index");


    }
*/
}


class IndexParams {
    public String indexName;
    public String fileList;
    public String indexType; /** trecWeb, trecNews, trec678, cacm **/
    //public Boolean compressed;
    public String tokenFilterFile;
    public Boolean recordPositions;

}

