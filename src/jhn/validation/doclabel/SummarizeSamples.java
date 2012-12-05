package jhn.validation.doclabel;

import java.util.Calendar;

import jhn.eda.EDA;
import jhn.eda.EDA1;
import jhn.eda.Paths;
import jhn.eda.summarize.SampleSummarizer;
import jhn.eda.summarize.SumSampleSummarizer;

public class SummarizeSamples {
	public static void main(String[] args) throws Exception {
		final long start = Calendar.getInstance().getTimeInMillis();
		
		Class<? extends EDA> algo = EDA1.class;
		SampleSummarizer summarizer = new SumSampleSummarizer();
		String datasetName = "reuters21578_noblah2";
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		final boolean includeClass = true;
		final int runCount = 5;
		final int firstIter = 10;
		final int lastIter = 100;
		final int minCount = 2;
		
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");

			final String runDir = Paths.runDir(runsDir, run);
			jhn.eda.summarize.SummarizeSamples.summarize(summarizer, runDir, firstIter, lastIter, minCount, includeClass);
		}
		
		final long stop = Calendar.getInstance().getTimeInMillis();
		
		System.out.println("Duration: " + (stop-start) + "ms");
	}
}
