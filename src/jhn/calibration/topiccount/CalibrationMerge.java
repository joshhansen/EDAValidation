package jhn.calibration.topiccount;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.util.RandUtil;
import jhn.validation.LabelSource;

/**
 * 
 * @author Josh Hansen
 *
 * @param <K> The type of key the labels are indexed by
 */
public class CalibrationMerge<K> {
	private final File srcDir;
	private final String destFilename;
	private final int comparisonsPerPair;
	private final LabelSourceFactory<K> labelSrcFact;
	private final KeySource<K> keySource;
	public CalibrationMerge(String srcDir, String destFilename, int comparisonsPerPair,
			LabelSourceFactory<K> labelSrcFact, KeySource<K> keySource) {
		this.srcDir = new File(srcDir);
		this.destFilename = destFilename;
		this.comparisonsPerPair = comparisonsPerPair;
		this.labelSrcFact = labelSrcFact;
		this.keySource = keySource;
	}
	
	/** topicCount -> run -> label source */
	private class Labels {
		private Int2ObjectMap<Int2ObjectMap<LabelSource<K>>> labels = new Int2ObjectOpenHashMap<>();
		
		public void setLabelSource(int topicCount, int run, LabelSource<K> source) {
			getLabels(topicCount).put(run, source);
		}
		
		private Int2ObjectMap<LabelSource<K>> getLabels(int topicCount) {
			Int2ObjectMap<LabelSource<K>> map = labels.get(topicCount);
			if(map==null) {
				map = new Int2ObjectOpenHashMap<>();
				labels.put(topicCount, map);
			}
			return map;
		}
		
		private LabelSource<K> getLabels(int topicCount, int run) {
			return getLabels(topicCount).get(run);
		}
	}

	public interface LabelSourceFactory<Key> {
		LabelSource<Key> create(File file) throws Exception;
	}
	
	public interface KeySource<Key> {
		Key randomKey();
	}
	
	public void run() throws Exception {
		// Load label lines from disk
		Labels labels = new Labels();
		for(File file : srcDir.listFiles()) {
			Matcher m = jhn.validation.Paths.NAME_RGX.matcher(file.getName().split("[.]")[0]);
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			labels.setLabelSource(topicCount, run, labelSrcFact.create(file));
		}
		
		List<String> output = new ArrayList<>();
		for(Int2ObjectMap.Entry<Int2ObjectMap<LabelSource<K>>> entry1 : labels.labels.int2ObjectEntrySet()) {
			
			final int topicCount1 = entry1.getIntKey();
			final int[] runs1 = entry1.getValue().keySet().toIntArray();
			
			for(Int2ObjectMap.Entry<Int2ObjectMap<LabelSource<K>>> entry2 : labels.labels.int2ObjectEntrySet()) {
				final int topicCount2 = entry2.getIntKey();
				final int[] runs2 = entry2.getValue().keySet().toIntArray();
				
				if(topicCount1 != topicCount2) {
					for(int cmpNum = 0; cmpNum < comparisonsPerPair; cmpNum++) {
						final K key = keySource.randomKey();
						
						int run1 = RandUtil.randItem(runs1);
						int run2 = RandUtil.randItem(runs2);
						
						String label1 = labels.getLabels(topicCount1, run1).labels(key, 1)[0];
						String label2 = labels.getLabels(topicCount2, run2).labels(key, 1)[0];
						
						StringBuilder outputLine = new StringBuilder();
						outputLine.append(key).append(',');
						outputLine.append(topicCount1).append(',').append(run1).append(',');
						outputLine.append(topicCount2).append(',').append(run2).append(',');
						outputLine.append(label1).append(',').append(label2);
						output.add(outputLine.toString());
					}
				}
			}
		}
		
		// Output comparisons
		try(PrintWriter w = new PrintWriter(new FileWriter(destFilename))) {
			w.append("key,");
			
			final String[] sides = new String[]{"1","2"};
			for(String side : sides) {
				w.append("topicCount").append(side).append(',');
				w.append("run").append(side).append(',');
			}
			
			for(int i = 0; i < sides.length; i++) {
				String side = sides[i];
				w.append("topic").append(side).append(',');
				
//				for(int wordNum = 0; wordNum < 20; wordNum++) {
//					w.append("model").append(side).append("word").append(String.valueOf(wordNum)).append(',');
//				}
//				w.append("model").append(side).append("label");
//				if(i < sides.length - 1) w.append(',');
			}
			w.append('\n');
			
			Collections.shuffle(output);
			Collections.shuffle(output);
			Collections.shuffle(output);
			Collections.shuffle(output);
			Collections.shuffle(output);
			
			for(String outputLine : output) {
				w.append(outputLine).append('\n');
			}
		}
	}
}
