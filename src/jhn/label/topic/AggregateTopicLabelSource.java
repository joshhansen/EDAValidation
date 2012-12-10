package jhn.label.topic;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.io.TopicLabel;
import jhn.io.TopicLabelFileReader;
import jhn.util.RandUtil;

public class AggregateTopicLabelSource implements SampleableTopicLabelSource {
	private int nextTopicNum = 0;
	private Int2ObjectMap<String[]> labels = new Int2ObjectOpenHashMap<>();
	private Int2ObjectMap<String[]> words = new Int2ObjectOpenHashMap<>();
	
	public AggregateTopicLabelSource(String topicLabelsDir, Pattern filenameRgx) throws Exception {
		System.out.println(topicLabelsDir);
		
		for(File f : new File(topicLabelsDir).listFiles()) {
			Matcher m = filenameRgx.matcher(f.getName());
			if(m.matches()) {
				try(TopicLabelFileReader r = new TopicLabelFileReader(f.getPath())) {
					for(TopicLabel tl : r) {
						labels.put(nextTopicNum, new String[]{tl.label()});
						words.put(nextTopicNum, tl.words());
						nextTopicNum++;
					}
				}
			}
//			Matcher m = filenameRgx.matcher(f.getName());
//			if(m.matches()) {
//				int run = Integer.parseInt(m.group(1));
//				allLabels.put(run, ctor.newInstance(f.getPath()));
//			}
		}
		if(labels.size() < 1) {
			throw new IllegalArgumentException("No labels found in directory " + topicLabelsDir + " matching regex " + filenameRgx.pattern());
		}
	}
	
	protected AggregateTopicLabelSource() {
		// Override if desired
	}

//	@Override
//	public String[] labels(Integer topicNum, int numLabels) {
//		int run = RandUtil.randItem(allLabels.keySet().toIntArray());
//		System.out.println("Run: " + run);
//		String[] labels = allLabels.get(run).labels(docFilename, numLabels);
//		
//		String[] selectedLabels = new String[numLabels];
//		for(int i = 0; i < selectedLabels.length; i++) {
//			selectedLabels[i] = labels[i];
//		}
//		return selectedLabels;
//	}

	@Override
	public String[] labels(int topicNum, int numLabels) {
		return labels.get(topicNum);
	}
	
	@Override
	public String[] labels(Integer labelKey, int numLabels) {
		return labels(labelKey.intValue(), numLabels);
	}

	@Override
	public String[] topicWords(int topicNum) {
		return words.get(topicNum);
	}

	@Override
	public int randTopicNum() {
		return RandUtil.rand.nextInt(nextTopicNum);
	}


}
