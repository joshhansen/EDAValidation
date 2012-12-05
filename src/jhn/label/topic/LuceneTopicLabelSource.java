package jhn.label.topic;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.FSDirectory;

import jhn.util.RandUtil;
import jhn.wp.Fields;

public class LuceneTopicLabelSource implements SampleableTopicLabelSource {
	private IndexReader r;
	private final int numTopics;
	public LuceneTopicLabelSource(String topicWordIdxDir) throws CorruptIndexException, IOException {
		r = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)));
		numTopics = r.numDocs();
	}
	
	@Override
	public String[] labels(Integer labelKey, int numLabels) {
		return labels(labelKey.intValue(), numLabels);
	}
	
	@Override
	public String[] labels(int topicNum, int numLabels) {
		String label;
		try {
			label = r.document(topicNum).get(Fields.label);
		} catch (IOException e) {
			label = null;
		}
		if(label==null) {
			return new String[0];
		}
		return new String[]{label};
	}
	
	@Override
	public int randTopicNum() {
		return RandUtil.rand.nextInt(numTopics);
	}

	@Override
	public String[] topicWords(int topicNum) {
		try {
			TermFreqVector tfv = r.getTermFreqVector(topicNum, Fields.text);
			if(tfv != null) {
				return tfv.getTerms();
			}
		} catch(IOException e) {
			// Pass through
		}
		
		return new String[0];
	}
}
