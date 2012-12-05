package jhn.label;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.label.LabelSource;
import jhn.util.RandUtil;

public class RandomRunsLabelSource<K> implements LabelSource<K> {
	private Int2ObjectMap<LabelSource<K>> allLabels = new Int2ObjectOpenHashMap<>();
	
	public RandomRunsLabelSource(Class<? extends LabelSource<K>> labelSrcCls, String docLabelsDir, Pattern filenameRgx) throws Exception {
		Constructor<? extends LabelSource<K>> ctor = labelSrcCls.getConstructor(String.class);
		System.out.println(docLabelsDir);
		for(File f : new File(docLabelsDir).listFiles()) {
			Matcher m = filenameRgx.matcher(f.getName());
			if(m.matches()) {
				int run = Integer.parseInt(m.group(1));
				allLabels.put(run, ctor.newInstance(f.getPath()));
			}
		}
		if(allLabels.size() < 1) {
			throw new IllegalArgumentException("No files in directory " + docLabelsDir + " matching regex " + filenameRgx.pattern());
		}
	}
	
	protected RandomRunsLabelSource() {
		// Override if desired
	}

	@Override
	public String[] labels(K docFilename, int numLabels) {
		int run = RandUtil.randItem(allLabels.keySet().toIntArray());
		System.out.println("Run: " + run);
		String[] labels = allLabels.get(run).labels(docFilename, numLabels);
		
		String[] selectedLabels = new String[numLabels];
		for(int i = 0; i < selectedLabels.length; i++) {
			selectedLabels[i] = labels[i];
		}
		return selectedLabels;
	}
}
