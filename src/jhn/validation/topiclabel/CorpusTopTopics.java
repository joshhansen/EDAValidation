package jhn.validation.topiclabel;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import jhn.counts.i.i.IntIntCounter;
import jhn.counts.i.i.IntIntRAMCounter;
import jhn.eda.EDA;
import jhn.eda.EDA1;
import jhn.eda.EDA2;
import jhn.eda.EDA2_1;
import jhn.eda.Paths;
import jhn.eda.io.SampleSummaryFileReader;
import jhn.eda.summarize.SampleSummarizer;
import jhn.eda.summarize.SumSampleSummarizer;
import jhn.eda.tokentopics.DocTopicCounts;
import jhn.io.TopTopicsWriter;

public class CorpusTopTopics {
	public static void main(String[] args) throws Exception {
//		Class<? extends EDA> algo = EDA1.class;
//		Class<? extends EDA> algo = EDA2.class;
		Class<? extends EDA> algo = EDA2_1.class;
		SampleSummarizer summarizer = new SumSampleSummarizer();
//		String datasetName = "reuters21578_noblah2";
//		String datasetName = "sotu_chunks";
		String datasetName = "toy_dataset4";
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		final int topicCount = 100;
		final boolean includeClass = true;
		final int runCount = 1;
		final int firstIter = 31;
		final int lastIter = 50;
		final int minCount = 0;
		
		IntIntCounter topicCounts = new IntIntRAMCounter();
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");

			final String runDir = Paths.runDir(runsDir, run);
			String summaryFilename = Paths.sampleSummaryFilename(summarizer.name(), runDir, firstIter, lastIter, minCount, includeClass);
			try(SampleSummaryFileReader r = new SampleSummaryFileReader(summaryFilename)) {
				for(DocTopicCounts dtc : r) {
					while(dtc.hasNext()) {
						topicCounts.inc(dtc.nextInt(), dtc.nextDocTopicCount());
					}
				}
			}
		}
		
		String topTopicsFilename = jhn.validation.Paths.topTopicsFilename(algo, datasetName);
		List<Int2IntMap.Entry> entries = topicCounts.fastTopN(topicCount);
		try(TopTopicsWriter w = new TopTopicsWriter(topTopicsFilename)) {
			for(Int2IntMap.Entry entry : entries) {
				w.topic(entry.getIntKey(), entry.getIntValue());
			}
		}
	}
}
