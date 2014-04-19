package request;
import model.Ebook_db;
import model.Page;
import response.DisplayResponse;
import response.MessageResponse;
import response.Response;


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
		if (p == null) {
			return new MessageResponse("Book or page does not exist");
		}
		return new DisplayResponse(p);
	}
	

}
