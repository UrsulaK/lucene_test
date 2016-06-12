package de.uk.lucene.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.text.PDFTextStripper;


public class LucenePdfTest {
	
	private static final String INDEX_DIRCTORY = "src/main/resources/index";
	public static void main(String[] args) throws IOException, ParseException {

		    // 0. Specify the analyzer for tokenizing text.

		    //    The same analyzer should be used for indexing and searching

		    StandardAnalyzer analyzer = new StandardAnalyzer();
//
//		    Directory index = new RAMDirectory();

		 

		    // 1. create the index

		    IndexWriterConfig config = new IndexWriterConfig(analyzer);
		    deleteDirectory(new File(INDEX_DIRCTORY));

		    IndexWriter w = new IndexWriter(FSDirectory.open(new File(INDEX_DIRCTORY).toPath()),config);

			
			List<File> listOfFiles = Files.walk(Paths.get("/Users/UK/Documents/Microservices"))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
			
			for (File file : listOfFiles) {
			    if (file.isFile()) {
			    	addDoc(w, createDocument(file));
			    }
			}
			
		    w.close();

		 

		    // 2. query

		    String querystr = args.length > 0 ? args[0] : "Microservices";

		 

		    // the "title" arg specifies the default field to use

		    // when no field is explicitly specified in the query.

		    Query q = null;
			try {
				
				q = new QueryParser("title", analyzer).parse(querystr);
			} catch (org.apache.lucene.queryparser.classic.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		 

		    // 3. search

		    int hitsPerPage = 10;

		    IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_DIRCTORY).toPath()));

		    IndexSearcher searcher = new IndexSearcher(reader);

		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);

		    searcher.search(q, collector);

		    ScoreDoc[] hits = collector.topDocs().scoreDocs;

		 

		    // 4. display results

		    System.out.println("Found " + hits.length + " hits.");

		    for(int i=0;i<hits.length;++i) {

		      int docId = hits[i].doc;

		      Document d = searcher.doc(docId);

		      System.out.println((i + 1) + ". " + d.get("id") + "\t" + d.get("title"));

		    }

		    reader.close();

	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	}
	
	public static PDFDocument createDocument(File pdfFile) throws IOException{
		
		PDDocument doc = PDDocument.load(pdfFile);
		
	    PDFTextStripper stripper = new PDFTextStripper();

	    stripper.setLineSeparator("\n");
	    stripper.setStartPage(1);
	    stripper.setEndPage(5);// this mean that it will index the first 5 pages only
	    
	    String content = stripper.getText(doc);
	    
		doc.close();
		
		return new PDFDocument(pdfFile.getName(), content);
			
		
	}

	public static void addDoc (IndexWriter writer, PDFDocument indexFile)
            throws IOException {
		
		writer.deleteDocuments(new Term("title", indexFile.getTitle()));

		Document luceneDoc = new Document();	
		
		luceneDoc.add(new TextField(PDFDocument.ID, indexFile.getId(),Field.Store.YES));
		luceneDoc.add(new TextField(PDFDocument.CONTENT, indexFile.getContent(),Field.Store.YES));
		luceneDoc.add(new TextField(PDFDocument.TITLE, indexFile.getTitle(),Field.Store.YES));
		
		writer.addDocument(luceneDoc);
    }
}
