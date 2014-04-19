
public class DisplayRequest extends Request{

	private static final long serialVersionUID = 1L;
	private String bookName;
	private String pageNumber;
	
	public DisplayRequest(String userName, String bookName, String pageNumber) {
		super(userName);
		this.bookName = bookName;
		this.pageNumber = pageNumber;
	}

	@Override
	public Response process(Ebook_db db) {
		Page p = db.search(bookName, pageNumber);
		Response response = new DisplayResponse(p);
		return response;
	}
	

}
