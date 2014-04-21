package response;

import java.util.ArrayList;
import java.util.List;

import model.DiscussionPost;

public class PushNotification implements Response {
	
	private DiscussionPost post;
	private String pushMessage;
	
	public PushNotification (DiscussionPost post) {
		this.post = post;
		this.pushMessage = "";
	}

	private static final long serialVersionUID = 1L;
	
	public void updateLocalDB (List<DiscussionPost> postDB, List<Integer> readPosts) {
		postDB.add(this.post);
	}
	
	public String toString(String currentBookName, int currentPageNumber) {
		if (this.post.getBookName().equals(currentBookName) && this.post.getPageNumber() == currentPageNumber) {
			this.pushMessage = "There are new posts.\n";
		}
		return toString();
	}

	@Override
	public String toString() {
		return this.pushMessage;
	}

}
