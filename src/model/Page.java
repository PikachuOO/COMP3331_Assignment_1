package model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Page implements Serializable {

	private static final long serialVersionUID = 1L;
	private String book;
	private int page;
	private List<String> lines;
	private List<DiscussionPost> posts;
	
	public Page(List<String> lines, String book, String page) {
		this.lines = lines;
		this.book = book;
		this.page = Integer.parseInt(page);
		this.posts = new ArrayList<DiscussionPost>();
	}
	
	public void addDiscussionPost(DiscussionPost post) {
		posts.add(post);
	}
	
	public List<DiscussionPost> getPosts() {
		return this.posts;
	}
	
	public List<String> getContent() {
		return this.lines;
	}

	public String getBookName() {
		return this.book;
	}

	public int getPageNumber() {
		return this.page;
	}

}
