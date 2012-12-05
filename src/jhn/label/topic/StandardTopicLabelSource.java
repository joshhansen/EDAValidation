package jhn.label.topic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.label.BareLabelSource;
import jhn.util.RandUtil;

/**
 * Reads topic labels from a file. Format:
 *     #comment
 *     topicNum,word1,word2,...,wordN,"label"
 * etc.
 */
public class StandardTopicLabelSource implements TopicLabelSource, BareLabelSource {

	private static final Pattern rgx = Pattern.compile("([^,]+),(.+),\"([^\"]+)\"");
	private Int2ObjectMap<String> labels = new Int2ObjectOpenHashMap<>();
	private Int2ObjectMap<String[]> topicWords = new Int2ObjectOpenHashMap<>();
	public StandardTopicLabelSource(String topicLabelsFilename) throws FileNotFoundException, IOException {
		try(BufferedReader r = new BufferedReader(new FileReader(topicLabelsFilename))) {
			Matcher m;
			int topicNum;
			String line = null;
			while( (line=r.readLine()) != null) {
				if(!line.startsWith("#")) {
					m = rgx.matcher(line);
					m.matches();
					topicNum = Integer.parseInt(m.group(1));
					topicWords.put(topicNum, m.group(2).split(","));
					labels.put(topicNum, m.group(3));
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
