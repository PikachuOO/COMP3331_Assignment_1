package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Line implements Serializable{

	private static final long serialVersionUID = 1L;
	private final int lineNumber;
	private final String line;
	private List<DiscussionPost> discussionPost;
	
	public Line(int lineNumber, String line) {
		this.lineNumber = lineNumber;
		this.line = line;
		this.discussionPost = new Vector<DiscussionPost>();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getLine() {
		return line;
	}
	
	public void addPost (DiscussionPost post) {
		this.discussionPost.add(post);
		System.out.println("Post added to database and given the serial number "+post.getSerialID());
	}
	
	public List<DiscussionPost> getDiscussionPost () {
		return this.discussionPost;
	}
	
	

}
