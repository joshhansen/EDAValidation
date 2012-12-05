package jhn.label.doc;

import java.util.regex.Pattern;

import jhn.label.RandomRunsLabelSource;

public class RandomRunsDocLabelSource extends RandomRunsLabelSource<String> {
	public RandomRunsDocLabelSource(String docLabelsDir, Pattern filenameRgx) throws Exception {
		super(StandardDocLabelSource.class, docLabelsDir, filenameRgx);
	}
}
