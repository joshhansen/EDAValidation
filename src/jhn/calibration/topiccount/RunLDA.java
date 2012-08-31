package jhn.calibration.topiccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class RunLDA {
	/** 
	 * Reads everything from an InputStream and writes it to an OutputStream (includes System.out and System.err) until
	 * the InputStream is empty.
	 * 
	 * Originally from http://stackoverflow.com/a/1732506
	 */
	private static class StreamGobbler extends Thread {
		private InputStream is;
		private Writer out;

		public StreamGobbler(InputStream is, OutputStream out) {
			this.is = is;
			this.out = new OutputStreamWriter(out);
		}

		@Override
		public void run() {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line = null;
				while ((line = br.readLine()) != null) {
					out.write(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	private static String malletBin() {
		return jhn.Paths.homeDir()+"/Projects/External/mallet-2.0.7/bin/mallet";
	}
	
	private static final int DOC_TOPICS_MAX = 10;
	private static final int OPTIMIZE_INTERVAL = 10;
	private static final int NUM_ITERATIONS = 500;
	
	public static void generate(String datasetName, int[] topicCounts, int numRuns) throws IOException, InterruptedException {
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
				
				Process p = Runtime.getRuntime().exec(cmd.toString());
				StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), System.out);
				StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), System.out);
				errorGobbler.start();
				outputGobbler.start();
				p.waitFor();
			}
		}
	}
	
	private static void ensureDirs(String dataset) {
		new File(jhn.validation.Paths.topicCountCalibrationKeysDir(dataset)).mkdirs();
		new File(jhn.validation.Paths.topicCountCalibrationStateDir(dataset)).mkdirs();
		new File(jhn.validation.Paths.topicCountCalibrationDocTopicsDir(dataset)).mkdirs();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
//		String datasetName = "reuters21578_noblah";
		String datasetName = "state_of_the_union";
		int[] topicCounts = new int[]{10,20,50,100,200};
		int numRuns = 5;
		generate(datasetName, topicCounts, numRuns);
	}
}
