package jhn.label.doc;

import java.util.ArrayList;
import java.util.List;

import cc.mallet.types.LabelAlphabet;

import jhn.label.BareLabelSource;
import jhn.util.RandUtil;

/**
 * A document label source that returns labels selected at random from a LabelAlphabet
 * @author Josh Hansen
 *
 */
public class RandomDocLabelsSource implements DocLabelSource, BareLabelSource {
	private LabelAlphabet labels;
	
	public RandomDocLabelsSource(LabelAlphabet labels) {
		this.labels = labels;
	}
	
	@Override
	public String[] labels(String docFilename, int numLabels) {
		return labels(numLabels);
	}
	
	@Override
	public String[] labels(int numLabels) {
		List<String> traceParts = new ArrayList<>();
		
		for(int i = 0; i < numLabels; i++) {
			int labelNum = RandUtil.rand.nextInt(labels.size());
			String label = labels.lookupObject(labelNum).toString();
			traceParts.add(label);
		}
		
		return traceParts.toArray(new String[0]);
	}
}
