package request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.DiscussionPost;
import model.Ebook_db;
import model.Line;
import model.Page;
import response.MessageResponse;
import response.ReadResponse;
import response.Response;

public class ReadRequest extends Request{

	private static final long serialVersionUID = 1L;
	
	private final int lineNumber;
	private final List<Integer> readPosts;

	public ReadRequest(String command, String userName, String bookName, int pageNumber, String lineNumber, List<Integer> readPosts) {
		super(command, userName, bookName, pageNumber);
		this.lineNumber = Integer.parseInt(lineNumber)-1;
		this.readPosts = readPosts;
	}

	@Override
	public Response process(Ebook_db db) {
		Page p = db.search(this.bookName, this.pageNumber);
		if (p == null) {
			return new MessageResponse("Book and/or page does not exist");
		}
		if (this.lineNumber >= p.getLines().size() || this.lineNumber <= 0) {
			return new MessageResponse("Invalid Line Number");
		}
		Line l = p.getLines().get(this.lineNumber);
		Set<Integer> readSet = new HashSet<Integer>(readPosts);
		Set<Integer> lineSet = new HashSet<Integer>();
		for (DiscussionPost post : l.getDiscussionPost()) {
			lineSet.add(post.getSerialID());
		}
		lineSet.removeAll(readSet);
		if (lineSet.size() == 0) {
			return new MessageResponse("There are no posts here");
		} else {
			List<String> unreadPosts = new ArrayList<String>();
			unreadPosts.add("Book by " + p.getBookName() + ", Page " + p.getPageNumber() + ", Line number " + this.lineNumber + ":\n");
			for (int i : lineSet) {
				for (DiscussionPost post : l.getDiscussionPost()) {
					if (post.getSerialID() == i) {
						unreadPosts.add(post.getSerialID() + " " + post.getUserName() + ":\t" + post.getPost());
						readPosts.add(post.getSerialID());
					}
				}
			}
			return new ReadResponse(unreadPosts, readPosts);
		}
	}

}
