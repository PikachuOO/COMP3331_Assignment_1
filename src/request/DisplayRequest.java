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
	private String bookName;
	private String pageNumber;
	private List<Integer> readPosts;

	public DisplayRequest(String userName, List<Integer> readPosts, String bookName, String pageNumber) {
		super(userName);
		this.readPosts = readPosts;
		this.bookName = bookName;
		this.pageNumber = pageNumber;
	}

	@Override
	public Response process(Ebook_db db) {
		Page p = db.search(bookName, pageNumber);
		/*Page joyce3 = db.search("joyce", 3);
		System.out.println("----------------------------");
		for (Line l : joyce3.getLines()) {
			System.out.println(l.getDiscussionPost().size());
		}
		System.out.println("----------------------------");
		*/
		if (p == null) {
			return new MessageResponse("Book or page does not exist");
		}

		List<Character> markList = new ArrayList<Character>(p.getLines().size());
		
		Set<Integer> read = new HashSet<Integer>(this.readPosts);
		for (Line l : p.getLines()) {
			char mark;
			if (l.getDiscussionPost().size() == 0) {
				mark = ' ';
			} else {
				Set<Integer> s = new HashSet<Integer>();
				for (DiscussionPost post : l.getDiscussionPost()) {
					s.add(post.getSerialID());
				}
				if (read.containsAll(s)) {
					mark = 'm';
				} else {
					mark = 'n';
				}
				//s now conteains all ID of each
			}
			
			
			markList.add(mark);
			
			
		}
		
		/*for (Line l : p.getLines()) {
			char mark;
			if (l.getDiscussionPost().size() == 0) {
				mark = 'b';
			} else {
				mark = 'n';
				for (DiscussionPost post : l.getDiscussionPost()) {
					for (int readPostID : readPosts) {
						if (post.getSerialID() == readPostID) {
							mark = 'm';
						}
					}
				}
			}
			markList.add(mark);
		}*/

		Response r = new DisplayResponse(p, markList);
		//System.out.println(r.toString());
		return r;
	}


}
