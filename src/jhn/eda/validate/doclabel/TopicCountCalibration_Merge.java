package jhn.eda.validate.doclabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.eda.validate.Paths;
import jhn.util.FileExtensionFilter;
import jhn.util.RandUtil;

public class TopicCountCalibration_Merge {
	private final File srcDir;
	private final String destFilename;
	private final int comparisonsPerPair;
	
	public TopicCountCalibration_Merge(String srcDir, String destFilename, int comparisonsPerPair) {
		this.srcDir = new File(srcDir);
		this.destFilename = destFilename;
		this.comparisonsPerPair = comparisonsPerPair;
	}
	
	private static class Labels {
		private Int2ObjectMap<Int2ObjectMap<List<String>>> labels = new Int2ObjectOpenHashMap<>();
		
		public void addLabel(int topicCount, int run, String labelLine) {
			getLabels(topicCount, run).add(labelLine);
		}
		
		private Int2ObjectMap<List<String>> getLabels(int topicCount) {
			Int2ObjectMap<List<String>> map = labels.get(topicCount);
			if(map==null) {
				map = new Int2ObjectOpenHashMap<>();
				labels.put(topicCount, map);
			}
			return map;
		}
		
		private List<String> getLabels(int topicCount, int run) {
			Int2ObjectMap<List<String>> map = getLabels(topicCount);
			List<String> list = map.get(run);
			if(list == null) {
				list = new ArrayList<>();
				map.put(run, list);
			}
			return list;
		}
	}
	

	private static final Pattern rgx = Pattern.compile("lda(\\d+)topics_(\\d+)[.]lau_labels");
	public void run() throws FileNotFoundException, IOException {
		
		// Load label lines from disk
		Labels labels = new Labels();
		for(File file : srcDir.listFiles(new FileExtensionFilter("lau_labels"))) {
			Matcher m = rgx.matcher(file.getName());
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			try(BufferedReader r = new BufferedReader(new FileReader(file))) {
				String line = null;
				while( (line=r.readLine()) != null) {
					labels.addLabel(topicCount, run, line);
				}
			}
		}
		
		List<String> output = new ArrayList<>();
		for(Int2ObjectMap.Entry<Int2ObjectMap<List<String>>> entry1 : labels.labels.int2ObjectEntrySet()) {
			
			final int topicCount1 = entry1.getIntKey();
			final int[] runs1 = entry1.getValue().keySet().toIntArray();
			
			for(Int2ObjectMap.Entry<Int2ObjectMap<List<String>>> entry2 : labels.labels.int2ObjectEntrySet()) {
				final int topicCount2 = entry2.getIntKey();
				final int[] runs2 = entry2.getValue().keySet().toIntArray();
				
				if(topicCount1 != topicCount2) {
					for(int cmpNum = 0; cmpNum < comparisonsPerPair; cmpNum++) {
						int run1 = RandUtil.randItem(runs1);
						int run2 = RandUtil.randItem(runs2);
						
						String labelLine1 = RandUtil.randItem(labels.getLabels(topicCount1, run1));
						String labelLine2 = RandUtil.randItem(labels.getLabels(topicCount2, run2));
						
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
		String datasetName = "reuters21578";
		TopicCountCalibration_Merge tccm = new TopicCountCalibration_Merge(
				Paths.topicCountCalibrationDir(datasetName),
				Paths.topicCountCalibrationDir(datasetName)+"/../" + datasetName + "_2.csv",
				10);
		tccm.run();
	}
}
