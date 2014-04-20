package response;

public class MessageResponse implements Response{

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
