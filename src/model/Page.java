package model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Page implements Serializable {

	private static final long serialVersionUID = 1L;
	private String book;
	private int page;
	private List<Line> lines;
	
	public Page(List<String> lines, String book, String page) {
		this.lines = new ArrayList<Line>();
		for (int i = 0; i < lines.size(); i++) {
			this.lines.add(new Line(i, lines.get(i)));
		}
		this.book = book;
		this.page = Integer.parseInt(page);
	}
	
	public void addDiscussionPost(DiscussionPost post) {
		//System.out.println("adding post");
		Line l = this.lines.get(post.getLineNumber());
		//System.out.println("before " + l.getDiscussionPost().size());
		l.addPost(post);
		//System.out.println("after " + l.getDiscussionPost().size());
	}
	
	public List<Line> getLines() {
		return this.lines;
	}

	public String getBookName() {
		return this.book;
	}

	public int getPageNumber() {
		return this.page;
	}

}
