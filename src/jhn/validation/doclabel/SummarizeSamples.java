package jhn.validation.doclabel;

import java.util.Calendar;

import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.LDASTWD;
import jhn.eda.EDA;
import jhn.eda.Paths;
import jhn.eda.summarize.Sum;
import jhn.eda.summarize.SummaryParams;

public class SummarizeSamples {
	public static void main(String[] args) throws Exception {
//		Class<? extends EDA> algo = EDA1.class;
//		Class<? extends EDA> algo = EDA2.class;
		Class<? extends ProbabilisticExplicitTopicModel> algo = EDA.class;
		
		String datasetName = "reuters21578_noblah2";
//		String datasetName = "sotu_chunks";
//		String datasetName = "toy_dataset4";
		
		final int runCount = 5;
		
		SummaryParams sp = new SummaryParams();
		sp.summarizerCls = Sum.class;
		sp.includeClass = true;
		sp.firstIter = 10;
		sp.lastIter = 50;
		sp.minCount = 0;
		
		summarizeSamples(algo, datasetName, runCount, sp);
	}
	
	public static void summarizeSamples(Class<? extends ProbabilisticExplicitTopicModel> algo, String datasetName, int runCount, SummaryParams sp) throws Exception {
		final long start = Calendar.getInstance().getTimeInMillis();
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		
		for(int run = 0; run < runCount; run++) {
			System.out.println("----- Run " + run + " -----");

			final String runDir = Paths.runDir(runsDir, run);
			jhn.eda.summarize.SummarizeSamples.summarize(runDir, sp);
		}
		
		final long stop = Calendar.getInstance().getTimeInMillis();
		
		System.out.println("Duration: " + (stop-start) + "ms");
	}
}
