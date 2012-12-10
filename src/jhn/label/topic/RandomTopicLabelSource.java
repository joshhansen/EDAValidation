package jhn.label.topic;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

/** Topic label source where labels do not correspond to topic number */
public class RandomTopicLabelSource extends LuceneTopicLabelSource {

	public RandomTopicLabelSource(String topicWordIdxDir, String datasetName) throws CorruptIndexException, IOException {
		super(topicWordIdxDir, datasetName);
	}
	
	public RandomTopicLabelSource(IndexReader topicWordIdx, String datasetName) {
		super(topicWordIdx, datasetName);
	}

	@Override
	public String[] labels(int topicNum, int numLabels) {
		return super.labels(randTopicNum(), numLabels);
	}

	@Override
	public String[] topicWords(int topicNum) {
		return super.topicWords(randTopicNum());
	}

	@Override
	public String[] labels(Integer labelKey, int numLabels) {
		return super.labels(randTopicNum(), numLabels);
	}
}
