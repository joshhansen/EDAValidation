package jhn.eda.validate.doclabel;

import java.util.ArrayList;
import java.util.List;

import cc.mallet.types.LabelAlphabet;

import jhn.eda.validate.TraceGenerator;

class DocLabelTraceGenerator implements TraceGenerator {
		private LabelAlphabet labels;
		private int numLabels;
		
		public DocLabelTraceGenerator(LabelAlphabet labels, int numLabels) {
			this.labels = labels;
			this.numLabels = numLabels;
		}

		@Override
		public String[] generateTrace() {
			List<String> traceParts = new ArrayList<>();
			
			for(int i = 0; i < numLabels; i++) {
				int labelNum = MergeHitData.rand.nextInt(labels.size());
				String label = labels.lookupObject(labelNum).toString();
//				trace.append(",\"").append(label).append("\"");
				traceParts.add(label);
			}
			
			return traceParts.toArray(new String[0]);
		}
	}