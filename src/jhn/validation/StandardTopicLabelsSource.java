package jhn.validation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class StandardTopicLabelsSource extends StandardNamed implements TopicLabelsSource {

	private Int2ObjectMap<String> labels = new Int2ObjectOpenHashMap<>();
	public StandardTopicLabelsSource(String name, String topicLabelsFilename) throws FileNotFoundException, IOException {
		super(name);
		try(BufferedReader r = new BufferedReader(new FileReader(topicLabelsFilename))) {
			String line = null;
			while( (line=r.readLine()) != null) {
				String[] parts = line.split(",");
				int topicNum = Integer.parseInt(parts[0]);
				String label = parts[parts.length-1];
				label = label.substring(1, label.length()-1);
				labels.put(topicNum, label);
			}
		}
	}

	@Override
	public String[] labels(int topicNum, int numLabels) {
		if(numLabels > 1) throw new IllegalArgumentException("Only one label at a time for now");
		
		return new String[]{ labels.get(topicNum) };
	}

}
