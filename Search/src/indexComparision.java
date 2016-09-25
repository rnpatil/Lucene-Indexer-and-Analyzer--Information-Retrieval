import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class indexComparision {


	private static String TEXT = "TEXT";

	// main method where the object for the generateIndex class is instantiated
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {

		// this has the path where the index needs to be created


		File keywordindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\KeyWordIndex\\");
		File simpleindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\SimpleIndex\\");
		File standardindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\StandardIndex\\");
		File stopwordindexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\StopWordIndex\\");
		// this is the path from which the documents to be indexed
		File dataDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\Data\\");



		Analyzer analyzer= null;
		// this object will call the index method to generate the indexing
		indexComparision indexObj = new indexComparision();

		analyzer = new StandardAnalyzer();
		//indexObj.index(standardindexDirectory, dataDirectory,analyzer);
		analyzer = new KeywordAnalyzer();
		//indexObj.index(keywordindexDirectory, dataDirectory,analyzer);
		analyzer = new SimpleAnalyzer();
		//indexObj.index(simpleindexDirectory, dataDirectory,analyzer);
		analyzer = new StopAnalyzer();
		//indexObj.index(stopwordindexDirectory, dataDirectory,analyzer);

		System.out.println("Now Analyzing Keyword Indexing");
		indexInfo(keywordindexDirectory);

		System.out.println("Now Analyzing simpleindexDirectory Indexing" +analyzer.getVersion());
		indexInfo(simpleindexDirectory);

		System.out.println("Now Analyzing standardindexDirectory Indexing" +analyzer.getVersion());
		indexInfo(standardindexDirectory);

		System.out.println("Now Analyzing stopwordindexDirectory Indexing" +analyzer.getVersion());
		indexInfo(stopwordindexDirectory);

		analyzer.close();

	}

	private void index(File indexDirectory, File dataDirectory, Analyzer analyzer)
			throws Exception {

		Directory dir = FSDirectory.open(Paths.get(String.valueOf(indexDirectory)));

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		iwc.setOpenMode(OpenMode.CREATE);

		// to write the documents to the index

		IndexWriter writer = new IndexWriter(dir, iwc);

		indexing(writer, dataDirectory);

		writer.forceMerge(1);
		writer.commit();

		System.out.println("Index Commited");
		writer.close();
		System.out.println("Index Closed");





	}


	private static void indexInfo(File indexDirectory)throws Exception
	{

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(String.valueOf(indexDirectory))));

		System.out.println("Number of occurrences of \"have\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","have")));

		Terms vocabulary = MultiFields.getTerms(reader, "TEXT");

		System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
		
		
		//Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
		System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
		reader.close();

	}
	private void indexing(IndexWriter writer, File dataDirectory) throws Exception {


		ArrayList<String> tag = new ArrayList<String>();

		tag.add(TEXT);

		File[] files = dataDirectory.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory() && !files[i].isHidden() && files[i].canRead() && files[i].exists()) {
				System.out.println("\nIndexing is going on with file : "+ files[i].getCanonicalPath());

				String fileContents = new String(Files.readAllBytes(Paths.get(files[i].getCanonicalPath())));
				String[] splitFileContent = fileContents.split("</DOC>");

				//since the last split will be space subtracting one from the doc count in a single trectext file
				String[] documents = new String[splitFileContent.length-1];
				for(int k=0;k<splitFileContent.length-1;k++) documents[k]  = splitFileContent[k];



				for (String docContent : documents) {

					Document document = new Document();
					for (int j = 0; j < tag.size(); j++) {
						String tagContent = "";
						int startIndex = 0;
						StringBuffer contentBuffer = new StringBuffer();

						while ((startIndex = docContent.indexOf("<" + tag.get(j)+">", startIndex)) != -1) {
							startIndex += tag.get(j).length() + 2;
							int endindex = docContent.indexOf("</" + tag.get(j)+ ">", startIndex);
							String content = docContent.substring(startIndex,endindex);
							contentBuffer.append(content);
							startIndex += content.length();
							//System.out.println(content);
						}

						tagContent = contentBuffer.toString();
						document.add(new TextField(tag.get(j), tagContent,Field.Store.YES));
					}
					writer.addDocument(document);
				}


			}


		}
		System.out.println("Indexing Successful");

	}

}
