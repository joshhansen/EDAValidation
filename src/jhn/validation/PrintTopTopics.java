package jhn.validation;

import jhn.eda.EDA;
import jhn.eda.EDA1;
import jhn.eda.EDA2;
import jhn.eda.EDA2_1;
import jhn.idx.IntIndex;
import jhn.io.TopTopicsReader;
import jhn.label.LabelSource;
import jhn.label.topic.LuceneTopicLabelSource;
import jhn.util.Util;

public class PrintTopTopics {
	public static void main(String[] args) throws Exception {
//		Class<? extends EDA> algo = EDA1.class;
//		Class<? extends EDA> algo = EDA2.class;
		Class<? extends EDA> algo = EDA2_1.class;
//		String datasetName = "reuters21578_noblah2";
//		String datasetName = "sotu_chunks";
		String datasetName = "toy_dataset4";
//		final int run = 0;
		final int minCount = 0;
		
		
		final String topicWordIdxName = "wp_lucene4";
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir(topicWordIdxName);
		LabelSource<Integer> topicWordIdx = new LuceneTopicLabelSource(topicWordIdxDir, datasetName);
		
		String topicMappingFilename = jhn.Paths.topicMappingFilename(topicWordIdxName, datasetName, minCount);
		IntIndex topicMapping = (IntIndex) Util.deserialize(topicMappingFilename);
		
		int topicNum;
		int realTopicNum;
		int count;
		String label;
		String topTopicsFilename = jhn.validation.Paths.topTopicsFilename(algo, datasetName);
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
