package jhn.validation;

import java.util.regex.Pattern;

public class Paths {
	public static final String HIT_EXT = ".hit.csv";
	
	public static String outputDir() {
		return jhn.Paths.outputDir("EDAValidation");
	}
	
	public static String outputDir(String dataset) {
		return outputDir() + "/" + dataset;
	}
	
	public static String mergedLauTopicLabelsFilename(String dataset) {
		return outputDir(dataset) + "/merged_lau_topic_labels.csv";
	}
	
	public static String mergedLauDocLabelsFilename(String dataset) {
		return outputDir(dataset) + "/merged_lau_doc_labels.csv";
	}
	
	public static final Pattern NAME_RGX = Pattern.compile("lda(\\d+)topics_(\\d+)");
	public static String name(int numTopics, int run) {
		return "lda"+numTopics+"topics_"+run;
	}
	
	public static String lauStateDir(String dataset) {
		return outputDir(dataset) + "/lau_state";
	}
	
	public static String topicCountCalibrationStateFilename(String dataset, int numTopics, int run) {
		return lauStateDir(dataset) + "/" + name(numTopics, run) + jhn.Paths.STATE_GZ_EXT;
	}
	
	public static String lauLogFilename(String dataset) {
		return outputDir(dataset) + "/main.log";
	}
	
	public static String lauKeysDir(String dataset) {
		return outputDir(dataset) + "/lau_keys";
	}
	
	public static String lauKeysFilename(String dataset, int numTopics, int run) {
		return lauKeysDir(dataset) + "/" + name(numTopics, run) + jhn.Paths.KEYS_EXT;
	}
	
	public static String lauTopicLabelsDir(String dataset) {
		return outputDir(dataset) + "/lau_topic_labels";
	}
	
	public static String lauTopicLabelsFilename(String dataset, int numTopics, int run) {
		return lauTopicLabelsDir(dataset) + "/" + name(numTopics, run) + jhn.Paths.TOPIC_LABELS_EXT;
	}
	
	public static String lauDocLabelsDir(String dataset) {
		return outputDir(dataset) + "/lau_doc_labels";
	}
	
	public static String lauDocLabelsFilename(String dataset, int numTopics, int run) {
		return lauDocLabelsDir(dataset) + "/" + name(numTopics, run) + jhn.Paths.DOC_LABELS_EXT;
	}
	
	public static String edaRunsDir(Class<? extends EDA> algo, String dataset) {
		return outputDir(algo, dataset) + "/runs";
	}
	
	public static String edaDocLabelsDir(Class<? extends EDA> algo, String dataset) {
		return outputDir(algo, dataset) + "/doc_labels";
	}
	
	public static String edaDocLabelsFilename(Class<? extends EDA> algo, String dataset, int firstIter, int lastIter, int run) {
		return edaDocLabelsDir(algo, dataset) + "/run" + run + "_iters" + firstIter + "-" + lastIter + jhn.Paths.DOC_LABELS_EXT;
	}
	
	public static String lauDocTopicsDir(String dataset) {
		return outputDir(dataset) + "/lau_doctopics";
	}
	
	public static String lauDocTopicsFilename(String dataset, int numTopics, int run) {
		return lauDocTopicsDir(dataset) + "/" + name(numTopics, run) + jhn.Paths.DOCTOPICS_EXT;
	}
	
	public static String hitDataDir(String dataset) {
		return outputDir(dataset) + "/hit_data";
	}
	
	public static String mergedDocLabelsFilename(String dataset, int numComparisons, int chooseFromTopN) {
		return hitDataDir(dataset) + "/merged_document_labels"
			+ "_" + dataset
			+ "_cmps" + numComparisons
			+ "_n" + chooseFromTopN
			+ "_6.hit.csv";
	}
}
