package jhn.validation.doclabel;

import java.io.IOException;

import jhn.eda.Paths;
import jhn.eda.SampleSummarizer;

public class SummarizeSamples {
	public static void main(String[] args) throws IOException {
		String datasetName = "toy_dataset4";
		String runsDir = jhn.validation.Paths.outputDir()+"/doclabel/" + datasetName;
		
		final int runCount = 5;
		final int lastN = 50;
		final int minCount = 0;
		
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");

			final String runDir = Paths.runDir(runsDir, run);
			SampleSummarizer.summarize(runDir, lastN, minCount, false);
		}
	}
}
