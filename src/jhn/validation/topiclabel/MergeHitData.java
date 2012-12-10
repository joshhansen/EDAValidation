package jhn.validation.topiclabel;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import jhn.eda.EDA1;
import jhn.idx.IntIndex;
import jhn.io.TopTopicsReader;
import jhn.label.LabelSource;
import jhn.label.topic.AggregateTopicLabelSource;
import jhn.label.topic.RestrictedLuceneTopicLabelSource;
import jhn.label.topic.RestrictedRandomTopicLabelSource;
import jhn.label.topic.SampleableTopicLabelSource;
import jhn.util.Util;
import jhn.validation.Merger;
import jhn.validation.Paths;

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
		
		SampleableTopicLabelSource[] sources = new SampleableTopicLabelSource[] {
				(SampleableTopicLabelSource) src1, (SampleableTopicLabelSource) src2
		};
		
		int[] topics = new int[2];
		String[] labels = new String[2];
		String[][] words = new String[2][];
		
		for(int i = 0; i < sources.length; i++) {
			do {
				topics[i] = sources[i].randTopicNum();
				labels[i] = sources[i].labels(topics[i], chooseFromTopN)[0];
				words[i] = sources[i].topicWords(topics[i]);
			} while(words[i].length < numTopicWords);
		}
		
		StringBuilder line = new StringBuilder();
		
		for(int i = 0; i < sources.length; i++) {
			line.append(modelName(sources[i])).append(',')
				.append(topics[i]).append(',')
				.append('"').append(labels[i]).append("\",");
		}
		
		for(int side = 0; side < sources.length; side++) {
			for(int i = 0; i < numTopicWords; i++) {
				String word = words[side].length > i ? words[side][i] : "";
				line.append(word);
				if(side < words.length-1 || i < numTopicWords - 1) {
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
				h.append("model").append(sides[i]).append("word").append(j+1);
				if(i < sides.length - 1 || j < numTopicWords - 1) {
					h.append(',');
				}
			}
		}
		
		return h.toString();
	}
	
	public static void main(String[] args) throws Exception {
		final int topicCount = 10;
		final String topicWordIdxName = "wp_lucene4";
//		final String datasetName = "toy_dataset4";
//		final String datasetName = "reuters21578_noblah2";
		String datasetName = "sotu_chunks";
		String topicWordIdxDir = jhn.Paths.topicWordIndexDir(topicWordIdxName);
		final int numComparisons = 200;
		final int chooseFromTopN = 1;
		final int minCount = 2;
		
		final String lauLabelsDir = jhn.validation.Paths.lauTopicLabelsDir(datasetName);
		
		
//		Pattern edaFilenameRgx = Pattern.compile("run(\\d+)_iters\\d+-\\d+\\" + jhn.Paths.DOC_LABELS_EXT);
//		DocLabelSource eda = new RandomRunsDocLabelSource(edaLabelsDir, edaFilenameRgx);
		IntSet allowedTopics = null;
		try(TopTopicsReader r = new TopTopicsReader(Paths.topTopicsFilename(EDA1.class, datasetName))) {
			allowedTopics = r.readTopicsSet(100);
		}
		
		String topicMappingFilename = jhn.Paths.topicMappingFilename(topicWordIdxName, datasetName, minCount);
		IntIndex topicMapping = (IntIndex) Util.deserialize(topicMappingFilename);
		
		IntSet actuallyAllowedTopics = new IntOpenHashSet();
		for(int i : allowedTopics) {
			actuallyAllowedTopics.add(topicMapping.objectAtI(i));
		}
		
		LabelSource<Integer> eda = new RestrictedLuceneTopicLabelSource(topicWordIdxDir, datasetName, actuallyAllowedTopics);
		
		Pattern lauFilenameRgx = Pattern.compile("lda" + topicCount + "topics_(\\d+)\\" + jhn.Paths.TOPIC_LABELS_EXT);
//		DocLabelSource lauEtAl = new RandomRunsDocLabelSource(lauLabelsDir, lauFilenameRgx);
//		LabelSource<Integer> lauEtAl = new RandomRunsTopicLabelSource(lauLabelsDir, lauFilenameRgx);
		LabelSource<Integer> lauEtAl = new AggregateTopicLabelSource(lauLabelsDir, lauFilenameRgx);
		
		try(IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			LabelSource<Integer> random = new RestrictedRandomTopicLabelSource(topicWordIdx, datasetName, actuallyAllowedTopics);
			
			File outputDir = new File(jhn.validation.Paths.hitDataDir(datasetName));
			if(!outputDir.exists()) {
				outputDir.mkdirs();
			}
			
			String outputFilename = jhn.validation.Paths.mergedTopicLabelsFilename(datasetName, numComparisons, chooseFromTopN) + ".restricted";
			
			MergeHitData mhd = new MergeHitData(numComparisons, outputFilename, chooseFromTopN);
			mhd.addModel(eda, "EDA", 0.45);
			mhd.addModel(lauEtAl, "LAU_ET_AL", 0.45);
			mhd.addModel(random, "RANDOM", 0.1);
			
			mhd.run();
			
			System.out.println(outputFilename);
		}
	}

}
