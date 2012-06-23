package jhn.eda.hit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.NIOFSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.eda.lucene.LuceneLabelAlphabet;



public class MergeDocumentLabelHitData {
	private static final Random rand = new Random();
//
//	public interface TraceGenerator {
//		String generateTrace();
//	}
//	private double tracerProportion;
//	private TraceGenerator traceGenerator;
//	private String edaFilename;
//	private String lauEtAlFilename;
//	private String[] fields;
//	
//	private String outputFilename;
//	
//	public void merge() throws Exception {
//		BufferedReader r1 = new BufferedReader(new FileReader(edaFilename));
//		BufferedReader r2 = new BufferedReader(new FileReader(lauEtAlFilename));
//		r1.readLine();
//		r2.readLine();
//		
//		PrintStream w =  new PrintStream(new FileOutputStream(outputFilename));
//		w.print("model1,model2");
//		for(String field : fields) {
//			w.append(",model1").append(field);
//		}
//		for(String field : fields) {
//			w.append(",model2").append(field);
//		}
//		w.println();
//		
//		String edaLine = null;
//		String lauLine;
//		
//		Models firstModel;
//		Models secondModel;
//		String firstModelFields;
//		String secondModelFields;
//		
//		while( (edaLine=r1.readLine()) != null) {
//			lauLine = r2.readLine();
//			if(rand.nextBoolean()) {
//				firstModel = Models.EDA;
//				secondModel = Models.LAU_ET_AL;
//				firstModelFields = edaLine;
//				secondModelFields = lauLine;
//			} else {
//				firstModel = Models.LAU_ET_AL;
//				secondModel = Models.EDA;
//				firstModelFields = lauLine;
//				secondModelFields = edaLine;
//			}
//			
//			w.append(firstModel.toString()).append(',');
//			w.append(secondModel.toString()).append(',');
//			w.print(firstModelFields);
//			w.print(',');
//			w.println(secondModelFields);
//		}
//	}
//	
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
//	
	private static class DocLabelTraceGen implements TraceGenerator {
		private LabelAlphabet labels;
		private int numLabels;
		
		public DocLabelTraceGen(LabelAlphabet labels, int numLabels) {
			this.labels = labels;
			this.numLabels = numLabels;
		}

		public String[] generateTrace() {
			List<String> traceParts = new ArrayList<String>();
			
			for(int i = 0; i < numLabels; i++) {
				int labelNum = rand.nextInt(labels.size());
				String label = labels.lookupObject(labelNum).toString();
//				trace.append(",\"").append(label).append("\"");
				traceParts.add(label);
			}
			
			return traceParts.toArray(new String[0]);
		}
	};
	
	private static Int2ObjectMap<String> docSources(String filename) throws Exception {
		System.out.println(filename);
		Int2ObjectMap<String> sources = new Int2ObjectOpenHashMap<String>();
		
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
		Int2ObjectMap<String[]> sources = new Int2ObjectOpenHashMap<String[]>();
		
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
	
	private static void merge(String edaFilename, String lauFilename, String outputFilename, int numDocs,
			double tracerProportion, TraceGenerator traceGen) throws Exception {
		Int2ObjectMap<String> sources = docSources(edaFilename);
		Int2ObjectMap<String[]> edaLabels = docLabels(edaFilename);
		Int2ObjectMap<String[]> lauLabels = docLabels(lauFilename);
		
		PrintStream w = new PrintStream(new FileOutputStream(outputFilename));
		w.print("model1,model2,docnum,doctext");
		w.print(",model1label1,model1label2,model1label3,model1label4,model1label5,model1label6,model1label7,model1label8,model1label9,model1label10");
		w.println(",model2label1,model2label2,model2label3,model2label4,model2label5,model2label6,model2label7,model2label8,model2label9,model2label10");
		for(int i = 0; i < numDocs; i++) {
			int docNum = rand.nextInt(sources.size());
			Models[] models = new Models[2];
			String[][] labels = new String[2][];
			
			if(rand.nextDouble() <= tracerProportion) {
				// Use tracer for one side
				models[0] = Models.TRACER;
				labels[0] = traceGen.generateTrace();
				if(rand.nextBoolean()) {
					models[1] = Models.EDA;
					labels[1] = edaLabels.get(docNum);
				} else {
					models[1] = Models.LAU_ET_AL;
					labels[1] = lauLabels.get(docNum);
				}
			} else {
				// Just use regular models---no tracer
				models[0] = Models.EDA;
				models[1] = Models.LAU_ET_AL;
				labels[0] = edaLabels.get(docNum);
				labels[1] = lauLabels.get(docNum);
			}
			
			final boolean reverseOrder = rand.nextBoolean();
			if(reverseOrder) {
				Models tmp = models[0];
				models[0] = models[1];
				models[1] = tmp;
				
				String[] tmpLabels = labels[0];
				labels[0] = labels[1];
				labels[1] = tmpLabels;
			}
			
			
			String text = loadDocument(sources.get(docNum));
			w.append(models[0].toString()).append(',').append(models[1].toString());
			w.append(",\"").append(text).append("\"");
			for(String label : labels[0]) {
				w.append(",\"").append(label).append("\"");
			}
			for(String label : labels[1]) {
				w.append(",\"").append(label).append("\"");
			}
			w.println();
		}
		w.close();
	}
	
	public static void main(String[] args) throws Exception {
//		MergeDocumentLabelHitData merger = new MergeDocumentLabelHitData();
//		merger.tracerProportion = 0.1;
//		merger.fields = new String[] {"label1","label2","label3","label4","label5","label6","label7","label8","label9","label10"};
		
		
		String topicWordIdxDir = jhn.eda.Paths.topicWordIndexDir("wp_lucene4");
		IndexReader topicWordIdx = IndexReader.open(NIOFSDirectory.open(new File(topicWordIdxDir)));
		LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
		TraceGenerator traceGen = new DocLabelTraceGen(labels, 10);
		
		merge(jhn.eda.Paths.documentLabelHitDataFilename(17, 95),
				jhn.lauetal.Paths.documentLabelHitDataFilename(0),
				jhn.Paths.outputDir("EDAValidation") + "/reuters_merged_document_labels.hit.csv",
				100,
				0.1,
				traceGen);
		
		
//		merge(jhn.Paths.outputDir("EDA") + "/runs/17/hit_data_it95.hit.csv",
//			  jhn.Paths.outputDir("LauEtAl") + "/reuters-labels.hit.csv",
//		      jhn.Paths.outputDir("EDAValidation") + "/merged_reuters.hit.csv");
		
		topicWordIdx.close();
	}
}
