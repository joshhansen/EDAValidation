package jhn.eda.validate.doclabel;

import java.io.File;

import jhn.eda.EDA;
import jhn.eda.listeners.PrintDocTopics;
import jhn.eda.listeners.PrintFastState;
import jhn.eda.listeners.PrintTopDocTopics;
import jhn.eda.listeners.PrintTopTopicWords;

public class RunEDA extends jhn.eda.RunEDA {
	protected int runCount;
	public RunEDA(String outputDir, int runCount) {
//		this.datasetName = "reuters21578_noblah";
		this.datasetName = "toy_dataset4";
		
		this.runsDir = outputDir + "/" + datasetName;
		this.runCount = runCount;
		this.iterations = 5;

		minCount = 0;
		
		new File(this.runsDir).mkdirs();
	}

	@Override
	public void run() throws Exception {
		super.loadAll();
		for(int i = 0; i < runCount; i++) {
			System.out.println("----- Run " + i + " -----");
			moveToNextRun();
			super.runEDA();
		}
	}
	
	@Override
	protected void addListeners(EDA eda) {
//		eda.addListener(new PrintState(PRINT_INTERVAL, runDir()));
		eda.addListener(new PrintFastState(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintReducedDocsLibSVM(PRINT_INTERVAL, runDir()));
//		eda.addListener(new PrintReducedDocsLibSVM(PRINT_INTERVAL, runDir(), false));
		eda.addListener(new PrintDocTopics(PRINT_INTERVAL, runDir()));
//		eda.addListener(new SerializeModel(PRINT_INTERVAL, runDir()));
		eda.addListener(new PrintTopDocTopics(PRINT_INTERVAL, runDir(), 10));
		eda.addListener(new PrintTopTopicWords(PRINT_INTERVAL, runDir(), 10));
	}
	
	public static void main(String[] args) throws Exception {
		final int runCount = 5;
		RunEDA runner = new RunEDA(jhn.eda.validate.Paths.outputDir()+"/doclabel", runCount);
		runner.run();
	}

}
