package model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Ebook_db {

	private List<Page> db;
	private int serialID;

	public Ebook_db() throws IOException {
		File list = new File("Ebooks");
		this.db = new ArrayList<Page>();
		this.serialID = 0;

		for (File f : list.listFiles()) {
			Page p = create(f);
			db.add(p);
		}
	}

	private Page create (File f) {
		List<String> list = new ArrayList<String>();
		String name = f.getName();
		String book = name.replaceAll("_page[0-9]", "");
		String page = name.replace(book+"_page", "");
		try {

			BufferedReader buf = new BufferedReader(new FileReader(f));

			while (buf.ready()) {
				list.add(buf.readLine());
			}

			buf.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (new Page(list, book, page));
	}

	public Page search(String book, String page) {
		return search(book, Integer.parseInt(page));
	}
	public Page search(String book, int page) {
		for (Page p : db) {
			if (p.getBookName().equals(book) && p.getPageNumber() == page) {
				return p;
			}
		}
		return null;
	}
	
	public int generateSerialID() {
		return this.serialID++;
	}

}
