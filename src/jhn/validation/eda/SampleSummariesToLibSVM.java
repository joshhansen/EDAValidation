package jhn.validation.eda;

import jhn.eda.EDA;
import jhn.eda.EDA1;
import jhn.eda.io.SampleSummaryToLibSVM;

public class SampleSummariesToLibSVM {
	public static void main(String[] args) throws Exception {
		final Class<? extends EDA> algo = EDA1.class;
		final int minCount = 2;
		final boolean includesClass = true;
		final String datasetName = "reuters21578_noblah2";// toy_dataset2 debates2012 sacred_texts state_of_the_union reuters21578
		final String summarizer = "sum";
		final int firstIter = 11;
		final int lastIter = 50;
		final int runCount = 5;
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		
		for(int run = 0; run < runCount; run++) {
			String runDir = jhn.eda.Paths.runDir(runsDir, run);
			String sampleSummaryFilename = jhn.eda.Paths.sampleSummaryFilename(summarizer, runDir, firstIter, lastIter, minCount, includesClass);
			String outputFilename =       jhn.validation.Paths.edaLibSvmFilename(algo, datasetName, summarizer, run, firstIter, lastIter, minCount, includesClass);
			SampleSummaryToLibSVM.convert(sampleSummaryFilename, outputFilename);
		}
	}
}
