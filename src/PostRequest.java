
public class PostRequest extends Request{

	private static final long serialVersionUID = 1L;
	
	private String line_number;
	private String content;

	public PostRequest(String userName, String line_number, String content) {
		super(userName);
		this.line_number = line_number;
		this.content = content;
	}

	@Override
	public Response process(Ebook_db db) {
		
		return null;
	}

}
