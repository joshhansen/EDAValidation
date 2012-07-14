package jhn.eda.hit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.util.RandUtil;



public class MergeDocumentLabelHitData {
	private static final Random rand = new Random();
	private static String loadDocument(String filename) throws Exception {
		StringBuilder doc = new StringBuilder();
		
		BufferedReader r = new BufferedReader(new FileReader(filename.replace("file:", "")));
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
		r.close();
		
		return doc.toString();
	}
	
	private static class DocLabelTraceGen implements TraceGenerator {
		private LabelAlphabet labels;
		private int numLabels;
		
		public DocLabelTraceGen(LabelAlphabet labels, int numLabels) {
			this.labels = labels;
			this.numLabels = numLabels;
		}

		@Override
		public String[] generateTrace() {
			List<String> traceParts = new ArrayList<>();
			
			for(int i = 0; i < numLabels; i++) {
				int labelNum = rand.nextInt(labels.size());
				String label = labels.lookupObject(labelNum).toString();
//				trace.append(",\"").append(label).append("\"");
				traceParts.add(label);
			}
			
			return traceParts.toArray(new String[0]);
		}
	}
	
	private static Int2ObjectMap<String> docSources(String filename) throws Exception {
		System.out.println(filename);
		Int2ObjectMap<String> sources = new Int2ObjectOpenHashMap<>();
		
		BufferedReader r = new BufferedReader(new FileReader(filename));
		
		String line = null;
		while( (line=r.readLine()) != null) {
			if(!line.startsWith("#")) {
				String[] parts = line.split(",");
				sources.put(Integer.parseInt(parts[0]), parts[1]);
			}
		}
		r.close();
		
		return sources;
	}
	
