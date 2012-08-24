package jhn.calibration.topiccount;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import jhn.util.RandUtil;
import jhn.validation.LabelSource;

/**
 * 
 * @author Josh Hansen
 *
 * @param <K> The type of key the labels are indexed by
 */
public abstract class CalibrationMerge<K> {
	protected final File srcDir;
	protected final String destFilename;
	protected final int comparisonsPerPair;
	
	public CalibrationMerge(String srcDir, String destFilename, int comparisonsPerPair) {
		this.srcDir = new File(srcDir);
		this.destFilename = destFilename;
		this.comparisonsPerPair = comparisonsPerPair;
	}
	
	public void run() throws Exception {
		Labels<K> labels = loadLabels();
		
		List<String> output = randomize(merge(labels));
		
		try(PrintWriter w = new PrintWriter(new FileWriter(destFilename))) {
			write(output, w);
		}
	}

	protected List<String> randomize(List<String> output) {
		Collections.shuffle(output);
		Collections.shuffle(output);
		Collections.shuffle(output);
		Collections.shuffle(output);
		Collections.shuffle(output);
		return output;
	}

	protected void write(List<String> output, PrintWriter w) {
		w.append(headerLine());
		w.append('\n');
		
		for(String outputLine : output) {
			w.append(outputLine).append('\n');
		}
	}

	protected List<String> merge(Labels<K> labels) throws Exception {
		List<String> output = new ArrayList<>();
		for(Int2ObjectMap.Entry<Int2ObjectMap<LabelSource<K>>> entry1 : labels.labels.int2ObjectEntrySet()) {
			
			final int topicCount1 = entry1.getIntKey();
			final int[] runs1 = entry1.getValue().keySet().toIntArray();
			
			for(Int2ObjectMap.Entry<Int2ObjectMap<LabelSource<K>>> entry2 : labels.labels.int2ObjectEntrySet()) {
				final int topicCount2 = entry2.getIntKey();
				final int[] runs2 = entry2.getValue().keySet().toIntArray();
				
				if(topicCount1 != topicCount2) {
					for(int cmpNum = 0; cmpNum < comparisonsPerPair; cmpNum++) {
						int run1 = RandUtil.randItem(runs1);
						int run2 = RandUtil.randItem(runs2);
						
						output.add(mergeLine(labels, topicCount1, run1, topicCount2, run2));
					}
				}
			}
		}
		return output;
	}

	protected Labels<K> loadLabels() throws Exception {
		Labels<K> labels = new Labels<K>();
		for(File file : srcDir.listFiles()) {
			Matcher m = jhn.validation.Paths.NAME_RGX.matcher(file.getName().split("[.]")[0]);
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			labels.setLabelSource(topicCount, run, createLabelSource(file));
		}
		return labels;
	}
	
	protected abstract LabelSource<K> createLabelSource(File file) throws Exception;

	protected abstract String headerLine();

	protected abstract String mergeLine(Labels<K> labels, final int topicCount1, final int run1, final int topicCount2, final int run2) throws Exception;
}
