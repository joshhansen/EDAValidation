package jhn.validation;

public interface TopicLabelSource extends LabelSource<Integer>, Named {
//	@Override
//	String[] labels(Integer topicNum, int numlabels);
	
	String[] labels(int topicNum, int numLabels);
}
