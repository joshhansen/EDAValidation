package jhn.validation.eda;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.ExtractorParams;
import jhn.counts.Counter;
import jhn.counts.i.i.IntIntCounter;
import jhn.counts.i.i.i.IntIntIntCounterMap;
import jhn.counts.i.i.i.IntIntIntRAMCounterMap;
import jhn.eda.ProbabilisticExplicitTopicModel;
import jhn.eda.EDA;
import jhn.eda.io.SampleSummaryFileReader;
import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.eda.summarize.Sum;
import jhn.eda.summarize.SummaryParams;
import jhn.eda.tokentopics.DocTopicCounts;
import jhn.idx.IntIndex;
import jhn.io.DocLabelsFileWriter;
import jhn.util.Util;

public class SampleSummariesToDocLabels {
	public static IntIntIntCounterMap docTopicCounts(String sampleSummaryFilename) throws Exception {
		System.out.println(sampleSummaryFilename);
		IntIntIntCounterMap counts = new IntIntIntRAMCounterMap();
		
		try(SampleSummaryFileReader r = new SampleSummaryFileReader(sampleSummaryFilename)) {
			for(DocTopicCounts dtc : r) {
				IntIntCounter subcounts = counts.getCounter(dtc.docNum());
				while(dtc.hasNext()) {
					subcounts.set(dtc.nextInt(), dtc.nextDocTopicCount());
				}
			}
		}
		
		return counts;
	}
	
	public static Int2ObjectMap<String> docFilenames(String sampleSummaryFilename) throws Exception {
		Int2ObjectMap<String> sources = new Int2ObjectOpenHashMap<>();
		
		try(SampleSummaryFileReader r = new SampleSummaryFileReader(sampleSummaryFilename)) {
			for(DocTopicCounts dtc : r) {
				sources.put(dtc.docNum(), dtc.docSource());
			}
		}
		
		return sources;
	}
	
	private static final Comparator<Int2ObjectMap.Entry<?>> cmp = new Comparator<Int2ObjectMap.Entry<?>>(){
		@Override
		public int compare(Entry<?> o1, Entry<?> o2) {
			return Util.compareInts(o1.getIntKey(), o2.getIntKey());
		}
	};
	
	private static void generate(String sampleSummaryFilename, String topicWordIdxDir, String topicMappingFilename,
								   String outputFilename, int topNlabels) throws Exception {
		
		System.out.print("Counting topics...");
		IntIntIntCounterMap docTopicCounts = docTopicCounts(sampleSummaryFilename);
		System.out.println("done.");
		
		System.out.print("Mapping document sources...");
		Int2ObjectMap<String> filenames = docFilenames(sampleSummaryFilename);
		System.out.println("done.");
		
		System.out.print("Deserializing topic mapping...");
		IntIndex topicMapping = (IntIndex) Util.deserialize(topicMappingFilename);
		System.out.println("done.");
		
		try(IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
			generate(docTopicCounts, filenames, labels, topicMapping, topNlabels, outputFilename);
		}
	}
	
	private static void generate(IntIntIntCounterMap docTopicCounts, Int2ObjectMap<String> docFilenames,
								   LabelAlphabet labels, IntIndex topicMapping, int topNlabels, String outputFilename) throws Exception {
		
		System.out.println("Writing " + outputFilename);
		
		try(DocLabelsFileWriter w = new DocLabelsFileWriter(outputFilename, topNlabels)) {
			int topicNum;
			int globalTopicNum;
			String label;
			
			@SuppressWarnings("unchecked")
			Int2ObjectMap.Entry<Counter<Integer,Integer>>[] entries = docTopicCounts.int2ObjectEntrySet().toArray(new Int2ObjectMap.Entry[0]);
			Arrays.sort(entries, cmp);
			
			for(Int2ObjectMap.Entry<Counter<Integer,Integer>> entry : entries) {
				w.startDocument(entry.getIntKey(), docFilenames.get(entry.getIntKey()));
				
				for(Int2IntMap.Entry count : ((IntIntCounter)entry.getValue()).fastTopN(topNlabels)) {
					topicNum = count.getIntKey();
					globalTopicNum = topicMapping.objectAtI(topicNum);
					label = labels.lookupObject(globalTopicNum).toString();
					
					w.label(label);
				}
				w.endDocument();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		Class<? extends ProbabilisticExplicitTopicModel> algo = EDA.class;
		final ExtractorParams ep = new ExtractorParams();
		ep.minCount = 2;
		
		ep.topicWordIdxName = "wp_lucene4";
		ep.datasetName = "reuters21578_noblah2";// toy_dataset2 debates2012 sacred_texts state_of_the_union reuters21578
//		ep.datasetName = "sotu_chunks";
//		final int run = 17;
//		final int iteration = 95;
//		final int lastN = 10;
		
		SummaryParams sp = new SummaryParams();
		sp.summarizerCls = Sum.class;
		sp.firstIter = 10;
		sp.lastIter = 50;
		sp.minCount = 0;
		sp.includeClass = true;
		
		final int topNlabels = 10;
		final int runCount = 5;
		
		convert(algo, ep, runCount, sp, topNlabels);
	}
	
	public static void convert(Class<? extends ProbabilisticExplicitTopicModel> algo, ExtractorParams ep, int runCount, SummaryParams sp, int topNlabels) throws Exception {
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, ep.datasetName);
		
		new File(jhn.validation.Paths.edaDocLabelsDir(algo, ep.datasetName)).mkdirs();
		
		for(int run = 0; run < runCount; run++) {
			String runDir = jhn.eda.Paths.runDir(runsDir, run);
			
	//		String fastStateFilename =    Paths.fastStateFilename(run, iteration);
			String sampleSummaryFilename = jhn.eda.Paths.sampleSummaryFilename(runDir, sp);
			String topicWordIdxDir =      jhn.Paths.topicWordIndexDir(ep.topicWordIdxName);
			String topicMappingFilename = jhn.Paths.topicMappingFilename(ep);
			String outputFilename =       jhn.validation.Paths.edaDocLabelsFilename(algo, ep.datasetName, sp.firstIter, sp.lastIter, run);
			
			generate(sampleSummaryFilename, topicWordIdxDir, topicMappingFilename, outputFilename, topNlabels);
		}
	}
}
