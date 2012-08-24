package jhn.calibration.topiccount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import jhn.calibration.topiccount.CalibrationMerge.KeySource;
import jhn.calibration.topiccount.CalibrationMerge.LabelSourceFactory;
import jhn.util.RandUtil;
import jhn.validation.LabelSource;
import jhn.validation.Paths;
import jhn.validation.StandardDocLabelSource;

public final class DocLabelMerge {
	private DocLabelMerge() {
		//Don't allow public instantiation
	}
	
	public static void main(String[] args) throws Exception {
		final int comparisonsPerPair = 10;
//		String datasetName = "reuters21578";
		String datasetName = "state_of_the_union";
		
		LabelSourceFactory<String> lsf = new LabelSourceFactory<String>(){
			@Override
			public LabelSource<String> create(File file) throws IOException {
				return new StandardDocLabelSource(file.getPath());
			}
		};
		
		final List<String> docFilenames = new ArrayList<>();
		InstanceList data = InstanceList.load(new File(jhn.Paths.malletDatasetFilename(datasetName)));
		for(Instance inst : data) {
			docFilenames.add(inst.getSource().toString());
		}
		KeySource<String> keys = new KeySource<String>(){
			@Override
			public String randomKey() {
				return RandUtil.randItem(docFilenames);
			}
		};
		

		CalibrationMerge<String> tccm = new CalibrationMerge<String>(
				Paths.topicCountCalibrationLauDocLabelsDir(datasetName),
				Paths.topicCountCalibrationMergedLauDocLabelsFilename(datasetName),
				comparisonsPerPair,
				lsf,
				keys);
		tccm.run();
	}
}
