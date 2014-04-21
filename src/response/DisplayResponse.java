package response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.DiscussionPost;
import model.Line;
import model.Page;


public class DisplayResponse implements Response{

	private static final long serialVersionUID = 1L;
	private Page p;
	public List<Character> markList;
	
	public DisplayResponse (Page p) {
		this.p = p;
		this.markList = null;
	}

	public DisplayResponse (Page p, List<Character> markList) {
		this(p);
		this.markList = markList;
	}

	public Page getPage() {
		return this.p;
	}
	
	public String toString (String currentBookName, int currentPageNumber, List<DiscussionPost> postDB, List<Integer> readPosts) {
		System.out.println("push version");
		List<DiscussionPost> filteredPostDB = new ArrayList<DiscussionPost>();
		this.markList = new ArrayList<Character>();
		for (DiscussionPost post : postDB) {
			if (post.getBookName().equals(currentBookName) && post.getPageNumber() == currentPageNumber) {
				filteredPostDB.add(post);
			}
		}
		Set<Integer> readPostSet = new HashSet<Integer>(readPosts);
		Set<Integer> filteredPostDBSet = new HashSet<Integer>();
		for (DiscussionPost post : filteredPostDB) {
			filteredPostDBSet.add(post.getSerialID());
		}
		List<Set<Integer>> categorisedFilteredPostDBSet = new ArrayList<Set<Integer>>();
		for (int i = 0; i < this.p.getLines().size(); i++) {
			categorisedFilteredPostDBSet.add(new HashSet<Integer>());
		}
		for (DiscussionPost post : filteredPostDB) {
			categorisedFilteredPostDBSet.get(post.getLineNumber()).add(post.getSerialID());
		}
		for (int i = 0; i < categorisedFilteredPostDBSet.size(); i++) {
			if (categorisedFilteredPostDBSet.get(i).size() == 0) {
				this.markList.add(i, ' ');
			}
			else if (readPostSet.containsAll(categorisedFilteredPostDBSet.get(i))) {
				this.markList.add(i, 'm');
			}
			else if (!readPostSet.containsAll(categorisedFilteredPostDBSet.get(i))) {
				this.markList.add(i, 'n');
			}
		}
		return toString();
		
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < this.p.getLines().size(); i++) {
			if (this.markList != null) {
				out.append(markList.get(i));
			}
			out.append(this.p.getLines().get(i).getLine() + '\n');
		}

		return out.toString();
	}

}
