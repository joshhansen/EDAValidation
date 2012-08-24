package jhn.validation;

public interface DocLabelSource extends LabelSource<String> {
	/**
	 * Selects a random run and returns the top numLabels labels for the document at docFilename.
	 */
	@Override
	String[] labels(String docFilename, int numLabels);
}
