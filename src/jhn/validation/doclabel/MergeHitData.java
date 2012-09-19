package jhn.validation.doclabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import jhn.counts.d.DoubleCounter;
import jhn.counts.d.o.ObjDoubleCounter;
import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.label.LabelSource;
import jhn.idx.Index;
import jhn.label.doc.DocLabelSource;
import jhn.label.doc.RandomDocLabelsSource;
import jhn.label.doc.RandomRunsDocLabelSource;
import jhn.util.RandUtil;
import jhn.util.Util;



public class MergeHitData {
	private Reference2ObjectMap<DocLabelSource,String> modelNames = new Reference2ObjectOpenHashMap<>();
	private DoubleCounter<DocLabelSource> modelProportions = new ObjDoubleCounter<>();

	private static String loadDocument(String filename) throws Exception {
		StringBuilder doc = new StringBuilder();
		
		try(BufferedReader r = new BufferedReader(new FileReader(filename.replace("file:", "")))) {
			String tmp = null;
			while( (tmp=r.readLine()) != null) {
				tmp = tmp.replace("&","&amp;")
						 .replace("\"", "&quot;")
						 .replace("'", "&apos;")
						 .replace("<", "&lt;")
						 .replace(">", "&gt;");
				
				doc.append(tmp);
				doc.append("<br/>");
			}
		}
		
		return doc.toString();
	}
	
	private static String wpLinkify(String label) {
		return "http://en.wikipedia.org/wiki/" + label.replace(" ", "_");
	}
	
	private static int numTokens(String text) {
		return text.split("\\s+").length;
	}
	
	private static final Pattern nonAlphaRgx = Pattern.compile("[^A-Za-z]+");
	private static double alphaPct(String text) {
		return (double) nonAlphaRgx.split(text).length / (double) numTokens(text);
	}
	
	private static final int MIN_TOKENS = 90;
	private static final double MIN_ALPHA_PCT = 0.9;
	private static boolean docOK(String text) {
		if(text.isEmpty()) return false;
		if(text.contains("Blah blah blah")) return false;
		if(alphaPct(text) < MIN_ALPHA_PCT) return false;
		if(numTokens(text) < MIN_TOKENS) return false;
		return true;
	}
	
	public void merge(Index<String> docFilenames, String outputFilename, int numDocs, int labelsPerDoc,
			int comparisonsPerDoc, int chooseFromTopN) throws Exception {
		
		try(PrintStream w = new PrintStream(new FileOutputStream(outputFilename))) {
			w.println("model1,model2,docnum,doctext,model1label,model2label,model1wplink,model2wplink");
			
			for(int docIterNum = 0; docIterNum < numDocs; docIterNum++) {
				int docNum;
				String filename;
				String text;
				
				do {
					docNum = RandUtil.rand.nextInt(docFilenames.size());
					filename = docFilenames.objectAt(docNum);
					text = loadDocument(filename);
				} while(!docOK(text));
				
				for(int comparisonNum = 0; comparisonNum < comparisonsPerDoc; comparisonNum++) {
					DocLabelSource[] models = initModels();
					String[] labels = initLabels(models, filename, labelsPerDoc, chooseFromTopN);
					cleanLabels(labels);
					
					w.append(modelNames.get(models[0])).append(',').append(modelNames.get(models[1]));
					w.print(',');
					w.print(docNum);
					w.append(",\"").append(text).append("\"");
					w.append(",\"").append(labels[0]).append("\"");
					w.append(",\"").append(labels[1]).append("\"");
					
					//Wikipedia links:
					w.append(",\"").append(wpLinkify(labels[0])).append("\"");
					w.append(",\"").append(wpLinkify(labels[1])).append("\"");
					w.println();
					
				}//end for comparisons
			}//end for docs
		}// end try
	}// end void merge

	private static void cleanLabels(String[] labels) {
		for(int k = 0; k < labels.length; k++) {
			labels[k] = StringUtils.capitalize(labels[k].trim());
		}
	}

	private static String[] initLabels(DocLabelSource[] models, String docFilename, int labelsPerDoc, int chooseFromTopN) throws Exception {
		String[] labels = new String[2];
		for(int position = 0; position < labels.length; position++) {
			labels[position] = RandUtil.randItem(models[position].labels(docFilename, labelsPerDoc), chooseFromTopN);
		}
		return labels;
	}

	private DocLabelSource randModel() {
		return modelProportions.sample();
	}
	
	private DocLabelSource[] initModels() {
		DocLabelSource[] models = new DocLabelSource[2];
		models[0] = randModel();
		do {
			models[1] = randModel();
		} while(models[1] != models[0]);
		return models;
	}
	
	public static void main(String[] args) throws Exception {
		final String datasetName = "toy_dataset4";
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir("wp_lucene4");
		final int numDocs = 100;
		final int labelsPerDoc = 10;
		final int cmpsPerDoc = 1;
		final int chooseFromTopN = 1;
		
		final String edaLabelsDir = jhn.validation.Paths.edaDocLabelsDir(datasetName);
		final String lauLabelsDir = jhn.validation.Paths.lauDocLabelsDir(datasetName);
		DocLabelSource eda = new RandomRunsDocLabelSource(edaLabelsDir, new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return name.startsWith("last10_") && name.endsWith(jhn.Paths.DOC_LABELS_EXT);
			}
		});
		DocLabelSource lauEtAl = new RandomRunsDocLabelSource(lauLabelsDir, new FileFilter(){
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName();
				return name.startsWith("lda10topics") && name.endsWith(jhn.Paths.DOC_LABELS_EXT);
			}
		});
		
		try(IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
			DocLabelSource rand = new RandomDocLabelsSource(labels);
			
			MergeHitData mhd = new MergeHitData();
			mhd.modelProportions.set(eda, 0.45);
			mhd.modelProportions.set(lauEtAl, 0.45);
			mhd.modelProportions.set(rand, 0.1);
			
			String outputFilename = jhn.validation.Paths.mergedDocLabelsFilename(datasetName, numComparisons, chooseFromTopN);
			
			mhd.modelNames.put(eda, "EDA");
			mhd.modelNames.put(lauEtAl, "LAU_ET_AL");
			mhd.modelNames.put(rand, "RANDOM");
			
			
			@SuppressWarnings("unchecked")
			Index<String> docFilenames = (Index<String>) Util.deserialize(jhn.Paths.malletDatasetFilenameIndexFilename(datasetName));
			mhd.merge(docFilenames, outputFilename, numDocs, labelsPerDoc, cmpsPerDoc, chooseFromTopN);
		} //end try
	}
}
