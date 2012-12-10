package jhn.label.topic;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

import it.unimi.dsi.fastutil.ints.IntSet;

import jhn.util.RandUtil;

public class RestrictedRandomTopicLabelSource extends RandomTopicLabelSource {
	private IntSet allowedTopics;
	public RestrictedRandomTopicLabelSource(IndexReader topicWordIdx,
			String datasetName, IntSet allowedTopics) {
		super(topicWordIdx, datasetName);
		this.allowedTopics = allowedTopics;
	}

	public RestrictedRandomTopicLabelSource(String topicWordIdxDir,
			String datasetName, IntSet allowedTopics) throws CorruptIndexException, IOException {
		super(topicWordIdxDir, datasetName);
		this.allowedTopics = allowedTopics;
	}

	@Override
	public int randTopicNum() {
		return RandUtil.randItem(allowedTopics);
	}

}
