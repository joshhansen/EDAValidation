package jhn.validation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Reads topic labels from a file. Format:
 *     #comment
 *     topicNum,word1,word2,...,wordN,"label"
 * etc.
 */
public class StandardTopicLabelSource extends StandardNamed implements TopicLabelSource {

	private Int2ObjectMap<String> labels = new Int2ObjectOpenHashMap<>();
	public StandardTopicLabelSource(String name, String topicLabelsFilename) throws FileNotFoundException, IOException {
		super(name);
		try(BufferedReader r = new BufferedReader(new FileReader(topicLabelsFilename))) {
			String line = null;
			while( (line=r.readLine()) != null) {
				if(!line.startsWith("#")) {
					String[] parts = line.split(",");
					int topicNum = Integer.parseInt(parts[0]);
					String label = parts[parts.length-1];
					label = label.substring(1, label.length()-1);
					labels.put(topicNum, label);
				}
			}
		}
	}

	@Override
	public String[] labels(int topicNum, int numLabels) {
		if(numLabels > 1) throw new IllegalArgumentException("Only one label at a time for now");
		
		return new String[]{ labels.get(topicNum) };
	}

}
