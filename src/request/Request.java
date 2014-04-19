package request;
import java.io.Serializable;

import model.Ebook_db;
import response.Response;


public  abstract class Request implements Serializable{
	
	private static final long serialVersionUID = 1L;
	protected String userName;
	
	public Request(String userName) {
		this.userName = userName;
	}
	
	public abstract Response process(Ebook_db db);

	public String getUserName() {
		return userName;
	}
	
	

}
