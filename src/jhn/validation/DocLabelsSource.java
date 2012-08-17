package jhn.validation;

public interface DocLabelsSource {
	String name();
	
	/**
	 * Selects a random run and returns the top numLabels labels for the document at docFilename.
	 */
	String[] labels(String docFilename, int numLabels);
}
