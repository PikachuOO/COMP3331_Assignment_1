package request;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.DiscussionPost;
import model.Ebook_db;
import model.Line;
import model.Page;
import response.DisplayResponse;
import response.MessageResponse;
import response.Response;


public class DisplayRequest extends Request{

	private static final long serialVersionUID = 1L;
	private final List<Integer> readPosts;

	public DisplayRequest(String command, String userName, String bookName, int pageNumber, List<Integer> readPosts) {
		super(command, userName, bookName, pageNumber);
		this.readPosts = readPosts;
	}

	@Override
	public Response process(Ebook_db db) {
		Page p = db.search(bookName, pageNumber);
		if (p == null) {
			return new MessageResponse("Book and/or page does not exist");
		}
		
		//calculating which lines have unread posts
		List<Character> markList = new ArrayList<Character>(p.getLines().size());
		
		Set<Integer> setOfReadPosts = new HashSet<Integer>(this.readPosts);
		for (Line l : p.getLines()) {
			char mark;
			if (l.getDiscussionPost().size() == 0) {
				mark = ' ';
			} else {
				Set<Integer> setOfLineDiscussionPosts = new HashSet<Integer>();
				for (DiscussionPost post : l.getDiscussionPost()) {
					setOfLineDiscussionPosts.add(post.getSerialID());
				}
				if (setOfReadPosts.containsAll(setOfLineDiscussionPosts)) {
					mark = 'm';
				} else {
					mark = 'n';
				}
			}
			markList.add(mark);	
		}
		return new DisplayResponse(p, markList);
	}
}
