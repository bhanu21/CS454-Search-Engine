package lucenePOC;


import java.io.*;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


/*
* Date Author Changes April 14, 2012 Kasun Perera Created
*/

/*
*
* Class contains methods for indexing documents with Lucene, and    calculating
* TFIDF weights
*/
public class newPOC {

private ArrayList<String> docNames=new ArrayList<String>();
private String docIDS[];
private String pathToIndex;
private String pathToDocumentCollection;
private String fiboTermList[]; //marked up fibo terms
private String taxoTermList[]; // marked up taxonomy terms
private RAMDirectory ramMemDir;
private String fileNames[];
private byte files[][];
private String filesInText[];
int filecount=6024;
int noOfWordsOfDOc[];
int noOfSentencesOfDoc[];
ArrayList<String> ArrLstSentencesOfDoc[];
String removedTermsOfDOc[][];
int freqAfterRemovalOfDoc[][];
//int queryDocIndex ;
private int curDocNo;
private final int maxTerms = 1000000;

static List<File> file_s = new ArrayList();


/**
 * Constructor used when indexing directory is a RAM memory directory, We
 * need RAM directory because Stratoes Server dosen't allow access local
 * files
 *
 * @param pathToIndex- doc index path 
 * @param pathToDocumentCollection - doccollection path
 */
public newPOC(String pathToIndex, String pathToDocumentCollection) {
    this.docNames = docNames;

    //this.bufPathToIndex= new RandomAccessBuffer() ;
  //  this.ramMemDir = new RAMDirectory();
    this.pathToIndex = pathToIndex;
    this.pathToDocumentCollection= pathToDocumentCollection;
    // this.files = files;
   // this.filesInText = docContent;

}




/**
 * Count the number of words in a given String
 *
 * @param line- Input String
 * @return - number of words in the input String
 */
private int wordCount(String line) {
    int numWords = 0;
    int index = 0;
    boolean prevWhiteSpace = true;
    while (index < line.length()) {
        char c = line.charAt(index++);
        boolean currWhiteSpace = Character.isWhitespace(c);
        if (prevWhiteSpace && !currWhiteSpace) {
            numWords++;
        }
        prevWhiteSpace = currWhiteSpace;
    }
    return numWords;
}

/*
*given it's URL this methods read the text files
*/
public static String fileReader(String filename) throws IOException {

    String filetext = null;
    BufferedReader reader = null;
    //BufferedReader namesReader; //reader for followers
    //Extractor extractor = new Extractor();
    File inFile = new File(filename);
    //File namesFile = new File(args[1]); //get followers file 
    //File userFile = new File(args[1]);

    //READING FROM USERS FILE
    reader = new BufferedReader(new FileReader(inFile));
    String line = null;

    int numLine = 0;

    while ((line = reader.readLine()) != null) {
        // numLine++;
        filetext = filetext + " " + line;

        // System.out.println(line);
    }

    reader.close();
    return filetext;
}

/**
 * Method to index the documents only using the content of the document
 * "docid" field is used for indexing, since Lucene Dosen't retrieve the
 * documents in the indexed order 
 *
 * @param docNo- document number of the document to be indexed
 * @throws IOException
 * @throws ParseException 
 */
 public void indexDocs() throws IOException, ParseException {
    //String pathToDocumentCollection = "F:\\karsha project\\Term Analysis\\keygraph docs\\selected_section_collection\\compelete_collection_2\\msrb_fibo_stopwords_replaced_term_docs\\";
   // String pathToIndex = "F:\\karsha project\\Term Analysis\\keygraph docs\\selected_section_collection\\compelete_collection_2\\INDEX_msrb_fibo_stopwords_replaced_term_docs";
   /* File folder = new File(pathToDocumentCollection);
    File[] listOfFiles = folder.listFiles();
    int noOfFiles = listOfFiles.length;
    System.out.println("Number of files : " + noOfFiles);
*/
    IndexWriter iW;
    int indexDocCount = 0;
    try {
        NIOFSDirectory dir = new NIOFSDirectory(new File(pathToIndex));
        iW =new IndexWriter(dir, new IndexWriterConfig(Version.LUCENE_34, new WhitespaceAnalyzer(Version.LUCENE_34)));

        List<File> files =list(new File(pathToDocumentCollection));
        filecount=files.size();
        for (File f:files ) {
            if (f.isFile()) {
                String docName = f.getName();
                System.out.println("doc name: " + docName + "length - " + f.length());
                docNames.add(docName);
                if (f.length() > 1) {
                    String filesInText = fileReader(f.getPath());

                    //docIds[i] = docNames[i].substring( 0, docName.length() - 4 );
                    System.out.println("Added to index : " + docName);

                    //  StringReader strRdElt = new StringReader(filesInText[i]);
                    //filesInText = filesInText.replaceAll( "[^A-Za-z_]", " " );
                    //System.out.println( "Added to index : " + docName );
                    StringReader strRdElt = new StringReader(filesInText.replaceAll("\\d+(?:[.,]\\d+)*\\s*", ""));
                    StringReader docId = new StringReader(docName.substring(0, docName.length() - 4)); // give a unique doc Id here

                    org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();

                    doc.add(new Field("doccontent", strRdElt, Field.TermVector.YES));
                    doc.add(new Field("docid", docId, Field.TermVector.YES));
                    iW.addDocument(doc);
                    indexDocCount++;
                }
            }
        }

        System.out.println("no of documents added to index : " + indexDocCount);

        iW.close();
       
        // dir.close() ;
    } catch (CorruptIndexException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

 public static List<File> list(File file) {
	    
		if(!file.isDirectory())
			file_s.add(file);	
		else{
		File[] children = file.listFiles();
	    for (File child : children) {
	        list(child);
	    }
		}
		return file_s;
	}

/**
 * This method calculates the TF-IDF score for each terms in the indexed
 * documents
 *
 * @param numberOfDocs
 * @return - Hashmap of TF-IDF score per each term in document wise
 * @throws CorruptIndexException
 * @throws ParseException
 */
public HashMap<String, HashMap> tfIdfScore(int numberOfDocs) throws CorruptIndexException, ParseException {

    //int noOfDocs = docNames.size();

    HashMap<String, HashMap> scoreMap = new HashMap<String, HashMap>();
    //HashMap<Integer, float[]> scoreMap = new HashMap<Integer, float[]>();


    try {

        IndexReader re = IndexReader.open(NIOFSDirectory.open(new File(pathToIndex))) ;
       // IndexReader re = IndexReader.open(ramMemDir);

        int i = 0;
        for (int k = 0; k < numberOfDocs; k++) {
            int freq[];
            TermFreqVector termsFreq;
            TermFreqVector termsFreqDocId;
            //TermFreqVector termsFreq3[];
            HashMap<String, Float> wordMap = new HashMap<String, Float>();
            String terms[];
            float score[] = null;

            //termsFreq3=re.getTermFreqVectors(currentDocID);
            termsFreq = re.getTermFreqVector(k, "doccontent");
            termsFreqDocId = re.getTermFreqVector(k, "docid");

	
	
            String aInt =termsFreqDocId.getTerms()[0];
            freq = termsFreq.getTermFrequencies();

            terms = termsFreq.getTerms();

            int noOfTerms = terms.length;
            score = new float[noOfTerms];
            DefaultSimilarity simi = new DefaultSimilarity();
            for (i = 0; i < noOfTerms; i++) {
                int noofDocsContainTerm = re.docFreq(new Term("doccontent", terms[i]));
                // System.out.println(terms[i]+"\t"+freq[i]);
                //int noofDocsContainTerm = docsContainTerm(terms[i], "docnames");
                float tf = simi.tf(freq[i]);
                float idf = simi.idf(noofDocsContainTerm, filecount);
                wordMap.put(terms[i], (tf * idf));

            }
            scoreMap.put(aInt, wordMap);
            

        }


    } catch (IOException e) {
        // score = null;
        e.printStackTrace();
    }



    //Map<Integer,Float[]> scoreMap=new Map<Integer, Float[]>(); 


    return scoreMap;
}


public HashMap<String, HashMap> getTFIDF() throws IOException, CorruptIndexException, ParseException, ClassNotFoundException {
  //  int noOfDocs = docNames.size();
    float tfIdfScore[][] = new float[filecount][];
    //HashMap<Integer, float[]> scoreMap = new HashMap<Integer, float[]>();
    HashMap<String, HashMap> scoreMap = new HashMap<String, HashMap>();


    scoreMap = tfIdfScore(filecount);




    return  scoreMap;
}
public static void main(String args[]) throws IOException, ClassNotFoundException, ParseException
	{
		newPOC poc=new newPOC("c:/test1/","C:/Temp/en/");
		//poc.indexDocs();
		System.out.println(poc.getTFIDF());
		
	}

}