package jhn.validation;

public interface TopicLabelSource extends Named {
	String[] labels(int topicNum, int numlabels);
}
