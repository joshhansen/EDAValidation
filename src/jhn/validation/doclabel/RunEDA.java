package jhn.validation.doclabel;

import java.io.File;

import jhn.eda.EDA;
import jhn.eda.listeners.PrintFastState;

public class RunEDA extends jhn.eda.RunEDA {
	private static final int PRINT_INTERVAL = 1;
	protected int runCount;
	public RunEDA(String datasetName, int runCount, int iterations, int minCount) {
		this.datasetName = datasetName;
		this.runsDir = jhn.validation.Paths.edaRunsDir(datasetName);
		this.runCount = runCount;
		this.iterations = iterations;
		this.minCount = minCount;
		
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
	protected void addListeners(EDA eda) {
//		eda.addListener(new PrintState(PRINT_INTERVAL, runDir()));
		eda.addListener(new PrintFastState(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintReducedDocsLibSVM(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintReducedDocsLibSVM(PRINT_INTERVAL, runDir(), false));
//		eda.addListener(new PrintDocTopics(PRINT_INTERVAL, runDir()));
//		eda.addListener(new SerializeModel(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintTopDocTopics(PRINT_INTERVAL, runDir(), 10));
//		eda.addListener(new PrintTopTopicWords(PRINT_INTERVAL, runDir(), 10));
	}
	
	public static void main(String[] args) throws Exception {
		final String datasetName = "reuters21578_noblah";
		final int iterations = 50;
		final int runCount = 5;
		final int minCount = 2;
		RunEDA runner = new RunEDA(datasetName, runCount, iterations, minCount);
		runner.run();
	}

}
