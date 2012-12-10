package jhn.label.topic;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import jhn.label.LabelSource;
import jhn.util.RandUtil;
import jhn.wp.Fields;

public class RandomTopicLabelsSource implements LabelSource<Integer> {
	private final int numDocs;
	private IndexReader topicWordIdx;
	public RandomTopicLabelsSource(IndexReader topicWordIdx) {
		this.topicWordIdx = topicWordIdx;
		numDocs = topicWordIdx.numDocs();
	}
	@Override
	public String[] labels(Integer labelKey, int numLabels) {
		int docNum = RandUtil.rand.nextInt(numDocs);
		String label;
		try {
			label = topicWordIdx.document(docNum).get(Fields.label);
		} catch(IOException e) {
			label = null;
		}
		if(label==null) {
			return new String[0];
		}
		return new String[]{label};
		
	}

}
