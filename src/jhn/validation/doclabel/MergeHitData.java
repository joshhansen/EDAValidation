package jhn.validation.doclabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.label.LabelSource;
import jhn.label.doc.DocLabelSource;
import jhn.label.doc.RandomDocLabelsSource;
import jhn.label.doc.RandomRunsDocLabelSource;
import jhn.util.RandUtil;
import jhn.validation.Merger;



public class MergeHitData extends Merger<String> {
	private final String[] docFilenames;
	private final int chooseFromTopN;
	private Reference2ObjectMap<LabelSource<String>,String> modelNames = new Reference2ObjectOpenHashMap<>();
	
	public MergeHitData(String datasetName, int comparisons, String destFilename, int chooseFromTopN) {
		super(comparisons, destFilename);
		this.chooseFromTopN = chooseFromTopN;
		
		final List<String> filenames = new ArrayList<>();
		InstanceList data = InstanceList.load(new File(jhn.Paths.malletDatasetFilename(datasetName)));
		for(Instance inst : data) {
			filenames.add(inst.getSource().toString());
		}
		this.docFilenames = filenames.toArray(new String[0]);
	}

	private static String loadDocument(String filename) throws Exception {
		StringBuilder doc = new StringBuilder();
		
		try(BufferedReader r = new BufferedReader(new FileReader(filename))) {
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
	
	private static String cleanLabel(String label) {
		return StringUtils.capitalize(label.trim());
	}
	
	protected String randKey() {
		return RandUtil.randItem(docFilenames);
	}
	
	private static final int MIN_DOC_LENGTH = 100;
	private static final int MIN_TOKEN_COUNT = 80;
	private static boolean docTextOK(String docText) {
		if(docText.length() < MIN_DOC_LENGTH) return false;
		if(docText.split("\\s+").length < MIN_TOKEN_COUNT) return false;
		return true;
	}
	
	@Override
	protected String mergeLine(LabelSource<String> src1, LabelSource<String> src2) throws Exception {
		String docFilename;
		String docText;
		do {
			docFilename = randKey();
			docText = loadDocument(docFilename.replaceAll("file:/home/[^/]+/", System.getenv("HOME")+"/"));
		} while(!docTextOK(docText));
		
		System.out.println("Filename: " + docFilename);
		
		System.out.println(modelNames.get(src1));
		String[] labels1 = src1.labels(docFilename, chooseFromTopN);
		System.out.println(modelNames.get(src2));
		String[] labels2 = src2.labels(docFilename, chooseFromTopN);
		String label1 = cleanLabel(RandUtil.randItem(labels1));
		String label2 = cleanLabel(RandUtil.randItem(labels2));
		
		StringBuilder w = new StringBuilder();
		w.append(modelNames.get(src1)).append(',').append(modelNames.get(src2));
		w.append(',');
		w.append(docFilename);
		w.append(",\"").append(docText).append("\"");
		w.append(",\"").append(label1).append("\"");
		w.append(",\"").append(wpLinkify(label1)).append("\"");
		w.append(",\"").append(label2).append("\"");
		w.append(",\"").append(wpLinkify(label2)).append("\"");
		
		return w.toString();
	}

	@Override
	protected String headerLine() {
		StringBuilder header = new StringBuilder();
		
		final String[] sides = new String[]{"1","2"};
		for(String side : sides) {
			header.append("model").append(side).append(',');
		}
		
		header.append("docFilename,docText,");
		
		for(int i = 0; i < sides.length; i++) {
			String side = sides[i];
			header.append("label").append(side).append(',');
			header.append("wplink").append(side);
			if(i < sides.length - 1) {
				header.append(',');
			}
		}
		return header.toString();
	}
	
	public static void main(String[] args) throws Exception {
//		final String datasetName = "toy_dataset4";
		final String datasetName = "sotu_chunks";
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir("wp_lucene4");
		final int numComparisons = 200;
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
			
			File outputDir = new File(jhn.validation.Paths.hitDataDir(datasetName));
			if(!outputDir.exists()) {
				outputDir.mkdirs();
			}
			
			String outputFilename = jhn.validation.Paths.mergedDocLabelsFilename(datasetName, numComparisons, chooseFromTopN);
			
			MergeHitData mhd = new MergeHitData(datasetName, numComparisons, outputFilename, chooseFromTopN);
			mhd.setModelProportion(eda, 0.45);
			mhd.setModelProportion(lauEtAl, 0.45);
			mhd.setModelProportion(rand, 0.1);
			
			mhd.modelNames.put(eda, "EDA");
			mhd.modelNames.put(lauEtAl, "LAU_ET_AL");
			mhd.modelNames.put(rand, "RANDOM");
			
			mhd.run();
		}
	}
}
