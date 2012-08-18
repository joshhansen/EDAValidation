package jhn.validation.trace;

import java.util.ArrayList;
import java.util.List;

import cc.mallet.types.LabelAlphabet;

import jhn.util.RandUtil;
import jhn.validation.AbstractNamed;
import jhn.validation.DocLabelsSource;

public class RandomDocLabelsSource extends AbstractNamed implements DocLabelsSource {
	private LabelAlphabet labels;
	
	public RandomDocLabelsSource(LabelAlphabet labels) {
		this.labels = labels;
	}

	@Override
	public String[] labels(String docFilename, int numLabels) {
		List<String> traceParts = new ArrayList<>();
		
		for(int i = 0; i < numLabels; i++) {
			int labelNum = RandUtil.rand.nextInt(labels.size());
			String label = labels.lookupObject(labelNum).toString();
//				trace.append(",\"").append(label).append("\"");
			traceParts.add(label);
		}
		
		return traceParts.toArray(new String[0]);
	}

	@Override
	public String name() {
		return "RANDOM";
	}
}
