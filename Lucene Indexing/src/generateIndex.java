import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class generateIndex {

	private static String DOCNO = "DOCNO";
	private static String HEAD = "HEAD";
	private static String BYLINE = "BYLINE";
	private static String DATELINE = "DATELINE";
	private static String TEXT = "TEXT";
	private static ArrayList<String> tags= new ArrayList<String>();
	private static String fileType ="trectext";

	// main method where the object for the generateIndex class is instantiated
	public static void main(String[] args) throws Exception {


		// Please change all the directory paths to your local directory path before executing.

		// this has the directory path where the corpus is located
		File dataDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\Data\\corpus\\corpus\\");

		// this has the directory path where the index needs to be created

		File indexDirectory = new File("C:\\Users\\Rohit\\Documents\\Search\\Index\\");


		// this object will call initiate createIndex process
		generateIndex indexObj = new generateIndex();

		indexObj.process(indexDirectory, dataDirectory);
	}

	/**
	 * 
	 * @param indexDirectory
	 * @param dataDirectory
	 * @throws Exception
	 */
	private void process(File indexDirectory, File dataDirectory)
			throws Exception {
		Directory dir = FSDirectory.open(Paths.get(String.valueOf(indexDirectory)));
		Analyzer indexanalyzer = new StandardAnalyzer();

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
		indexStats(indexDirectory);
	}


	// This method reads the index file and gives stats about the index

	/**
	 * 
	 * @param indexDirectory
	 * @throws Exception
	 */
	private void indexStats(File indexDirectory)throws Exception
	{

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(String.valueOf(indexDirectory))));

		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());
		//Print the number of documents containing the term "new" in <field>TEXT</field>.

		reader.close();

	}
	/**
	 * 
	 * @param writer
	 * @param dataDirectory
	 * @throws Exception
	 */
	private void createIndex(IndexWriter writer, File dataDirectory) throws Exception {


		tags.add(0,DOCNO);
		tags.add(1,HEAD);
		tags.add(2,BYLINE);
		tags.add(3,DATELINE);
		tags.add(4,TEXT);

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
						contentBuffer.setLength(0);  //Reset StringBuffer
						if (tags.get(tag).equals(DOCNO)) {
							
							// If the tag currently parsed is DOCNO, Add it to the lucene document as a String fields(which we don't want tokenized)

							document.add(new StringField(DOCNO, tagsContent,Field.Store.YES));
						}
						else
							
							// All other tag, add it to the lucene document as a Text field.
							document.add(new TextField(tags.get(tag), tagsContent,Field.Store.YES));
					}
					writer.addDocument(document);
				}
			}
		}
		System.out.println("Indexing Complete");
	}
}