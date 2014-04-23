

import java.io.Serializable;

public class DiscussionPost implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private final int serialID;
	private final String userName;
	private final String bookName;
	private final int pageNumber;
	private final int lineNumber;
	private final String post;
	
	
	public DiscussionPost (int serialID, String userName, String bookName, int pageNumber, int lineNumber, String post) {
		this.serialID = serialID;
		this.userName = userName;
		this.bookName = bookName;
		this.pageNumber = pageNumber;
		this.lineNumber = lineNumber;
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
	
	public String toStrings() {
		return this.getSerialID() + " " + this.getUserName() + ":\t" + this.getPost();
	}

	public String getBookName() {
		return bookName;
	}

	public int getPageNumber() {
		return pageNumber;
	}

}
