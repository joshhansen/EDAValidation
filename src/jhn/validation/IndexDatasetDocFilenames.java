package jhn.validation;

import java.io.File;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import jhn.idx.Index;
import jhn.idx.RAMIndex;
import jhn.util.Util;

public class IndexDatasetDocFilenames {
	public static void index(String datasetName) {
		Index<String> filenamesIdx = new RAMIndex<>();
		InstanceList data = InstanceList.load(new File(jhn.Paths.malletDatasetFilename(datasetName)));
		
		for(Instance inst : data) {
			System.out.println(inst.getSource());
			filenamesIdx.indexOf(inst.getSource().toString());
		}
		
		Util.serialize(filenamesIdx, jhn.Paths.malletDatasetFilenameIndexFilename(datasetName));
	}
	
	public static void main(String[] args) {
		index("reuters21578_noblah");
	}
}
