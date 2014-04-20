package response;
import java.util.ArrayList;
import java.util.List;

import model.DiscussionPost;
import model.Line;
import model.Page;


public class DisplayResponse implements Response{
	
	private static final long serialVersionUID = 1L;
	private Page p;
	public List<Character> mark;
	
	public DisplayResponse (Page p, List<Character> mark) {
		this.p = p;
		this.mark = mark;
	}
	
	public Page getPage() {
		return this.p;
	}
	
	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < mark.size(); i++) {
			out.append(mark.get(i));
			out.append(this.p.getLines().get(i).getLine() + '\n');
		}
		
		return out.toString();
	}

}
