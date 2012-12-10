package jhn.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import jhn.counts.i.i.IntIntCounter;
import jhn.counts.i.i.IntIntRAMCounter;

public class TopTopicsReader implements AutoCloseable, IntIterator {
	private ObjectInputStream r;
	private int nextTopicNum;
	private int nextTopicCount;
	public TopTopicsReader(String filename) throws FileNotFoundException, IOException {
		r = new ObjectInputStream(new FileInputStream(filename));
	}
	
	@Override
	public void close() throws Exception {
		r.close();
	}

	@Override
	public boolean hasNext() {
		try {
			nextTopicNum = r.readInt();
			nextTopicCount = r.readInt();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public Integer next() {
		return Integer.valueOf(nextInt());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int nextInt() {
		return nextTopicNum;
	}

	public int nextTopicCount() {
		return nextTopicCount;
	}

	@Override
	public int skip(int n) {
		throw new UnsupportedOperationException();
	}
	
	public IntIntCounter readCounter() {
		IntIntCounter counts = new IntIntRAMCounter();
		
		while(hasNext()) {
			counts.set(nextInt(), nextTopicCount());
		}
		
		return counts;
	}
	
	public IntSet readTopicsSet(int numTopics) {
		IntSet set = new IntOpenHashSet();
		
		for(int i = 0; hasNext() && i < numTopics; i++) {
			set.add(nextInt());
		}
		
		return set;
	}

}
