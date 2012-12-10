package jhn.label.topic;

import jhn.label.LabelSource;

public interface TopicLabelSource extends LabelSource<Integer> {
	String[] labels(int topicNum, int numLabels);
	
	public String[] topicWords(int topicNum);
}
