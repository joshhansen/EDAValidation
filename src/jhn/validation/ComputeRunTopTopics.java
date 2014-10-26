package jhn.validation;

import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

import jhn.counts.i.i.IntIntCounter;
import jhn.counts.i.i.IntIntRAMCounter;
import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.LDASTWD;
import jhn.eda.EDA;
import jhn.eda.Paths;
import jhn.eda.io.SampleSummaryFileReader;
import jhn.eda.summarize.Sum;
import jhn.eda.summarize.SummaryParams;
import jhn.eda.tokentopics.DocTopicCounts;
import jhn.io.TopTopicsWriter;

public class ComputeRunTopTopics {
	public static void main(String[] args) throws Exception {
//		Class<? extends EDA> algo = LDASTWD.class;
		Class<? extends ProbabilisticExplicitTopicModel> algo = EDA.class;
//		SampleSummarizer summarizer = new Sum();
//		String datasetName = "reuters21578_noblah2";
		String datasetName = "sotu_chunks";
		
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		final int topicCount = 100;
		final int runCount = 5;
		
		SummaryParams sp = new SummaryParams();
		sp.summarizerCls = Sum.class;
		sp.firstIter = 11;
		sp.lastIter = 50;
		sp.minCount = 0;
		sp.includeClass = true;
		
		
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");
			
			IntIntCounter topicCounts = new IntIntRAMCounter();

			final String runDir = Paths.runDir(runsDir, run);
			String summaryFilename = Paths.sampleSummaryFilename(runDir, sp);
			try(SampleSummaryFileReader r = new SampleSummaryFileReader(summaryFilename)) {
				for(DocTopicCounts dtc : r) {
					while(dtc.hasNext()) {
						topicCounts.inc(dtc.nextInt(), dtc.nextDocTopicCount());
					}
				}
			}
			
			String topTopicsFilename = jhn.validation.Paths.topTopicsFilename(algo, datasetName, run);
			List<Int2IntMap.Entry> entries = topicCounts.fastTopN(topicCount);
			try(TopTopicsWriter w = new TopTopicsWriter(topTopicsFilename)) {
				for(Int2IntMap.Entry entry : entries) {
					w.topic(entry.getIntKey(), entry.getIntValue());
				}
			}
		}
	}
}
