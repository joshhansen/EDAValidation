package jhn.validation.doclabel;

import java.util.Calendar;

import jhn.eda.EDA;
import jhn.eda.EDA1;
import jhn.eda.EDA2;
import jhn.eda.EDA2_1;
import jhn.eda.Paths;
import jhn.eda.summarize.SampleSummarizer;
import jhn.eda.summarize.SumSampleSummarizer;

public class SummarizeSamples {
	public static void main(String[] args) throws Exception {
		final long start = Calendar.getInstance().getTimeInMillis();
		
//		Class<? extends EDA> algo = EDA1.class;
//		Class<? extends EDA> algo = EDA2.class;
		Class<? extends EDA> algo = EDA2_1.class;
		SampleSummarizer summarizer = new SumSampleSummarizer();
//		String datasetName = "reuters21578_noblah2";
//		String datasetName = "sotu_chunks";
		String datasetName = "toy_dataset4";
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		final boolean includeClass = true;
		final int runCount = 5;
		final int firstIter = 30;
		final int lastIter = 50;
		final int minCount = 0;
		
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");

			final String runDir = Paths.runDir(runsDir, run);
			jhn.eda.summarize.SummarizeSamples.summarize(summarizer, runDir, firstIter, lastIter, minCount, includeClass);
		}
		
		final long stop = Calendar.getInstance().getTimeInMillis();
		
		System.out.println("Duration: " + (stop-start) + "ms");
	}
}
