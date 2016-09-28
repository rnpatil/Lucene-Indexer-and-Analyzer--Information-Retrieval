import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;


public class indexComparision {

	private static String TEXT = "TEXT";
	private static ArrayList<String> tags= new ArrayList<String>();
	private static String fileType ="trectext";

	// main method where the object for the generateIndex class is instantiated
	public static void main(String[] args) throws Exception {


		// Please change all the directory paths to your local directory path before executing.

		// this is the directory path to the corpus
		File dataDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\Data\\corpus\\corpus\\");

		Scanner reader = new Scanner(System.in); 
		System.out.println("Index Comparision : ");
		System.out.println("Select amongst the following Anayzers : ");
		System.out.println(" 1: KeywordAnalyzer \n 2: SimpleAnalyzer \n 3: StopAnalyzer \n 4: StandardAnalyzer ");

		int task = reader.nextInt();
		reader.close();
		Analyzer analyzer= null;

		// this object will call the index method to generate the indexing
		indexComparision indexObj = new indexComparision();

		switch(task)
		{
		case 1: 
			analyzer = new KeywordAnalyzer();
			File keywordindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\KeyWordIndex\\");
			indexObj.process(keywordindexDirectory, dataDirectory,analyzer);
			System.out.println("Analyzing Keyword Index");
			indexObj.indexStats(keywordindexDirectory);
			analyzer.close();
			break;
		case 2:
			File simpleindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\SimpleIndex\\");
			analyzer = new SimpleAnalyzer();
			indexObj.process(simpleindexDirectory, dataDirectory,analyzer);
			System.out.println("Analyzing Simple index");
			indexObj.indexStats(simpleindexDirectory);
			analyzer.close();
			break;
		case 3:
			File stopwordindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\StopWordIndex\\");
			analyzer = new StopAnalyzer();
			indexObj.process(stopwordindexDirectory, dataDirectory,analyzer);
			System.out.println("Analyzing Stopword index:");
			indexObj.indexStats(stopwordindexDirectory);
			analyzer.close();
			break;
		case 4: 
			File standardindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\StandardIndex\\");
			analyzer = new StandardAnalyzer();
			indexObj.process(standardindexDirectory, dataDirectory,analyzer);
			System.out.println("Analyzing Standard index");
			indexObj.indexStats(standardindexDirectory);
			analyzer.close();
			break;
		default: 
			System.out.println("Invalid Input");
			break;
		}
	}

	/**
	 * 
	 * 
	 * @param indexDirectory
	 * @param dataDirectory
	 * @param indexanalyzer
	 * @throws Exception
	 */
	private void process(File indexDirectory, File dataDirectory,Analyzer indexanalyzer)
			throws Exception {
		Directory dir = FSDirectory.open(Paths.get(String.valueOf(indexDirectory)));
		IndexWriterConfig iwc = new IndexWriterConfig(indexanalyzer);
		iwc.setOpenMode(OpenMode.CREATE);

		// to write the documents to the index
		IndexWriter writer = new IndexWriter(dir, iwc);

		// Call to Index Creation Logic
		createIndex(writer, dataDirectory);
		writer.forceMerge(1);
		writer.commit();

		System.out.println("Index Commited");
		writer.close();
		System.out.println("Writer Closed");

	}


	// This method reads the index file and gives stats about the index
	/**
	 * 
	 * 
	 * @param indexDirectory
	 * @throws Exception
	 */
	private void indexStats(File indexDirectory)throws Exception
	{

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(String.valueOf(indexDirectory))));

		/**
		 * 
		 * Total Number of Documents in the Corpus
		 */
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());

		/**
		 * 
		 * Stop Words Check
		 */
		//Print the number of documents containing the term "was" in <field>TEXT</field>.
		System.out.println("Number of occurrences of \"was\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","was")));

		//Print the number of documents containing the term "on" in <field>TEXT</field>.
		System.out.println("Number of occurrences of \"on\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","on")));


		//Print the number of documents containing the term "is" in <field>TEXT</field>.
		System.out.println("Number of occurrences of \"is\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","is")));


		/**
		 * 
		 * Total Number of Tokens for the TEXT tag
		 */
		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");

		System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());


		//Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.

		/**
		 * Total Number of terms in the dictionary/vocabulary
		 * 
		 */
		System.out.println("Size of the vocabulary for this field: "+vocabulary.size());


		/**
		 * Check if TEXT field has terms like "very","happy" and are they stemmed to "veri" and "happi"
		 * 
		 * 
		 */
		//Print the vocabulary for <field>TEXT</field>
		TermsEnum iterator = vocabulary.iterator();
		BytesRef byteRef = null;
		System.out.println("\n*******Vocabulary-Start**********");
		while((byteRef = iterator.next()) != null) {
			String term = byteRef.utf8ToString();

			if(term.equals("veri")||term.equals("very")||term.equals("happy")||term.equals("happi"))
				System.out.print(term+"\t");
		}
		System.out.println("\n*******Vocabulary-End**********");

		/**
		 * 
		 * Oberservation : No stemming applied for any for the 4 analyzers
		 */
		reader.close();


	}

	/**
	 * 
	 * @param writer
	 * @param dataDirectory
	 * @throws Exception
	 */
	private void createIndex(IndexWriter writer, File dataDirectory) throws Exception {

		tags.add(0,TEXT);

		File[] listFiles = dataDirectory.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (!listFiles[i].isDirectory() && !listFiles[i].isHidden() &&
					listFiles[i].canRead() && listFiles[i].exists() && 
					FilenameUtils.getExtension(listFiles[i].getName()).equals(fileType)) {

				System.out.println("Indexing going on with file : "+ listFiles[i].getCanonicalPath());
				String fileContents = new String(Files.readAllBytes(Paths.get(listFiles[i].getCanonicalPath())),StandardCharsets.UTF_8);
				String[] documents = fileContents.split("</DOC>\n");
				String content = null;

				StringBuffer contentBuffer = new StringBuffer();
				// Iterate for all documents in the current file

				for (String doc : documents) {
					Document document = new Document();
					for (int tag = 0; tag < tags.size(); tag++) {
						String tagsContent = "";
						int startPosition = 0;
						int endPosition= 0;

						//Merge content from multiple elements if they all bear the same tag.

						while ((startPosition = doc.indexOf("<" + tags.get(tag)+">", startPosition)) != -1) {
							startPosition += tags.get(tag).length() + 2;  // Move to the endPosition of tag
							endPosition = doc.indexOf("</" + tags.get(tag)+ ">", startPosition);
							content = doc.substring(startPosition,endPosition);
							contentBuffer.append(content+" ");
							startPosition += content.length();
						}
						tagsContent = contentBuffer.toString().trim();
						contentBuffer.setLength(0); //Reset StringBuffer

						document.add(new TextField(tags.get(tag), tagsContent,Field.Store.YES));
					}
					writer.addDocument(document);
				}
			}
		}
		System.out.println("Indexing Complete");
	}
}