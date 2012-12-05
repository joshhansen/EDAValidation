package jhn.validation.topiclabel;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import jhn.label.LabelSource;
import jhn.label.topic.LuceneTopicLabelSource;
import jhn.label.topic.RandomRunsTopicLabelSource;
import jhn.label.topic.RandomTopicLabelsSource;
import jhn.label.topic.SampleableTopicLabelSource;
import jhn.util.RandUtil;
import jhn.validation.Merger;

public class MergeHitData extends Merger<Integer> {
	private final int chooseFromTopN;
	private final int numTopicWords = 10;
	public MergeHitData(int comparisons, String destFilename, int chooseFromTopN) {
		super(comparisons, destFilename);
		this.chooseFromTopN = chooseFromTopN;
	}
	
	@Override
	protected String mergeLine(LabelSource<Integer> src1, LabelSource<Integer> src2) throws Exception {
		System.out.println(modelName(src1));
		System.out.println(modelName(src2));
		
		SampleableTopicLabelSource stls1 = (SampleableTopicLabelSource) src1;
		SampleableTopicLabelSource stls2 = (SampleableTopicLabelSource) src2;
		
		int topic1 = stls1.randTopicNum();
		int topic2 = stls2.randTopicNum();
		
		String[] labels1 = stls1.labels(topic1, chooseFromTopN);
		String[] labels2 = stls2.labels(topic2, chooseFromTopN);
		
		String label1 = cleanLabel(RandUtil.randItem(labels1));
		String label2 = cleanLabel(RandUtil.randItem(labels2));
		
		String[][] words = new String[][]{stls1.topicWords(topic1), stls2.topicWords(topic2)};
		
		StringBuilder line = new StringBuilder();
		
		line.append(modelName(src1)).append(',')
			.append(topic1).append(',')
			.append(label1).append(',')
			.append(modelName(src2)).append(',')
			.append(topic2).append(',')
			.append(label2).append(',');
		
		for(int side = 0; side < words.length; side++) {
			for(int i = 0; i < numTopicWords; i++) {
				String word = words[side].length > i ? words[side][i] : "";
				line.append(word);
				if(i < numTopicWords - 1) {
					line.append(',');
				}
			}
		}
		
		return line.toString();
	}

	@Override
	protected String headerLine() {
		StringBuilder h = new StringBuilder();
		final String[] sides = new String[]{"1","2"};
		for(String side : sides) {
			h.append("model").append(side).append(',')
			 .append("model").append(side).append("topicNum,")
			 .append("model").append(side).append("label,");
		}
		
		for(int i = 0; i < sides.length; i++) {
			for(int j = 0; j < numTopicWords; j++) {
				h.append("model").append(sides[i]).append("word").append(j);
				if(i < sides.length - 1 || j < numTopicWords - 1) {
					h.append(',');
				}
			}
		}
		
		return h.toString();
		
//		return "model1,model2,"
//			+ "model1topicnum,model1word1,model1word2,model1word3,model1word4,model1word5,model1word6,model1word7,model1word8,model1word9,model1word10,model1label,"
//			+ "model2topicnum,model2word1,model2word2,model2word3,model2word4,model2word5,model2word6,model2word7,model2word8,model2word9,model2word10,model2label\n";
	}
	
	public static void main(String[] args) throws Exception {
//		merge(jhn.Paths.outputDir("EDA") + "/runs/17/hit_data_it95.hit.csv",
//			  jhn.Paths.outputDir("LauEtAl") + "/reuters-labels.hit.csv",
//		      jhn.Paths.outputDir("EDAValidation") + "/merged_reuters.hit.csv");
		
//		Class<? extends EDA> algo = EDA2.class;
		final int topicCount = 20;
		final String topicWordIdxName = "wp_lucene4";
//		final String datasetName = "toy_dataset4";
		final String datasetName = "reuters21578_noblah2";
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir(topicWordIdxName);
		final int numComparisons = 200;
		final int chooseFromTopN = 1;
		
		final String lauLabelsDir = jhn.validation.Paths.lauTopicLabelsDir(datasetName);
		
		
//		Pattern edaFilenameRgx = Pattern.compile("run(\\d+)_iters\\d+-\\d+\\" + jhn.Paths.DOC_LABELS_EXT);
//		DocLabelSource eda = new RandomRunsDocLabelSource(edaLabelsDir, edaFilenameRgx);
		LabelSource<Integer> eda = new LuceneTopicLabelSource(topicWordIdxDir);
		
		Pattern lauFilenameRgx = Pattern.compile("lda" + topicCount + "topics_(\\d+)\\" + jhn.Paths.TOPIC_LABELS_EXT);
//		DocLabelSource lauEtAl = new RandomRunsDocLabelSource(lauLabelsDir, lauFilenameRgx);
		LabelSource<Integer> lauEtAl = new RandomRunsTopicLabelSource(lauLabelsDir, lauFilenameRgx);
		
		try(IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			LabelSource<Integer> random = new RandomTopicLabelsSource(topicWordIdx);
			
			File outputDir = new File(jhn.validation.Paths.hitDataDir(datasetName));
			if(!outputDir.exists()) {
				outputDir.mkdirs();
			}
			
			String outputFilename = jhn.validation.Paths.mergedTopicLabelsFilename(datasetName, numComparisons, chooseFromTopN);
			
			MergeHitData mhd = new MergeHitData(numComparisons, outputFilename, chooseFromTopN);
			mhd.addModel(eda, "EDA", 0.45);
			mhd.addModel(lauEtAl, "LAU_ET_AL", 0.45);
			mhd.addModel(random, "RANDOM", 0.1);
			
			mhd.run();
			
			System.out.println(outputFilename);
		}
	}

}
