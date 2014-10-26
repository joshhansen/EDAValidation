package jhn.validation.doclabel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;

import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.EDA;
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
		
		System.out.println(modelName(src1));
		String[] labels1 = src1.labels(docFilename.replaceFirst("file:", ""), chooseFromTopN);
		System.out.println(modelName(src2));
		String[] labels2 = src2.labels(docFilename.replaceFirst("file:", ""), chooseFromTopN);
		String label1 = cleanLabel(RandUtil.randItem(labels1));
		String label2 = cleanLabel(RandUtil.randItem(labels2));
		
		return new StringBuilder()
		.append(modelName(src1)).append(',').append(modelName(src2))
		.append(',')
		.append(docFilename)
		.append(",\"").append(docText).append("\"")
		.append(",\"").append(label1).append("\"")
		.append(",\"").append(wpLinkify(label1)).append("\"")
		.append(",\"").append(label2).append("\"")
		.append(",\"").append(wpLinkify(label2)).append("\"").toString();
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
	
	private static final Pattern edaFilenameRgx = Pattern.compile("run(\\d+)_iters\\d+-\\d+\\" + jhn.Paths.DOC_LABELS_EXT);
	private static final Pattern lauFilenameRgx = Pattern.compile("lda10topics_(\\d+)\\" + jhn.Paths.DOC_LABELS_EXT);
	public static void main(String[] args) throws Exception {
		Class<? extends ProbabilisticExplicitTopicModel> algo = EDA.class;
		
		final String topicWordIdxName = "wp_lucene4";
//		final String datasetName = "toy_dataset4";
		final String datasetName = "reuters21578_noblah2";
//		final String datasetName = "sotu_chunks";
		
		final int numComparisons = 200;
		final int chooseFromTopN = 1;
		
		merge(algo, topicWordIdxName, datasetName, numComparisons, chooseFromTopN);
	}
	
	public static void merge(Class<? extends ProbabilisticExplicitTopicModel> algo, String topicWordIdxName, String datasetName,
			int numComparisons, int chooseFromTopN) throws Exception {
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir(topicWordIdxName);
		final String edaLabelsDir = jhn.validation.Paths.edaDocLabelsDir(algo, datasetName);
		final String lauLabelsDir = jhn.validation.Paths.lauDocLabelsDir(datasetName);
		
		LabelSource<String> eda = new RandomRunsDocLabelSource(edaLabelsDir, edaFilenameRgx);
		LabelSource<String> lauEtAl = new RandomRunsDocLabelSource(lauLabelsDir, lauFilenameRgx);
		
		try(IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
			DocLabelSource rand = new RandomDocLabelsSource(labels);
			
			File outputDir = new File(jhn.validation.Paths.hitDataDir(algo, datasetName));
			if(!outputDir.exists()) {
				outputDir.mkdirs();
			}
			
			String outputFilename = jhn.validation.Paths.mergedDocLabelsFilename(algo, datasetName, numComparisons, chooseFromTopN);
			
			MergeHitData mhd = new MergeHitData(datasetName, numComparisons, outputFilename, chooseFromTopN);
			mhd.addModel(eda, algo.getSimpleName(), 0.45);
			mhd.addModel(lauEtAl, "LAU_ET_AL", 0.45);
			mhd.addModel(rand, "RANDOM", 0.1);
			
			mhd.run();
			
			System.out.println(outputFilename);
		}
	}
}
