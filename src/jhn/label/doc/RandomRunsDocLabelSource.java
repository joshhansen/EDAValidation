package jhn.label.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.util.FileExtensionFilter;
import jhn.util.RandUtil;

public class RandomRunsDocLabelSource implements DocLabelSource {
	private Int2ObjectMap<Map<String,String[]>> allLabels = new Int2ObjectOpenHashMap<>();
	
	public RandomRunsDocLabelSource(String docLabelsDir) throws Exception {
		for(File f : new File(docLabelsDir).listFiles(new FileExtensionFilter(".doclabels"))) {
			int run = Integer.parseInt(f.getName().split("[.]")[0]);
			allLabels.put(run, loadDocLabels(f.getPath()));
		}
	}

	@Override
	public String[] labels(String docFilename, int numLabels) {
		int run = RandUtil.randItem(allLabels.keySet().toIntArray());
		String[] labels = allLabels.get(run).get(docFilename);
		
		String[] selectedLabels = new String[numLabels];
		for(int i = 0; i < selectedLabels.length; i++) {
			selectedLabels[i] = labels[i];
		}
		return selectedLabels;
	}
	
	private static Map<String,String[]> loadDocLabels(String filename) throws Exception {
		Map<String,String[]> sources = new HashMap<>();
		
		try(BufferedReader r = new BufferedReader(new FileReader(filename))) {
			String line = null;
			while( (line=r.readLine()) != null) {
				if(!line.startsWith("#")) {
					String[] parts = line.split("[\",]+");
					
					// Skip the leading and trailing quotation marks:
					String[] labels = new String[parts.length - 2];
					for(int i = 0; i < labels.length; i++) {
						labels[i] = parts[i+2];
					}
					
					sources.put(parts[0], labels);
				}
			}
		}
		
		return sources;
	}
}
