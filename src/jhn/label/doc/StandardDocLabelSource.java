package jhn.label.doc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jhn.io.DocLabels;
import jhn.io.DocLabelsFileReader;
import jhn.label.BareLabelSource;
import jhn.util.RandUtil;

/**
 * Format:
 *     #comment
 *     docNum,filename,"label1","label2",...,"labelN"
 *
 */
public class StandardDocLabelSource implements DocLabelSource, BareLabelSource {
	private Map<String,String[]> labels = new HashMap<>();
	
	public StandardDocLabelSource(String srcFilename) throws IOException {
		try(DocLabelsFileReader r = new DocLabelsFileReader(srcFilename)) {
			for(DocLabels dl : r) {
				String fullFilename = dl.docSource().replaceFirst("file:", "");
				labels.put(fullFilename, dl.labels());
			}
		}
	}

	@Override
	public String[] labels(String docFilename, int numLabels) {
		String[] labelsArr = labels.get(docFilename);
		if(labelsArr.length < numLabels) {
			throw new IllegalArgumentException("Can't return that many labels");
		}
		return labelsArr;
	}

	@Override
	public String[] labels(int numLabels) {
		return labels(RandUtil.randItem(labels.keySet()), numLabels);
	}
}
