package jhn.calibration.topiccount;

import java.io.IOException;

public class RunLDA {
	private static String malletBin() {
		return jhn.Paths.homeDir()+"/Projects/External/mallet-2.0.7/bin/mallet";
	}
	
	private static final int DOC_TOPICS_MAX = 10;
	private static final int OPTIMIZE_INTERVAL = 10;
	private static final int NUM_ITERATIONS = 500;
	
	public static void generate(String datasetName, int[] topicCounts, int numRuns) throws IOException {
		ensureDirs(datasetName);
		for(int numTopics : topicCounts) {
			for(int run = 0; run < numRuns; run++) {
				System.out.println("K="+numTopics+" #"+run);
				StringBuilder cmd = new StringBuilder();
				cmd.append(malletBin());
				cmd.append(" train-topics --input ");
				cmd.append(jhn.Paths.malletDatasetFilename(datasetName));
				cmd.append(" --output-topic-keys ");
				cmd.append(jhn.validation.Paths.topicCountCalibrationKeysFilename(datasetName, numTopics, run));
				cmd.append(" --output-state ");
				cmd.append(jhn.validation.Paths.topicCountCalibrationStateFilename(datasetName, numTopics, run));
				cmd.append(" --output-doc-topics ");
				cmd.append(jhn.validation.Paths.topicCountCalibrationDocTopicsFilename(datasetName, numTopics, run));
				cmd.append(" --doc-topics-max ");
				cmd.append(DOC_TOPICS_MAX);
				cmd.append(" --num-topics ");
				cmd.append(numTopics);
				cmd.append(" --optimize-interval ");
				cmd.append(OPTIMIZE_INTERVAL);
				cmd.append(" --num-iterations ");
				cmd.append(NUM_ITERATIONS);
				System.out.println(cmd);
				Runtime.getRuntime().exec(cmd.toString());
			}
		}
	}
	
	private static void ensureDirs(String dataset) {
		new File(jhn.validation.Paths.topicCountCalibrationKeysDir(dataset)).mkdirs();
		new File(jhn.validation.Paths.topicCountCalibrationStateDir(dataset)).mkdirs();
		new File(jhn.validation.Paths.topicCountCalibrationDocTopicsDir(dataset)).mkdirs();
	}
	
	public static void main(String[] args) throws IOException {
//		String datasetName = "reuters21578_noblah";
		String datasetName = "state_of_the_union";
		int[] topicCounts = new int[]{10,20,50,100,200};
		int numRuns = 5;
		generate(datasetName, topicCounts, numRuns);
	}
}
