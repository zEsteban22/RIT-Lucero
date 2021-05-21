package lucene4ir;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.search.similarities.LMSimilarity.CollectionModel;
import org.apache.lucene.store.FSDirectory;

import lucene4ir.similarity.SMARTBNNBNNSimilarity;
import lucene4ir.similarity.OKAPIBM25Similarity;
import lucene4ir.similarity.BM25LSimilarity;
import lucene4ir.similarity.BM25Similarity;
import lucene4ir.utils.TokenAnalyzerMaker;

import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.*;

import static lucene4ir.RetrievalApp.SimModel.BM25;
import static lucene4ir.RetrievalApp.SimModel.LMD;
import static lucene4ir.RetrievalApp.SimModel.PL2;

public class RetrievalApp {

    private final String indexName;
    public RetrievalParams p;

    protected Similarity simfn;
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected Analyzer analyzer;
    protected QueryParser parser;
    protected CollectionModel colModel;
    protected String fieldsFile;
    protected String qeFile;

    protected enum SimModel {
        DEF, BM25, BM25L, LMD, LMJ, PL2, TFIDF,
	OKAPIBM25, SMARTBNNBNN, DFR
    }

    protected SimModel sim;

    private void setSim(String val){
        try {
            sim = SimModel.valueOf(p.model.toUpperCase());
        } catch (Exception e){
            System.out.println("Similarity Function Not Recognized - Setting to Default");
            System.out.println("Possible Similarity Functions are:");
            for(SimModel value: SimModel.values()){
                System.out.println("<model>"+value.name()+"</model>");
            }
            sim = SimModel.DEF;
        }
    }

