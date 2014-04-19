import java.io.Serializable;


public  abstract class Request implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String userName;
	
	public Request(String userName) {
		this.userName = userName;
	}
	
	public abstract Response process(Ebook_db db);

	public String getUserName() {
		return userName;
	}
	
	

}
