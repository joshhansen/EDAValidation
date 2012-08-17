package jhn.eda.validate.doclabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.eda.Paths;
import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.eda.validate.Models;
import jhn.eda.validate.TraceGenerator;
import jhn.util.RandUtil;



public class MergeHitData {
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
	
	private static Int2ObjectMap<String> docSources(String filename) throws Exception {
		System.out.println(filename);
		Int2ObjectMap<String> sources = new Int2ObjectOpenHashMap<>();
		
		try(BufferedReader r = new BufferedReader(new FileReader(filename))) {
			String line = null;
			while( (line=r.readLine()) != null) {
				if(!line.startsWith("#")) {
					String[] parts = line.split(",");
					sources.put(Integer.parseInt(parts[0]), parts[1]);
				}
			}
		}
		
		return sources;
	}
	
	private static Int2ObjectMap<String[]> docLabels(String filename) throws Exception {
		Int2ObjectMap<String[]> sources = new Int2ObjectOpenHashMap<>();
		
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
					
					sources.put(Integer.parseInt(parts[0]), labels);
				}
			}
		}
		
		return sources;
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
	
	private static void merge(String edaDocLabelFilename, String lauFilename, String outputFilename, int numDocs,
			int comparisonsPerDoc, int chooseFromTopN, double traceProportion, TraceGenerator traceGen) throws Exception {
		
		Int2ObjectMap<String> sources = docSources(edaDocLabelFilename);
		Int2ObjectMap<String[]> edaLabels = docLabels(edaDocLabelFilename);
		Int2ObjectMap<String[]> lauLabels = docLabels(lauFilename);
		
		try(PrintStream w = new PrintStream(new FileOutputStream(outputFilename))) {
			w.println("model1,model2,docnum,doctext,model1label,model2label,model1wplink,model2wplink");
			
			for(int docIterNum = 0; docIterNum < numDocs; docIterNum++) {
				int docNum;
				String text;
				do {
					docNum = RandUtil.rand.nextInt(sources.size());
					text = loadDocument(sources.get(docNum));
				} while(!docOK(text));
				
				String[] edaLabelArr = edaLabels.get(docNum);
				String[] lauLabelArr = lauLabels.get(docNum);
				
				for(int comparisonNum = 0; comparisonNum < comparisonsPerDoc; comparisonNum++) {
					Models[] models = initModels(traceProportion);
					String[] labels = initLabels(chooseFromTopN, traceGen, edaLabelArr,	lauLabelArr, models);
					maybeReverseOrder(models, labels);
					cleanLabels(labels);
					
					
					w.append(models[0].toString()).append(',').append(models[1].toString());
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
			
		}
	}

	private static void cleanLabels(String[] labels) {
		// Clean up
		for(int k = 0; k < labels.length; k++) {
			labels[k] = StringUtils.capitalize(labels[k].trim());
		}
	}

	private static void maybeReverseOrder(Models[] models, String[] labels) {
		final boolean reverseOrder = RandUtil.rand.nextBoolean();
		if(reverseOrder) {
			Models tmp = models[0];
			models[0] = models[1];
			models[1] = tmp;
			
			String tmpLabels = labels[0];
			labels[0] = labels[1];
			labels[1] = tmpLabels;
		}
	}

	private static String[] initLabels(int chooseFromTopN, TraceGenerator traceGen,
			String[] edaLabelArr, String[] lauLabelArr, Models[] models) throws Exception {
		String[] labels = new String[2];
		for(int position = 0; position < labels.length; position++) {
			switch(models[position]) {
				case TRACER:
					labels[position] = traceGen.generateTrace()[0];
					break;
				case EDA:
					labels[position] = RandUtil.randItem(edaLabelArr, chooseFromTopN);
					break;
				case LAU_ET_AL:
					labels[position] = RandUtil.randItem(lauLabelArr, chooseFromTopN);
					break;
				default:
					throw new Exception();
			}
		}
		return labels;
	}

	private static Models[] initModels(double traceProportion) {
		Models[] models = new Models[2];
		if(RandUtil.rand.nextDouble() <= traceProportion) {
			models[0] = Models.TRACER;
			if(RandUtil.rand.nextBoolean()) {
				models[1] = Models.EDA;
			} else {
				models[1] = Models.LAU_ET_AL;
			}
		} else {
			models[0] = Models.EDA;
			models[1] = Models.LAU_ET_AL;
		}
		return models;
	}
	
	public static void main(String[] args) throws Exception {
		final int edaRun = 17;
		final int edaIt = 95;
		final int lauRun = 0;
		final int numDocs = 100;
		final int cmpsPerDoc = 1;
		final int chooseFromTopN = 1;
		final double traceProp = 0.1;
		final String datasetName = "toy_dataset4";
		final String edaRunDir = Paths.runDir(Paths.defaultRunsDir(), edaRun);
		final String lauRunDir = jhn.eda.validate.Paths.outputDir()+"/topic count calibration/" + datasetName;
		
		String topicWordIdxDir = jhn.eda.Paths.topicWordIndexDir("wp_lucene4");
		try(IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
			TraceGenerator traceGen = new DocLabelTraceGenerator(labels, 10);
			
			final String outputFilename = jhn.Paths.outputDir("EDAValidation")
					+ "/merged_document_labels"
					+ "_" + datasetName
					+ "_docs" + numDocs
					+ "_cmpsPerDoc" + cmpsPerDoc
					+ "_tracePrp" + traceProp
					+ "_n" + chooseFromTopN
					+ "_eda"+edaRun+","+edaIt
					+ "_lau"+lauRun
					+ "_minTokens" + MIN_TOKENS
					+ "_minAlphaPct" + MIN_ALPHA_PCT
					+ "_5.hit.csv";
			
			merge(jhn.eda.Paths.documentLabelHitDataFilename(edaRunDir, edaIt),
					jhn.lauetal.Paths.documentLabelHitDataFilename(lauRunDir),
					outputFilename,
					numDocs,
					cmpsPerDoc,
					chooseFromTopN,
					traceProp,
					traceGen);
			
		}
	}
}
