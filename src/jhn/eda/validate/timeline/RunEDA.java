package jhn.eda.validate.timeline;

import jhn.eda.EDA;
import jhn.eda.listeners.PrintFastState;

public class RunEDA extends jhn.eda.RunEDA {
	
	private final int window;
	
	public RunEDA() {
		super();
		window = 40;
	}

	@Override
	public void run() throws Exception {
		for(int startYear = 1790; startYear < 2000; startYear += 20) {
			int endYear = startYear + window;
			
			this.datasetName = "sotu_" + startYear + "-" + endYear;
			
			super.run();
		}
	}
	
	@Override
	protected void addListeners(EDA eda) {
		eda.addListener(new PrintFastState(PRINT_INTERVAL, runDir()));
	}

	public static void main(String[] args) throws Exception {
		RunEDA runner = new RunEDA();
		runner.run();
	}
}
