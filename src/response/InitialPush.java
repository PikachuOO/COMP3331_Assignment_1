package response;

import java.util.ArrayList;
import java.util.List;

import model.DiscussionPost;

public class InitialPush implements Response {
	
	private static final long serialVersionUID = 1L;
	
	private List<Integer> serialIDs;
	private List<String> userNames;
	private List<String> bookNames;
	private List<Integer> pageNumbers;
	private List<Integer> lineNumbers;
	private List<String> posts;
	
	public InitialPush () {
		serialIDs = new ArrayList<Integer>();
		userNames = new ArrayList<String>();
		bookNames = new ArrayList<String>();
		pageNumbers = new ArrayList<Integer>();
		lineNumbers = new ArrayList<Integer>();
		posts = new ArrayList<String>();
	}
	
	public void add (DiscussionPost post) {
		/*assert((this.serialIDs.size()|
				this.userNames.size()|
				this.bookNames.size()|
				this.pageNumbers.size()|
				this.lineNumbers.size()|
				this.posts.size())==
				(this.serialIDs.size()|
				this.userNames.size()|
				this.bookNames.size()|
				this.pageNumbers.size()|
				this.lineNumbers.size()|
				this.posts.size()));*/
		this.serialIDs.add(post.getSerialID());
		this.userNames.add(post.getUserName());
		this.bookNames.add(post.getBookName());
		this.pageNumbers.add(post.getPageNumber());
		this.lineNumbers.add(post.getLineNumber());
		this.posts.add(post.getPost());
		
	}
	public List<DiscussionPost> rebuild () {
		List<DiscussionPost> posts = new ArrayList<DiscussionPost>();
		for (int index = 0; index < this.serialIDs.size(); index++) {
			posts.add(new DiscussionPost(this.getSerialID(index), this.getUserName(index), this.getBookName(index), this.getPageNumber(index), this.getLineNumber(index), this.getPost(index)));
		}
		return posts;
	}
	
	public int getSerialID(int index) {
		return this.serialIDs.get(index);
	}

	public String getUserName(int index) {
		return this.userNames.get(index);
	}
	
	public String getBookName(int index) {
		return this.bookNames.get(index);
	}
	
	public int getPageNumber(int index) {
		return this.pageNumbers.get(index);
	}
	
	public int getLineNumber(int index) {
		return this.lineNumbers.get(index);
	}
	
	public String getPost(int index) {
		return this.posts.get(index);
	}

}
