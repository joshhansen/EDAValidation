package jhn.eda.hit;

public class Paths {
//	/Projects/Output/LDA/results/reuters21578/hitcalibration
	public static String topicCountCalibrationDir() {
		return jhn.Paths.outputDir("LDA") + "/results/reuters21578/hitcalibration";
	}
	
//	public static String topicCountCalibrationFilename(int numTopics) {
//		return topicCountCalibrationDir() + "/aggregate_lda" + numTopics + "topics"
//	}
	
	public static String topicCountCalibrationFilename(int numTopics, int run) {
		return topicCountCalibrationDir() + "/lda" + numTopics + "topics_" + run + ".state";
	}
	
	public static String topicCountCalibrationLogFilename() {
		return topicCountCalibrationDir() + "/main.log";
	}
	
	public static String topicCountCalibrationKeysFilename(int numTopics, int run) {
		return topicCountCalibrationDir() + "/lda" + numTopics + "topics_" + run + ".keys";
	}
	
	public static String topicCountCalibrationLauLabelsFilename(int numTopics, int run) {
		return topicCountCalibrationDir() + "/lda" + numTopics + "topics_" + run + ".lau_labels";
	}
}
