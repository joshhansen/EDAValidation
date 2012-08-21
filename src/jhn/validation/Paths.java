package jhn.validation;

import java.util.regex.Pattern;

public class Paths {
	public static String outputDir() {
		return jhn.Paths.outputDir("EDAValidation");
	}
	
	public static String topicCountCalibrationDir() {
		return outputDir() + "/topic_count_calibration";
	}
	
	public static String topicCountCalibrationDir(String dataset) {
		return topicCountCalibrationDir() + "/" + dataset;
	}
	
	public static String topicCountCalibrationMergedLauTopicLabelsFilename(String dataset) {
		return topicCountCalibrationDir(dataset) + "/merged_lau_topic_labels.csv";
	}
	
	public static String topicCountCalibrationMergedLauDocLabelsFilename(String dataset) {
		return topicCountCalibrationDir(dataset) + "/merged_lau_doc_labels.csv";
	}
	
	public static String topicCountCalibrationMergedEdaTopicLabelsFilename(String dataset) {
		return topicCountCalibrationDir(dataset) + "/merged_eda_topic_labels.csv";
	}
	
	public static String topicCountCalibrationMergedEdaDocLabelsFilename(String dataset) {
		return topicCountCalibrationDir(dataset) + "/merged_eda_doc_labels.csv";
	}
	
//	public static String topicCountCalibrationFilename(int numTopics) {
//		return topicCountCalibrationDir() + "/aggregate_lda" + numTopics + "topics"
//	}
	
	public static final Pattern NAME_RGX = Pattern.compile("lda(\\d+)topics_(\\d+)");
	public static String name(int numTopics, int run) {
		return "lda"+numTopics+"topics_"+run;
	}
	
	public static String topicCountCalibrationStateDir(String dataset) {
		return topicCountCalibrationDir(dataset) + "/state";
	}
	
	public static final String STATE_EXT = ".state";
	public static String topicCountCalibrationStateFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationStateDir(dataset) + "/" + name(numTopics, run) + STATE_EXT;
	}
	
	public static String topicCountCalibrationLogFilename(String dataset) {
		return topicCountCalibrationDir(dataset) + "/main.log";
	}
	
	public static String topicCountCalibrationKeysDir(String dataset) {
		return topicCountCalibrationDir(dataset) + "/keys";
	}
	
	public static String KEYS_EXT = ".keys";
	public static String topicCountCalibrationKeysFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationKeysDir(dataset) + "/" + name(numTopics, run) + KEYS_EXT;
	}
	
	public static final String TOPIC_LABELS_EXT = ".topic_labels";
	public static String topicCountCalibrationLauTopicLabelsDir(String dataset) {
		return topicCountCalibrationDir(dataset) + "/lau_topic_labels";
	}
	
	public static String topicCountCalibrationLauTopicLabelsFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationLauTopicLabelsDir(dataset) + "/" + name(numTopics, run) + TOPIC_LABELS_EXT;
	}
	
	public static final String DOC_LABELS_EXT = ".doc_labels";
	public static String topicCountCalibrationLauDocLabelsDir(String dataset) {
		return topicCountCalibrationDir(dataset) + "/lau_doc_labels";
	}
	
	public static String topicCountCalibrationLauDocLabelsFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationLauDocLabelsDir(dataset) + "/" + name(numTopics, run) + DOC_LABELS_EXT;
	}
	
	public static String topicCountCalibrationEdaDocLabelsDir(String dataset) {
		return topicCountCalibrationDir(dataset) + "/eda_doc_labels";
	}
	
	public static String topicCountCalibrationEdaDocLabelsFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationEdaDocLabelsDir(dataset) + "/" + name(numTopics, run) + DOC_LABELS_EXT;
	}
	
	public static String topicCountCalibrationDocTopicsDir(String dataset) {
		return topicCountCalibrationDir(dataset) + "/doctopics";
	}
	
	public static final String DOCTOPICS_EXT = ".doctopics";
	public static String topicCountCalibrationDocTopicsFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationDocTopicsDir(dataset) + "/" + name(numTopics, run) + DOCTOPICS_EXT;
	}
}
