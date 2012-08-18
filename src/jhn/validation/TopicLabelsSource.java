package jhn.validation;

public interface TopicLabelsSource extends Named {
	String[] labels(int topicNum, int numlabels);
}
