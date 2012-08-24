package jhn.validation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jhn.util.RandUtil;

/**
 * Format:
 *     #comment
 *     docNum,filename,label1,label2,...,labelN
 *
 */
public class StandardDocLabelSource implements DocLabelSource, BareLabelSource {
	private Map<String,String[]> labels = new HashMap<>();
		
		try(BufferedReader r = new BufferedReader(new FileReader(srcFilename))) {
			String line;
			while( (line=r.readLine()) != null) {
				if(!line.startsWith("#")) {
					String[] parts = line.split(",");
					String filename = parts[0];
					String[] labelsArr = new String[parts.length - 2];
					for(int i = 2; i < parts.length; i++) {
						labelsArr[i-2] = parts[i];
					}
					labels.put(filename, labelsArr);
				}
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
