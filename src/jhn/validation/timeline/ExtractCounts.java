package jhn.validation.timeline;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;

import jhn.ExtractorParams;
import jhn.eda.CountsExtractor;
import jhn.eda.topiccounts.LuceneTopicCounts;
import jhn.eda.topiccounts.TopicCounts;
import jhn.eda.topictypecounts.LuceneTopicTypeCounts;
import jhn.eda.topictypecounts.TopicTypeCounts;
import jhn.eda.typetopiccounts.LuceneTypeTopicCounts;
import jhn.eda.typetopiccounts.TypeTopicCounts;
import jhn.util.Util;

public class ExtractCounts implements AutoCloseable {
	private final String topicWordIdxName;
	private IndexReader topicWordIdx;
	private TopicCounts srcTopicCounts;

	public ExtractCounts(String topicWordIdxName) throws Exception {
		this.topicWordIdxName = topicWordIdxName;
		String topicWordIdxLuceneDir = jhn.Paths.topicWordIndexDir(topicWordIdxName);
		topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxLuceneDir)));
		srcTopicCounts = new LuceneTopicCounts(topicWordIdx);
	}
	
	public void extract(ExtractorParams ep, String datasetFilename) throws Exception {
		System.out.println("Extracting " + ep.datasetName);
		
		String topicMappingFilename =              jhn.Paths.topicMappingFilename(ep);
		String propsFilename =                 jhn.Paths.propsFilename(ep);
		String topicCountsFilename =           jhn.Paths.topicCountsFilename(ep);
		String restrictedTopicCountsFilename = jhn.Paths.restrictedTopicCountsFilename(ep);
		String filteredTopicCountsFilename =   jhn.Paths.filteredTopicCountsFilename(ep);
		String typeTopicCountsFilename =       jhn.Paths.typeTopicCountsFilename(ep);
		
		// Load
		InstanceList targetData = InstanceList.load(new File(datasetFilename));
		Alphabet typeAlphabet = (Alphabet) targetData.getAlphabet().clone();
		final int typeCount = typeAlphabet.size();

		targetData = null;
		
		TypeTopicCounts srcTypeTopicCounts = new LuceneTypeTopicCounts(topicWordIdx, typeAlphabet);
		TopicTypeCounts srcTopicTypeCounts = new LuceneTopicTypeCounts(topicWordIdx, typeAlphabet);
		
		// Run
		try(CountsExtractor ce = new CountsExtractor(srcTopicCounts, srcTypeTopicCounts, srcTopicTypeCounts,
				typeCount, ep.minCount, topicMappingFilename, propsFilename, topicCountsFilename, restrictedTopicCountsFilename,
				filteredTopicCountsFilename, typeTopicCountsFilename)) {
			
			ce.extract();
		}
	}

	@Override
	public void close() throws Exception {
		Util.closeIfPossible(srcTopicCounts);
		topicWordIdx.close();
	}
	
	public static void main(String[] args) throws Exception {
		final int window = 40;
		ExtractorParams ep = new ExtractorParams();
		ep.topicWordIdxName = "wp_lucene4";
		ep.minCount = 2;
		
		try(ExtractCounts tec = new ExtractCounts("wp_lucene4")) {
			final String timelineDir = jhn.Paths.outputDir("LDA") + "/datasets/sotu_timeline";
			for(int startYear = 1790; startYear < 2000; startYear += 20) {
				int endYear = startYear + window;
				
				ep.datasetName = "sotu_" + startYear + "-" + endYear;
				String datasetFilename = timelineDir + "/" + startYear + "-" + endYear + ".mallet";
				tec.extract(ep, datasetFilename);
			}
		}
	}
}
