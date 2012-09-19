package jhn.validation;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jhn.counts.d.DoubleCounter;
import jhn.counts.d.o.ObjDoubleCounter;
import jhn.label.LabelSource;

/**
 * 
 * @author Josh Hansen
 *
 * @param <K> The type of key the labels are indexed by
 */
public abstract class Merger<K> {
	private final int comparisons;
	protected final String destFilename;
	private DoubleCounter<LabelSource<K>> modelProportions = new ObjDoubleCounter<>();
	
	public Merger(int comparisons, String destFilename) {
		this.comparisons = comparisons;
		this.destFilename = destFilename;
	}
	
	public void run() throws Exception {
		List<String> output = randomize(merge());
		
		try(PrintWriter w = new PrintWriter(new FileWriter(destFilename))) {
			write(output, w);
		}
	}
	
	protected void setModelProportion(LabelSource<K> model, double proportion) {
		modelProportions.set(model, proportion);
	}

	protected static List<String> randomize(List<String> output) {
		Collections.shuffle(output);
		Collections.shuffle(output);
		Collections.shuffle(output);
		Collections.shuffle(output);
		Collections.shuffle(output);
		return output;
	}

	protected void write(List<String> output, PrintWriter w) {
		w.append(headerLine());
		w.append('\n');
		
		for(String outputLine : output) {
			w.append(outputLine).append('\n');
		}
	}

	protected List<String> merge() throws Exception {
		List<String> output = new ArrayList<>();
		
		for(int cmp = 0; cmp < comparisons; cmp++) {
			LabelSource<K> model1;
			LabelSource<K> model2;
			do {
				model1 = randModel();
				model2 = randModel();
			} while(model1 == model2);
			
			output.add(mergeLine(model1, model2));
		}
		
		return output;
	}
	
	private LabelSource<K> randModel() {
		return modelProportions.sample();
	}

	protected abstract String mergeLine(LabelSource<K> src1, LabelSource<K> src2) throws Exception;

	protected abstract String headerLine();
}
