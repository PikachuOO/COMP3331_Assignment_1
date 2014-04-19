package response;
import model.Page;


public class DisplayResponse extends Response{
	
	private static final long serialVersionUID = 1L;
	private Page p;
	
	public DisplayResponse (Page p) {
		this.p = p;
	}
	
	public Page getPage() {
		return this.p;
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (Object o : p.getContent())
		{
		  out.append(o.toString() + '\n');
		}
		return out.toString();
	}

}
