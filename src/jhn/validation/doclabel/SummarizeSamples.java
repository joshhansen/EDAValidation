package jhn.validation.doclabel;

import java.io.IOException;
import java.util.Calendar;

import jhn.eda.Paths;
import jhn.eda.SampleSummarizer;

public class SummarizeSamples {
	public static void main(String[] args) throws IOException {
		final long start = Calendar.getInstance().getTimeInMillis();
		String datasetName = "toy_dataset4";
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(datasetName);
		final int runCount = 5;
		final int lastN = 10;
		final int minCount = 2;
		
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");

			final String runDir = Paths.runDir(runsDir, run);
			SampleSummarizer.summarize(runDir, lastN, minCount);
		}
		
		final long stop = Calendar.getInstance().getTimeInMillis();
		
		System.out.println("Duration: " + (stop-start) + "ms");
	}
}
