package response;

public class MessageResponse extends Response{

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public MessageResponse (String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
