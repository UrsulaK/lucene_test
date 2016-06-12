package de.uk.lucene.test;

import java.util.UUID;

public class PDFDocument {

	private String id;
	private String title;
	private String content;
	
	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public PDFDocument(){
		
	}
	public PDFDocument(String title, String content) {
		super();
		 this.setId();
		this.title = title;
		this.content = content;
	}
	public String getId() {
		return id;
	}
	public void setId() {
		if(this.id == null){
			this.id = UUID.randomUUID().toString();
		}
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	
}
