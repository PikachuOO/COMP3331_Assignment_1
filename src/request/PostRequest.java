package request;
import model.DiscussionPost;
import model.Ebook_db;
import model.Page;
import response.MessageResponse;
import response.Response;


public class PostRequest extends Request{

	private static final long serialVersionUID = 1L;
	
	private String book;
	private int page;
	private int line_number;
	private String content;

	public PostRequest(String userName, String book, int page, String line_number, String content) {
		super(userName);
		this.book = book;
		this.page = page;
		this.line_number = Integer.parseInt(line_number)-1;
		this.content = content;
	}

	@Override
	public Response process(Ebook_db db) {
		Page p = db.search(this.book, this.page);
		DiscussionPost post = new DiscussionPost(db.generateSerialID(), this.userName, this.line_number, this.content);
		p.addDiscussionPost(post);
		return new MessageResponse("Post Successful");
	}

}
