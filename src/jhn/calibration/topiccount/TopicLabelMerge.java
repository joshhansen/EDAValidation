package jhn.calibration.topiccount;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.util.RandUtil;
import jhn.validation.Paths;
import jhn.validation.StandardTopicLabelSource;
import jhn.validation.TopicLabelSource;

public class TopicLabelMerge {
	private final File srcDir;
	private final String destFilename;
	private final int comparisonsPerPair;
	
	public TopicLabelMerge(String srcDir, String destFilename, int comparisonsPerPair) {
		this.srcDir = new File(srcDir);
		this.destFilename = destFilename;
		this.comparisonsPerPair = comparisonsPerPair;
	}
	
	/** topicCount -> run -> label source */
	private static class Labels {
		private Int2ObjectMap<Int2ObjectMap<TopicLabelSource>> labels = new Int2ObjectOpenHashMap<>();
		
		public void setLabelSource(int topicCount, int run, TopicLabelSource source) {
			getLabels(topicCount).put(run, source);
		}
		
		private Int2ObjectMap<TopicLabelSource> getLabels(int topicCount) {
			Int2ObjectMap<TopicLabelSource> map = labels.get(topicCount);
			if(map==null) {
				map = new Int2ObjectOpenHashMap<>();
				labels.put(topicCount, map);
			}
			return map;
		}
		
		private TopicLabelSource getLabels(int topicCount, int run) {
			return getLabels(topicCount).get(run);
		}
	}

	
	public void run() throws FileNotFoundException, IOException {
		
		// Load label lines from disk
		Labels labels = new Labels();
		for(File file : srcDir.listFiles()) {
			Matcher m = jhn.validation.Paths.NAME_RGX.matcher(file.getName().split("[.]")[0]);
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			labels.setLabelSource(topicCount, run, new StandardTopicLabelSource(file.getName(), file.getPath()));
			
//			try(BufferedReader r = new BufferedReader(new FileReader(file))) {
//				String line = null;
//				while( (line=r.readLine()) != null) {
//					labels.addLabel(topicCount, run, line);
//				}
//			}
		}
		
		List<String> output = new ArrayList<>();
		for(Int2ObjectMap.Entry<Int2ObjectMap<TopicLabelSource>> entry1 : labels.labels.int2ObjectEntrySet()) {
			
			final int topicCount1 = entry1.getIntKey();
			final int[] runs1 = entry1.getValue().keySet().toIntArray();
			
			for(Int2ObjectMap.Entry<Int2ObjectMap<TopicLabelSource>> entry2 : labels.labels.int2ObjectEntrySet()) {
				final int topicCount2 = entry2.getIntKey();
				final int[] runs2 = entry2.getValue().keySet().toIntArray();
				
				if(topicCount1 != topicCount2) {
					for(int cmpNum = 0; cmpNum < comparisonsPerPair; cmpNum++) {
						int run1 = RandUtil.randItem(runs1);
						int run2 = RandUtil.randItem(runs2);
						
						int topicNum1 = RandUtil.rand.nextInt(topicCount1);
						int topicNum2 = RandUtil.rand.nextInt(topicCount2);
						
//						String labelLine1 = RandUtil.randItem(labels.getLabels(topicCount1, run1));
//						String labelLine2 = RandUtil.randItem(labels.getLabels(topicCount2, run2));
						String labelLine1 = labels.getLabels(topicCount1, run1).labels(topicNum1, 1)[0];
						String labelLine2 = labels.getLabels(topicCount2, run2).labels(topicNum2, 1)[0];
						
						StringBuilder outputLine = new StringBuilder();
						outputLine.append(topicCount1).append(',').append(run1).append(',');
						outputLine.append(topicCount2).append(',').append(run2).append(',');
						outputLine.append(labelLine1).append(',').append(labelLine2);
						output.add(outputLine.toString());
					}
				}
			}
		}
		
		// Output comparisons
		try(PrintWriter w = new PrintWriter(new FileWriter(destFilename))) {
			final String[] sides = new String[]{"1","2"};
			for(String side : sides) {
				w.append("topicCount").append(side).append(',');
				w.append("run").append(side).append(',');
				
			}
			
			for(int i = 0; i < sides.length; i++) {
				String side = sides[i];
				w.append("topic").append(side).append(',');
				
				for(int wordNum = 0; wordNum < 20; wordNum++) {
					w.append("model").append(side).append("word").append(String.valueOf(wordNum)).append(',');
				}
				w.append("model").append(side).append("label");
				if(i < sides.length - 1) w.append(',');
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
	
	public static void main(String[] args) throws Exception {
		final int comparisonsPerPair = 10;
		String datasetName = "reuters21578";
		TopicLabelMerge tccm = new TopicLabelMerge(
				Paths.topicCountCalibrationLauTopicLabelsDir(datasetName),
				Paths.topicCountCalibrationMergedTopicLabelsFilename(datasetName),
				comparisonsPerPair);
		tccm.run();
	}
}
