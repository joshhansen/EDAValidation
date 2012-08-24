package jhn.calibration.topiccount;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jhn.calibration.topiccount.CalibrationMerge.LabelSourceFactory;
import jhn.validation.BareLabelSource;
import jhn.validation.Paths;
import jhn.validation.StandardTopicLabelSource;

public final class TopicLabelMerge {
	private TopicLabelMerge() {
		//Don't allow public instantiation
	}
	public static void main(String[] args) throws Exception {
		LabelSourceFactory lsf = new LabelSourceFactory(){
			@Override
			public BareLabelSource create(File file) throws FileNotFoundException, IOException {
				return new StandardTopicLabelSource(file.getPath());
			}
		};
		
		final int comparisonsPerPair = 10;
		String datasetName = "reuters21578";
		CalibrationMerge tccm = new CalibrationMerge(
				Paths.topicCountCalibrationLauDocLabelsDir(datasetName),
				Paths.topicCountCalibrationMergedLauDocLabelsFilename(datasetName),
				comparisonsPerPair,
				lsf);
		tccm.run();
	}
}
