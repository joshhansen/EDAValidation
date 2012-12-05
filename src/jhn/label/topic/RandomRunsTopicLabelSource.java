package jhn.label.topic;

import java.util.regex.Pattern;

import jhn.label.RandomRunsLabelSource;

public class RandomRunsTopicLabelSource extends RandomRunsLabelSource<Integer> {
	public RandomRunsTopicLabelSource(String docLabelsDir, Pattern filenameRgx) throws Exception {
		super(StandardTopicLabelSource.class, docLabelsDir, filenameRgx);
	}
}