	private static Int2ObjectMap<String[]> docLabels(String filename) throws Exception {
		Int2ObjectMap<String[]> sources = new Int2ObjectOpenHashMap<>();
		
		BufferedReader r = new BufferedReader(new FileReader(filename));
		
		String line = null;
		while( (line=r.readLine()) != null) {
			if(!line.startsWith("#")) {
				String[] parts = line.split("[\",]+");
				String[] labels = new String[parts.length - 2];
				for(int i = 0; i < labels.length; i++) {
					labels[i] = parts[i+2];
				}
				sources.put(Integer.parseInt(parts[0]), labels);
			}
		}
		r.close();
		
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
	
	private static void merge(String edaFilename, String lauFilename, String outputFilename, int numDocs,
			int comparisonsPerDoc, int chooseFromTopN, double traceProportion, TraceGenerator traceGen) throws Exception {
		
		Int2ObjectMap<String> sources = docSources(edaFilename);
		Int2ObjectMap<String[]> edaLabels = docLabels(edaFilename);
		Int2ObjectMap<String[]> lauLabels = docLabels(lauFilename);
		
		PrintStream w = new PrintStream(new FileOutputStream(outputFilename));
		w.println("model1,model2,docnum,doctext,model1label,model2label,model1wplink,model2wplink");
		
		for(int i = 0; i < numDocs; i++) {
			int docNum;
			String text;
			do {
				docNum = rand.nextInt(sources.size());
				text = loadDocument(sources.get(docNum));
			} while(!docOK(text));//text.contains("Blah blah blah"));
			
//			final int docNum = rand.nextInt(sources.size());
//			String text = loadDocument(sources.get(docNum));
			
			String[] edaLabelArr = edaLabels.get(docNum);
			String[] lauLabelArr = lauLabels.get(docNum);
			
			for(int j = 0; j < comparisonsPerDoc; j++) {
				Models[] models = new Models[2];
				
				if(rand.nextDouble() <= traceProportion) {
					models[0] = Models.TRACER;
					if(rand.nextBoolean()) {
						models[1] = Models.EDA;
					} else {
						models[1] = Models.LAU_ET_AL;
					}
				} else {
					models[0] = Models.EDA;
					models[1] = Models.LAU_ET_AL;
				}
				
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
				
				final boolean reverseOrder = rand.nextBoolean();
				if(reverseOrder) {
					Models tmp = models[0];
					models[0] = models[1];
					models[1] = tmp;
					
					String tmpLabels = labels[0];
					labels[0] = labels[1];
					labels[1] = tmpLabels;
				}
				
				// Clean up
				for(int k = 0; k < labels.length; k++) {
					labels[k] = StringUtils.capitalize(labels[k].trim());
				}
				
				
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
		
		w.close();
	}
	
	
//	private static void merge(String edaFilename, String lauFilename, String outputFilename, int numDocs,
//			double tracerProportion, TraceGenerator traceGen) throws Exception {
//		Int2ObjectMap<String> sources = docSources(edaFilename);
//		Int2ObjectMap<String[]> edaLabels = docLabels(edaFilename);
//		Int2ObjectMap<String[]> lauLabels = docLabels(lauFilename);
//		
//		PrintStream w = new PrintStream(new FileOutputStream(outputFilename));
//		w.println("model1,model2,docnum,doctext,model1label,model2label");
//		
//		for(int i = 0; i < numDocs; i++) {
//			int docNum = rand.nextInt(sources.size());
//			Models[] models = new Models[2];
//			String[] labels = new String[2];
//			
//			if(rand.nextDouble() <= tracerProportion) {
//				// Use tracer for one side
//				models[0] = Models.TRACER;
//				labels[0] = traceGen.generateTrace()[0];
//				if(rand.nextBoolean()) {
//					models[1] = Models.EDA;
//					labels[1] = edaLabels.get(docNum);
//				} else {
//					models[1] = Models.LAU_ET_AL;
//					labels[1] = lauLabels.get(docNum);
//				}
//			} else {
//				// Just use regular models---no tracer
//				models[0] = Models.EDA;
//				models[1] = Models.LAU_ET_AL;
//				labels[0] = edaLabels.get(docNum);
//				labels[1] = lauLabels.get(docNum);
//			}
//			
//			final boolean reverseOrder = rand.nextBoolean();
//			if(reverseOrder) {
//				Models tmp = models[0];
//				models[0] = models[1];
//				models[1] = tmp;
//				
//				String[] tmpLabels = labels[0];
//				labels[0] = labels[1];
//				labels[1] = tmpLabels;
//			}
//			
//			
//			String text = loadDocument(sources.get(docNum));
//			w.append(models[0].toString()).append(',').append(models[1].toString());
//			w.append(",\"").append(text).append("\"");
//			for(String label : labels[0]) {
//				w.append(",\"").append(label).append("\"");
//			}
//			for(String label : labels[1]) {
//				w.append(",\"").append(label).append("\"");
//			}
//			w.println();
//		}
//		w.close();
//	}
	
	public static void main(String[] args) throws Exception {
//		MergeDocumentLabelHitData merger = new MergeDocumentLabelHitData();
//		merger.tracerProportion = 0.1;
//		merger.fields = new String[] {"label1","label2","label3","label4","label5","label6","label7","label8","label9","label10"};
		final int edaRun = 17;
		final int edaIt = 95;
		final int lauRun = 0;
		final int numDocs = 100;
		final int cmpsPerDoc = 1;
		final int chooseFromTopN = 1;
		final double traceProp = 0.1;
		
		String topicWordIdxDir = jhn.eda.Paths.topicWordIndexDir("wp_lucene4");
		IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)));
		LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
		TraceGenerator traceGen = new DocLabelTraceGen(labels, 10);
		
		final String outputFilename = jhn.Paths.outputDir("EDAValidation")
				+ "/reuters_merged_document_labels"
				+ "_docs" + numDocs
				+ "_cmpsPerDoc" + cmpsPerDoc
				+ "_tracePrp" + traceProp
				+ "_n" + chooseFromTopN
				+ "_eda"+edaRun+","+edaIt
				+ "_lau"+lauRun
				+ "_minTokens" + MIN_TOKENS
				+ "_minAlphaPct" + MIN_ALPHA_PCT
				+ "_4.hit.csv";
		
		merge(jhn.eda.Paths.documentLabelHitDataFilename(edaRun, edaIt),
				jhn.lauetal.Paths.documentLabelHitDataFilename(lauRun),
				outputFilename,
				numDocs,
				cmpsPerDoc,
				chooseFromTopN,
				traceProp,
				traceGen);
		
		
//		merge(jhn.Paths.outputDir("EDA") + "/runs/17/hit_data_it95.hit.csv",
//			  jhn.Paths.outputDir("LauEtAl") + "/reuters-labels.hit.csv",
//		      jhn.Paths.outputDir("EDAValidation") + "/merged_reuters.hit.csv");
		
		topicWordIdx.close();
	}
}
