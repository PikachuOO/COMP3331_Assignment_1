package model;

import java.io.Serializable;

public class DiscussionPost implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int serialID;
	private int lineNumber;
	private String userName;
	private String post;
	
	
	public DiscussionPost (int serialID, String userName, int lineNumber, String post) {
		this.serialID = serialID;
		this.lineNumber = lineNumber;
		this.userName = userName;
		this.post = post;
	}
	
	public int getSerialID() {
		return this.serialID;
	}
	
	public int getLineNumber() {
		return this.lineNumber;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public String getPost() {
		return this.post;
	}

}
