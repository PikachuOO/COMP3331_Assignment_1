
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Ebook_db {

	private List<Page> db;
	private int serialID;	//counter to give unique id to discussion posts

	public Ebook_db() throws IOException {
		File list = new File("Ebooks");		//hardcode book files
		if (!list.exists()) {				//if system has different relative dir
			list = new File("../Ebooks");
		}
		this.db = new ArrayList<Page>();
		this.serialID = 0;

		//populates list with book contents
		for (File f : list.listFiles()) {
			Page p = create(f);
			db.add(p);
		}
		System.out.println("The database for discussion posts has been initialised");
	}

	private Page create (File f) {
		List<String> list = new ArrayList<String>();
		String name = f.getName();
		String book = name.replaceAll("_page[0-9]", "");
		String page = name.replace(book+"_page", "");
		try {

			BufferedReader buf = new BufferedReader(new FileReader(f));

			while (buf.ready()) {
				list.add(buf.readLine());
			}

			buf.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (new Page(list, book, page));
	}

	public Page search(String book, String page) {
		return search(book, Integer.parseInt(page));
	}
	public Page search(String book, int page) {
		for (Page p : db) {
			if (p.getBookName().equals(book) && p.getPageNumber() == page) {
				return p;
			}
		}
		return null;
	}
	
	public List<DiscussionPost> getAllDiscussionPosts () {
		List<DiscussionPost> allPosts = new ArrayList<DiscussionPost>();
		for (Page page : this.db) {
			for (Line line : page.getLines()) {
				for (DiscussionPost post : line.getDiscussionPost()) {
					allPosts.add(post);
				}
			}
		}
		return allPosts;
	}
	
	public DiscussionPost getMostRecentPost() {
		//assumes that in order to get to this function, at least one post would have been made
		DiscussionPost mostRecentPost = getAllDiscussionPosts().get(0);
		for (DiscussionPost post : getAllDiscussionPosts()) {
			if (mostRecentPost.getSerialID() != Math.max(mostRecentPost.getSerialID(), post.getSerialID())) {
				mostRecentPost = post;
			}
		}
		return mostRecentPost;
	}
	
	public synchronized int generateSerialID() {
		return this.serialID++;
	}

}
