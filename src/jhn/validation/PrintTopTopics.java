package jhn.validation;

import jhn.ExtractorParams;
import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.LDASTWD;
import jhn.eda.EDA;
import jhn.idx.IntIndex;
import jhn.io.TopTopicsReader;
import jhn.label.LabelSource;
import jhn.label.topic.LuceneTopicLabelSource;
import jhn.util.Util;

public class PrintTopTopics {
	public static void main(String[] args) throws Exception {
//		Class<? extends EDA> algo = LDASTWD.class;
		Class<? extends ProbabilisticExplicitTopicModel> algo = EDA.class;
		final int run = 0;
		
		ExtractorParams ep = new ExtractorParams()
				.topicWordIdxName("wp_lucene4")
				.datasetName("sotu_chunks")
				.minCount(2);
		
		
		
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir(ep.topicWordIdxName);
		LabelSource<Integer> topicWordIdx = new LuceneTopicLabelSource(topicWordIdxDir, ep.datasetName);
		
		String topicMappingFilename = jhn.Paths.topicMappingFilename(ep);
		IntIndex topicMapping = (IntIndex) Util.deserialize(topicMappingFilename);
		
		int topicNum;
		int realTopicNum;
		int count;
		String label;
		String topTopicsFilename = jhn.validation.Paths.topTopicsFilename(algo, ep.datasetName, run);
		try(TopTopicsReader r = new TopTopicsReader(topTopicsFilename)) {
			System.out.println("rank|label|topicNum|realTopicNum|count");
			int i = 1;
			while(r.hasNext()) {
				topicNum = r.nextInt();
				realTopicNum = topicMapping.objectAtI(topicNum);
//				realTopicNum = topicMapping.indexOfI(topicNum);
				count = r.nextTopicCount();
				label = topicWordIdx.labels(realTopicNum, 1)[0];
				System.out.println(i + "|" + label + "|" + topicNum + "|" + realTopicNum + "|" + count);
//				System.out.println("#" + i + " " + label + " [" + topicNum  + "/" + realTopicNum + "]: " + count);
				i++;
			}
		}
		
	}
}
