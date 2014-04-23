
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DisplayRequest extends Request{

	private static final long serialVersionUID = 1L;
	private final List<Integer> readPosts;

	public DisplayRequest(String command, String userName, String bookName, int pageNumber, List<Integer> readPosts) {
		super(command, userName, bookName, pageNumber);
		this.readPosts = readPosts;
	}

	@Override
	public Response process(Ebook_db db, String mode) {
		Page p = db.search(bookName, pageNumber);
		if (p == null) {
			return new MessageResponse("Book and/or page does not exist");
		}
		
		if (mode.equals("pull")) {
			System.out.println("Querying discussion posts");
			
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
		} else {	//PUSH mode
			return new DisplayResponse(p);
		}
		
	}
}
