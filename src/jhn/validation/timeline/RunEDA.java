package jhn.validation.timeline;

import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.listeners.PrintFastState;

public class RunEDA extends jhn.eda.RunEDA {
	private static final int PRINT_INTERVAL = 1;
	private final int window;
	
	public RunEDA() {
		super();
		window = 40;
	}

	@Override
	public void run() throws Exception {
		for(int startYear = 1790; startYear < 2000; startYear += 20) {
			int endYear = startYear + window;
			
			this.ep.datasetName = "sotu_" + startYear + "-" + endYear;
			
			super.run();
		}
	}
	
	@Override
	protected void addListeners(ProbabilisticExplicitTopicModel eda) throws NoSuchMethodException, SecurityException {
		eda.addListener(new PrintFastState(PRINT_INTERVAL, runDir()));
	}

	public static void main(String[] args) throws Exception {
		RunEDA runner = new RunEDA();
		runner.run();
	}
}
