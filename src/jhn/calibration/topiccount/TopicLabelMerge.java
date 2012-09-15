package jhn.calibration.topiccount;

import java.io.File;
import java.util.regex.Matcher;

import jhn.label.topic.StandardTopicLabelSource;
import jhn.util.RandUtil;
import jhn.validation.Paths;

public final class TopicLabelMerge extends CalibrationMerge<Integer> {

	public TopicLabelMerge(	String datasetName, int comparisonsPerPair) {
		super(Paths.lauTopicLabelsDir(datasetName),
			  Paths.mergedLauTopicLabelsFilename(datasetName),
			  comparisonsPerPair);
	}
	
	@Override
	protected Labels<Integer> loadLabels() throws Exception {
		Labels<Integer> labels = new Labels<>();
		for(File file : srcDir.listFiles()) {
			Matcher m = jhn.validation.Paths.NAME_RGX.matcher(file.getName().split("[.]")[0]);
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			labels.setLabelSource(topicCount, run, createLabelSource(file));
		}
		return labels;
	}

	@Override
	protected StandardTopicLabelSource createLabelSource(File file) throws Exception {
		return new StandardTopicLabelSource(file.getPath());
	}

	@Override
	protected String headerLine() {
		StringBuilder header = new StringBuilder();
		
		
		final String[] sides = new String[]{"1","2"};
		for(String side : sides) {
			header.append("topicCount").append(side).append(',');
			header.append("run").append(side).append(',');
			header.append("topic").append(side).append(',');
			header.append("topic").append(side).append("label,");
		}
		
		for(int i = 0; i < sides.length; i++) {
			String side = sides[i];
			
			for(int wordNum = 0; wordNum < 20; wordNum++) {
				header.append("model").append(side).append("word").append(String.valueOf(wordNum)).append(',');
			}
			header.append("model").append(side).append("label");
			if(i < sides.length - 1) header.append(',');
		}
		return header.toString();
	}

	@Override
	protected String mergeLine(Labels<Integer> labels, int topicCount1, int run1, int topicCount2, int run2) {
		StringBuilder outputLine = new StringBuilder();
		
		final int topic1 = randomTopic(topicCount1);
		final int topic2 = randomTopic(topicCount2);
		
		final StandardTopicLabelSource labelSource1 = (StandardTopicLabelSource)labels.getLabels(topicCount1, run1);
		final StandardTopicLabelSource labelSource2 = (StandardTopicLabelSource)labels.getLabels(topicCount2, run2);
		
		String[] topic1words = labelSource1.topicWords(topic1);
		String[] topic2words = labelSource2.topicWords(topic2);
		
		String label1 = labelSource1.labels(topic1, 1)[0];
		String label2 = labelSource2.labels(topic2, 1)[0];
		
		outputLine.append(topicCount1).append(',')
		          .append(run1).append(',')
		          .append(topic1).append(',')
		          .append('"').append(label1).append("\",");
		outputLine.append(topicCount2).append(',')
		          .append(run2).append(',')
		          .append(topic2).append(',')
		          .append('"').append(label2).append("\",");
		
		for(String topicWord : topic1words) {
			outputLine.append(topicWord).append(',');
		}
		for(int i = 0; i < topic2words.length; i++) {
			outputLine.append(topic2words[i]);
			if(i < topic2words.length - 1) {
				outputLine.append(',');
			}
		}
		
		return outputLine.toString();
	}
	
	private static int randomTopic(int topicCount) {
		return RandUtil.rand.nextInt(topicCount);
	}
	
	public static void main(String[] args) throws Exception {
		final int comparisonsPerPair = 10;
//		String datasetName = "reuters21578";
		String datasetName = "state_of_the_union";
		CalibrationMerge<Integer> tccm = new TopicLabelMerge(datasetName, comparisonsPerPair);
		tccm.run();
	}
}