    public void selectSimilarityFunction(SimModel sim){
        colModel = null;
        switch(sim){
            case OKAPIBM25:
                System.out.println("OKAPI BM25 Similarity Function");
                simfn = new OKAPIBM25Similarity(1.2f, 0.75f);
                break;
            case SMARTBNNBNN:
                System.out.println("SMART bnn.bnn Similarity Function");
                simfn = new SMARTBNNBNNSimilarity();
            case BM25:
                System.out.println("BM25 Similarity Function");
                simfn = new BM25Similarity(p.k, p.b);
                break;
            case BM25L:
                System.out.println("BM25L Similarity Function");
                simfn = new BM25LSimilarity(p.k, p.b, p.delta);
                break;
            case LMD:
                System.out.println("LM Dirichlet Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMDirichletSimilarity(colModel, p.mu);
                break;

            case LMJ:
                System.out.println("LM Jelinek Mercer Similarity Function");
                colModel = new LMSimilarity.DefaultCollectionModel();
                simfn = new LMJelinekMercerSimilarity(colModel, p.lam);
                break;

            case PL2:
                System.out.println("PL2 Similarity Function (?)");
                BasicModel bm = new BasicModelP();
                AfterEffect ae = new AfterEffectL();
                Normalization nn = new NormalizationH2(p.c);
                simfn = new DFRSimilarity(bm, ae, nn);
                break;

            case DFR:
                System.out.println("DFR Similarity Function with no after effect (?)");
                BasicModel bmd = new BasicModelD();
                AfterEffect aen = new AfterEffect.NoAfterEffect();
                Normalization nh1 = new NormalizationH1();
                simfn = new DFRSimilarity(bmd, aen, nh1);
                break;

            default:
                System.out.println("Default Similarity Function");
                simfn = new BM25Similarity();

                break;
        }
    }

    public void readParamsFromFile(String paramFile){
        /*
        Reads in the xml formatting parameter file
        Maybe this code should go into the RetrievalParams class.

        Actually, it would probably be neater to create a ParameterFile class
        which these apps can inherit from - and customize accordinging.
         */
        System.out.println("Reading parameters...");
        try {
            p = JAXB.unmarshal(new File(paramFile), RetrievalParams.class);
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        setSim(p.model);

        if (p.maxResults==0.0) {p.maxResults=1000;}
        if (p.b < 0.0){ p.b = 0.75f;}
        if (p.beta <= 0.0){p.beta = 500f;}
        if (p.k <= 0.0){ p.k = 1.2f;}
        if (p.delta<=0.0){p.delta = 1.0f;}
        if (p.lam <= 0.0){p.lam = 0.5f;}
        if (p.mu <= 0.0){p.mu = 500f;}
        if (p.c <= 0.0){p.c=10.0f;}
        if (p.model == null){
            p.model = "def";
        }
        if (p.runTag == null){
            p.runTag = p.model.toLowerCase();
        }

        qeFile=p.qeFile;

        System.out.println("Path to index: " + indexName);
        System.out.println("Model: " + p.model);
        System.out.println("Max Results: " + p.maxResults);
        if (sim==BM25) {
            System.out.println("b: " + p.b);
            System.out.println("k: " + p.k);
        }
        else if (sim==PL2){
            System.out.println("c: " + p.c);
        }
        else if (sim==LMD){
            System.out.println("mu: " + p.mu);
        }
        if (p.qeFile!=null){
            System.out.println("QE File: " + p.qeFile);
        }

        if (p.tokenFilterFile != null){
            TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
            analyzer = tam.createAnalyzer(p.tokenFilterFile);
        }
        else{
            analyzer = Lucene4IRConstants.ANALYZER;
        }
    }
    public ScoreDoc[] runQuery(String consulta){
        ScoreDoc[] hits = null;
        try {
            Query query = parser.parse(QueryParser.escape(consulta));

            try {
                TopDocs results = searcher.search(query, p.maxResults);
                hits = results.scoreDocs;
            }
            catch (IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }
        } catch (ParseException pe){
            pe.printStackTrace();
            System.exit(1);
        }
        return hits;
    }

    public static String[][] run(String indexName,String consulta){
        RetrievalApp app=new RetrievalApp(indexName);
        String[][]datos;
        try {
            Query query = app.parser.parse(consulta);
            try {
                TopDocs results = app.searcher.search(query, Integer.MAX_VALUE);
                datos=new String[results.scoreDocs.length][5];
                for (int i=0;i<datos.length;i++){
                    Document d=app.searcher.doc(results.scoreDocs[i].doc);
                    datos[i][0]=Integer.toString(i+1);
                    datos[i][1]=d.get("Titulo");
                    datos[i][2]=d.get("Resumen");
                    datos[i][3]=Float.toString(results.scoreDocs[i].score);
                    datos[i][4]=d.get("Url");
                }
                return datos;
            }
            catch (IOException ioe){
                ioe.printStackTrace();
                System.exit(1);
            }
        } catch (ParseException pe){
            pe.printStackTrace();
            System.exit(1);
        }
        return null;
    }
    public RetrievalApp(String indexName){
        this.indexName=indexName;
        String retrievalParamFile="params/retrieval_params.xml";
        System.out.println("Retrieval App");
        System.out.println("Param File: " + retrievalParamFile);
        readParamsFromFile(retrievalParamFile);
        try {
            reader = DirectoryReader.open(FSDirectory.open( new File(indexName).toPath()) );
            searcher = new IndexSearcher(reader);

            // create similarity function and parameter
            selectSimilarityFunction(sim);
            searcher.setSimilarity(simfn);

            parser = new QueryParser("Texto", analyzer);

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public static void main(String []args) {

        String retrievalParamFile = "";

        try {
            retrievalParamFile = args[0];
        } catch (Exception e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }

        RetrievalApp retriever = new RetrievalApp(retrievalParamFile);
        //retriever.processQueryFile();
    }
}

@XmlRootElement(name = "RetrievalParams")
class RetrievalParams {
    public String model;
    public int maxResults;
    public float k;
    public float b;
    public float lam;
    public float beta;
    public float mu;
    public float c;
    public float delta;
    public String runTag;
    public String tokenFilterFile;
    public String qeFile;
}