package jhn.label.topic;

import java.io.FileNotFoundException;
import java.io.IOException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.io.TopicLabel;
import jhn.io.TopicLabelFileReader;
import jhn.label.BareLabelSource;
import jhn.util.RandUtil;

/**
 * Reads topic labels from a file. Format:
 *     #comment
 *     topicNum,word1,word2,...,wordN,"label"
 * etc.
 */
public class StandardTopicLabelSource implements SampleableTopicLabelSource, BareLabelSource {
	private Int2ObjectMap<String> labels = new Int2ObjectOpenHashMap<>();
	private Int2ObjectMap<String[]> topicWords = new Int2ObjectOpenHashMap<>();
	public StandardTopicLabelSource(String topicLabelsFilename) throws FileNotFoundException, IOException {
		try(TopicLabelFileReader r = new TopicLabelFileReader(topicLabelsFilename)) {
			for(TopicLabel tl : r) {
				topicWords.put(tl.topicNum(), tl.words());
				labels.put(tl.topicNum(), tl.label());
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
	
	@Override
	public String[] topicWords(int topicNum) {
		return topicWords.get(topicNum);
	}

	@Override
	public int randTopicNum() {
		return RandUtil.randItem(labels.keySet().toIntArray());
	}

}
