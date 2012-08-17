package jhn.validation;

public class Paths {
	public static String outputDir() {
		return jhn.Paths.outputDir("EDAValidation");
	}
	
	
//	/Projects/Output/LDA/results/reuters21578/hitcalibration
	public static String topicCountCalibrationDir(String dataset) {
		return outputDir() + "/topic count calibration/" + dataset;
	}
	
//	public static String topicCountCalibrationFilename(int numTopics) {
//		return topicCountCalibrationDir() + "/aggregate_lda" + numTopics + "topics"
//	}
	
	public static String topicCountCalibrationFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationDir(dataset) + "/lda" + numTopics + "topics_" + run + ".state";
	}
	
	public static String topicCountCalibrationLogFilename(String dataset) {
		return topicCountCalibrationDir(dataset) + "/main.log";
	}
	
	public static String topicCountCalibrationKeysFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationDir(dataset) + "/lda" + numTopics + "topics_" + run + ".keys";
	}
	
	public static String topicCountCalibrationLauLabelsFilename(String dataset, int numTopics, int run) {
		return topicCountCalibrationDir(dataset) + "/lda" + numTopics + "topics_" + run + ".lau_labels";
	}
}
