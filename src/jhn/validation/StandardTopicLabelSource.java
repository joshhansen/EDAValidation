package jhn.validation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.util.RandUtil;

/**
 * Reads topic labels from a file. Format:
 *     #comment
 *     topicNum,word1,word2,...,wordN,"label"
 * etc.
 */
public class StandardTopicLabelSource implements TopicLabelSource, BareLabelSource {

	private Int2ObjectMap<String> labels = new Int2ObjectOpenHashMap<>();
	private Int2ObjectMap<String[]> topicWords = new Int2ObjectOpenHashMap<>();
	public StandardTopicLabelSource(String topicLabelsFilename) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(topicLabelsFilename))) {
			String line = null;
			while( (line=r.readLine()) != null) {
				if(!line.startsWith("#")) {
					String[] parts = line.split(",");
					int topicNum = Integer.parseInt(parts[0]);
					
					String[] words = new String[parts.length-2];
					for(int i = 1; i < parts.length-1; i++) {
						words[i-1] = parts[i];
					}
					this.topicWords.put(topicNum, words);
					
					String label = parts[parts.length-1];
					label = label.substring(1, label.length()-1);
					labels.put(topicNum, label);
				}
			}
		}
	}

	@Override
	public String[] labels(Integer topicNum, int numLabels) {
		return labels(topicNum.intValue(), numLabels);
	}

	@Override
	public String[] labels(int topicNum, int numLabels) {
		if(numLabels > 1) throw new IllegalArgumentException("Only one label at a time for now");
		
		return new String[]{ labels.get(topicNum) };
	}

	@Override
	public String[] labels(int numLabels) {
		return labels(RandUtil.randItem(labels.keySet()), numLabels);
	}
	
	public String[] topicWords(int topicNum) {
		return topicWords.get(topicNum);
	}

}
