package jhn.label.doc;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.util.FileExtensionFilter;
import jhn.util.RandUtil;

public class RandomRunsDocLabelSource implements DocLabelSource {
	private static final Pattern filenameRgx = Pattern.compile("([^_]+_)?(\\d+)");
	
	private Int2ObjectMap<DocLabelSource> allLabels = new Int2ObjectOpenHashMap<>();
	public RandomRunsDocLabelSource(String docLabelsDir) throws Exception {
		this(docLabelsDir, new FileExtensionFilter(jhn.Paths.DOC_LABELS_EXT));
	}
	
	public RandomRunsDocLabelSource(String docLabelsDir, FileFilter fileFilter) throws Exception {
		System.out.println(docLabelsDir);
		for(File f : new File(docLabelsDir).listFiles()) {
			String baseName = f.getName().split("[.]")[0];
			Matcher m = filenameRgx.matcher(baseName);
			m.matches();
			int run = Integer.parseInt(m.group(2));
			allLabels.put(run, new StandardDocLabelSource(f.getPath()));
		}
	}
	
	protected RandomRunsDocLabelSource() {
		// Override if desired
	}

	@Override
	public String[] labels(String docFilename, int numLabels) {
		int run = RandUtil.randItem(allLabels.keySet().toIntArray());
		String[] labels = allLabels.get(run).labels(docFilename, numLabels);
		
		String[] selectedLabels = new String[numLabels];
		for(int i = 0; i < selectedLabels.length; i++) {
			selectedLabels[i] = labels[i];
		}
		return selectedLabels;
	}
}
