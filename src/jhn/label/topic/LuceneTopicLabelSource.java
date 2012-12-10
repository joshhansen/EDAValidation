package jhn.label.topic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;

import jhn.util.RandUtil;
import jhn.util.Util;
import jhn.wp.Fields;

public class LuceneTopicLabelSource implements SampleableTopicLabelSource {
	private IndexReader r;
	private Alphabet typeAlphabet;
	private final int numTopics;
	public LuceneTopicLabelSource(String topicWordIdxDir, String datasetName) throws CorruptIndexException, IOException {
		this(IndexReader.open(FSDirectory.open(new File(topicWordIdxDir))), datasetName);
	}
	
	public LuceneTopicLabelSource(IndexReader topicWordIdx, String datasetName) {
		r = topicWordIdx;
		numTopics = r.numDocs();
		
		String datasetFilename = jhn.Paths.malletDatasetFilename(datasetName);
		InstanceList targetData = InstanceList.load(new File(datasetFilename));
		typeAlphabet = (Alphabet) targetData.getAlphabet().clone();
		typeAlphabet.stopGrowth();
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
	
	private static class TermFreq implements Comparable<TermFreq> {
		String term;
		int freq;
		public TermFreq(String term, int freq) {
			super();
			this.term = term;
			this.freq = freq;
		}
		@Override
		public int compareTo(TermFreq o) {
			return Util.compareInts(o.freq, freq);
		}
		
	}

	@Override
	public String[] topicWords(int topicNum) {
		try {
			TermFreqVector tfv = r.getTermFreqVector(topicNum, Fields.text);
			if(tfv != null) {
				String[] terms = tfv.getTerms();
				int[] freqs = tfv.getTermFrequencies();
				List<TermFreq> tfs = new ArrayList<>();
				for(int i = 0; i < tfv.size(); i++) {
					if(typeAlphabet.contains(terms[i])) {
						tfs.add(new TermFreq(terms[i], freqs[i]));
					}
				}
				Collections.sort(tfs);
				String[] sortedFilteredTerms = new String[tfs.size()];
				for(int i = 0; i < tfs.size(); i++) {
					sortedFilteredTerms[i] = tfs.get(i).term;
				}
				return sortedFilteredTerms;
			}
		} catch(IOException e) {
			// Pass through
		}
		
		return new String[0];
	}
}
