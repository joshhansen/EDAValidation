package jhn.validation.doclabel;

import java.io.File;

import jhn.ExtractorParams;
import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.LDASTWD;
import jhn.eda.EDA;
import jhn.eda.listeners.PrintFasterState;

public class RunEDA extends jhn.eda.RunEDA {
	private static final int PRINT_STATE_INTERVAL = 10;
	protected int runCount;
	public RunEDA(Class<? extends ProbabilisticExplicitTopicModel> algo, ExtractorParams ep, int runCount, int iterations, boolean outputClass) {
		super();
		this.ep = ep;
		this.algo = algo;
		this.runsDir = jhn.validation.Paths.edaRunsDir(algo, ep.datasetName);
		this.runCount = runCount;
		this.iterations = iterations;
		this.outputClass = outputClass;
		
		new File(this.runsDir).mkdirs();
	}

	@Override
	public void run() throws Exception {
		super.loadAll();
		for(int i = 0; i < runCount; i++) {
			moveToNextRun();
			System.out.println("----- Run " + run + " -----");
			super.runEDA();
		}
		super.unloadAll();
	}
	
	@Override
	protected void addListeners(ProbabilisticExplicitTopicModel eda) throws NoSuchMethodException, SecurityException, FileNotFoundException {
//		eda.addListener(new PrintState(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintFastState(PRINT_INTERVAL, runDir(), outputClass));
		eda.addListener(new PrintFasterState(PRINT_STATE_INTERVAL, runDir(), outputClass));
//		eda.addListener(new PrintReducedDocsLibSVM(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintReducedDocsLibSVM(PRINT_INTERVAL, runDir(), false));
//		eda.addListener(new PrintDocTopics(PRINT_INTERVAL, runDir()));
//		eda.addListener(new SerializeModel(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintTopDocTopics(PRINT_INTERVAL, runDir(), 10));
//		eda.addListener(new PrintTopTopicWords(PRINT_INTERVAL, runDir(), 10));
	}
	
	public static void main(String[] args) throws Exception {
		Class<? extends ProbabilisticExplicitTopicModel> algo = LDASTWD.class;
//		Class<? extends EDA> algo = EDA2_1.class;
		
		ExtractorParams ep = new ExtractorParams();
		ep.topicWordIdxName = "wp_lucene4";
		
//		ep.datasetName = "reuters21578_noblah2";
		ep.datasetName = "sotu_chunks";
//		ep.datasetName = "toy_dataset4";
		
		ep.minCount = 2;
		
		final boolean outputClass = true;
		final int iterations = 50;
		final int runCount = 1;
		
		run(algo, ep, runCount, iterations, outputClass);
	}

}
