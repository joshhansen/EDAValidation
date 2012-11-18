package jhn.validation.doclabel;

import java.util.Calendar;

import jhn.eda.Paths;
import jhn.eda.summarize.SampleSummarizer;
import jhn.eda.summarize.SumSampleSummarizer;

public class SummarizeSamples {
	public static void main(String[] args) throws Exception {
		SampleSummarizer summarizer = new SumSampleSummarizer();
		final long start = Calendar.getInstance().getTimeInMillis();
		String datasetName = "sotu_chunks";
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(datasetName);
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
