package jhn.validation.eda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.counts.Counter;
import jhn.counts.i.i.IntIntCounter;
import jhn.counts.i.i.i.IntIntIntCounterMap;
import jhn.counts.i.i.i.IntIntIntRAMCounterMap;
import jhn.eda.EDA;
import jhn.eda.EDA2;
import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.idx.IntIndex;
import jhn.util.Util;

public class EDASampleSummariesToDocLabels {
	public static IntIntIntCounterMap docTopicCounts(String sampleSummaryFilename) throws Exception {
		System.out.println(sampleSummaryFilename);
		IntIntIntCounterMap counts = new IntIntIntRAMCounterMap();
		
		try(BufferedReader r = new BufferedReader(new FileReader(sampleSummaryFilename))) {
			int docNum;
			int lineNum = 0;
			String tmp = null;
			String[] parts;
			String[] subparts;
			while( (tmp=r.readLine()) != null) {
				if(!tmp.startsWith("#")) {
					parts = tmp.split("\\s+");
					
					docNum = Integer.parseInt(parts[0]);
					
					for(int i = 2; i < parts.length; i++) {
						subparts = parts[i].split(":");
						counts.set(docNum, Integer.parseInt(subparts[0]), Integer.parseInt(subparts[1]));
					}
					
					lineNum++;
					System.out.print('.');
					if(lineNum > 0 && lineNum % 120 == 0) {
						System.out.println(lineNum);
					}
				}
			}
		}
		
		return counts;
	}
	
	public static Int2ObjectMap<String> docFilenames(String sampleSummaryFilename) throws Exception {
		Int2ObjectMap<String> sources = new Int2ObjectOpenHashMap<>();
		
		try(BufferedReader r = new BufferedReader(new FileReader(sampleSummaryFilename))) {
			int docNum;
			String tmp = null;
			String[] parts;
			while( (tmp=r.readLine()) != null) {
				if(!tmp.startsWith("#")) {
					parts = tmp.split("\\s+");
					docNum = Integer.parseInt(parts[0]);
					sources.put(docNum, parts[1]);
				}
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
		try(PrintStream w = new PrintStream(new FileOutputStream(outputFilename))) {
			w.println("#docnum,source,topic1label,topic2label,topic3label,topic4label,topic5label,topic6label,topic7label,topic8label,topic9label,topic10label");
			
			int topicNum;
			int globalTopicNum;
			String label;
			
			@SuppressWarnings("unchecked")
			Int2ObjectMap.Entry<Counter<Integer,Integer>>[] entries = docTopicCounts.int2ObjectEntrySet().toArray(new Int2ObjectMap.Entry[0]);
			Arrays.sort(entries, cmp);
			
			for(Int2ObjectMap.Entry<Counter<Integer,Integer>> entry : entries) {
				w.print(entry.getIntKey());
				w.print(',');
				w.print(docFilenames.get(entry.getIntKey()));
				
				for(Int2IntMap.Entry count : ((IntIntCounter)entry.getValue()).fastTopN(topNlabels)) {
					topicNum = count.getIntKey();
					globalTopicNum = topicMapping.objectAtI(topicNum);
					label = labels.lookupObject(globalTopicNum).toString();
					
					w.print(",\"");
					w.print(label);
					w.print("\"");
				}
				w.println();
			}
		}//end try
	}
	
	public static void main(String[] args) throws Exception {
		Class<? extends EDA> algo = EDA2.class;
		final int minCount = 2;
		final String topicWordIdxName = "wp_lucene4";
		final String datasetName = "sotu_chunks";// toy_dataset2 debates2012 sacred_texts state_of_the_union reuters21578
		final String summarizer = "sum";
//		final int run = 17;
//		final int iteration = 95;
//		final int lastN = 10;
		final int firstIter = 11;
		final int lastIter = 50;
		final int topNlabels = 10;
		final int runCount = 5;
		
		final String runsDir = jhn.validation.Paths.edaRunsDir(algo, datasetName);
		
		for(int run = 0; run < runCount; run++) {
			String runDir = jhn.eda.Paths.runDir(runsDir, run);
			
	//		String fastStateFilename =    Paths.fastStateFilename(run, iteration);
			String sampleSummaryFilename = jhn.eda.Paths.sampleSummaryFilename(summarizer, runDir, firstIter, lastIter, minCount);
			String topicWordIdxDir =      jhn.Paths.topicWordIndexDir("wp_lucene4");
			String topicMappingFilename = jhn.eda.Paths.topicMappingFilename(topicWordIdxName, datasetName, minCount);
			String outputFilename =       jhn.validation.Paths.edaDocLabelsFilename(algo, datasetName, firstIter, lastIter, run);
			
			generate(sampleSummaryFilename, topicWordIdxDir, topicMappingFilename, outputFilename, topNlabels);
		}
	}
}
