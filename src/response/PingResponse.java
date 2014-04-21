package response;

public class PingResponse implements Response {

	private static final long serialVersionUID = 1L;
	
	private final boolean status;
	
	public PingResponse (boolean status) {
		this.status = status;
	}
	
	public boolean getStatus() {
		return this.status;
	}
	
	public String toString() {
		return this.status ? "There are new posts.\n" : "";
	}

}
