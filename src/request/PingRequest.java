package request;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.DiscussionPost;
import model.Ebook_db;
import model.Line;
import model.Page;
import response.MessageResponse;
import response.PingResponse;
import response.Response;

public class PingRequest extends Request {

	private static final long serialVersionUID = 1L;
	
	private List<Integer> readPosts;

	public PingRequest(String command, String userName, String bookName, int pageNumber, List<Integer> readPosts) {
		super(command, userName, bookName, pageNumber);
		this.readPosts = readPosts;
	}

	@Override
	public Response process(Ebook_db db, String mode) {
		Page p = db.search(bookName, pageNumber);
		if (p == null) {
			return new MessageResponse("Book and/or page does not exist");
		}
		Set<Integer> setOfReadPosts = new HashSet<Integer>(this.readPosts);
		Set<Integer> setOfPagePosts = new HashSet<Integer>();
		for (Line l : p.getLines()) {
			for (DiscussionPost post : l.getDiscussionPost()) {
				setOfPagePosts.add(post.getSerialID());
			}
		}
		setOfPagePosts.removeAll(setOfReadPosts);
		if (!setOfPagePosts.isEmpty()) {
			return new PingResponse(true);
		}
		return new PingResponse(false);
	}

}
