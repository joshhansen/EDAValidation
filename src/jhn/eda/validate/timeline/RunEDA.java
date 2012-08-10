package jhn.eda.validate.timeline;

import java.io.File;

import cc.mallet.types.InstanceList;

import jhn.eda.EDA;
import jhn.eda.Options;
import jhn.eda.Paths;
import jhn.eda.listeners.PrintFastState;
import jhn.eda.topiccounts.TopicCounts;
import jhn.eda.topicdistance.LuceneTopicDistanceCalculator;
import jhn.eda.topicdistance.TopicDistanceCalculator;
import jhn.eda.typetopiccounts.TypeTopicCounts;
import jhn.util.Config;
import jhn.util.ConstFactory;
import jhn.util.Factory;
import jhn.util.Util;

public class RunEDA {
	public static void run() throws Exception {
		final int window = 40;
		final int iterations = 500;
		final int minCount = 2;
		final String topicWordIdxName = "wp_lucene4";
		
		for(int startYear = 1790; startYear < 2000; startYear += 20) {
			int endYear = startYear + window;
			
			String datasetName = "sotu_" + startYear + "-" + endYear;
			
			System.out.print("Loading type-topic counts...");
			final String ttCountsFilename = Paths.typeTopicCountsFilename(topicWordIdxName, datasetName, minCount);
			TypeTopicCounts ttcs = (TypeTopicCounts) Util.deserialize(ttCountsFilename);
			System.out.println("done.");

			System.out.print("Loading topic counts...");
			final String topicCountsFilename = Paths.filteredTopicCountsFilename(topicWordIdxName, datasetName, minCount);
			TopicCounts tcs = (TopicCounts) Util.deserialize(topicCountsFilename);
			Factory<TopicCounts> tcFact = new ConstFactory<>(tcs);
			System.out.println("done.");

			Config props = (Config) Util.deserialize(Paths.propsFilename(topicWordIdxName, datasetName, minCount));
			
			TopicDistanceCalculator tdc = new LuceneTopicDistanceCalculator(null, null);
			
			final int run = Paths.nextRun();
			try(EDA eda = new EDA (tcFact, ttcs, tdc, props.getInt(Options.NUM_TOPICS), run)) {
				final int PRINT_INTERVAL = 1;
				eda.addListener(new PrintFastState(PRINT_INTERVAL, run));
				eda.conf.putDouble(Options.ALPHA_SUM, 10000);
				eda.conf.putDouble(Options.BETA, 0.01);
				eda.conf.putInt(Options.ITERATIONS, iterations);
				eda.conf.putInt(Options.MIN_THREADS, Runtime.getRuntime().availableProcessors());
				eda.conf.putInt(Options.MAX_THREADS, Runtime.getRuntime().availableProcessors()*3);
				
				System.out.print("Loading target corpus...");
				InstanceList targetData = InstanceList.load(new File(Paths.datasetFilename(datasetName)));
				System.out.println("done.");
				System.out.print("Processing target corpus...");
				eda.setTrainingData(targetData);
				System.out.println("done.");
				
				eda.sample();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		run();
	}
}
