package jhn.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class TopTopicsWriter implements AutoCloseable {
	private ObjectOutputStream w;
	public TopTopicsWriter(String filename) throws FileNotFoundException, IOException {
		w = new ObjectOutputStream(new FileOutputStream(filename));
	}
	
	public void topic(int topicNum, int count) throws IOException {
		w.writeInt(topicNum);
		w.writeInt(count);
	}
	
	@Override
	public void close() throws IOException {
		w.close();
	}
	
}
