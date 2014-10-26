package jhn.validation.topiclabel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.FSDirectory;

import cc.mallet.types.LabelAlphabet;

import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;

import jhn.ExtractorParams;
import jhn.counts.i.i.IntIntCounter;
import jhn.counts.i.i.IntIntRAMCounter;
import jhn.eda.Paths;
import jhn.eda.io.FastStateFileReader;
import jhn.eda.lucene.LuceneLabelAlphabet;
import jhn.eda.tokentopics.DocTokenTopics;
import jhn.idx.IntIndex;
import jhn.util.Util;
import jhn.wp.Fields;

public class GenerateHitData {
	private static IntIntCounter topicCounts(String fastStateFilename) throws Exception {
		IntIntCounter counts = new IntIntRAMCounter();
		
		try(FastStateFileReader r = new FastStateFileReader(fastStateFilename)) {
			int lineNum = 0;
			for(DocTokenTopics dtt : r) {
				while(dtt.hasNext()) {
					counts.inc(dtt.nextInt());
				}
				lineNum++;
			}
			
			System.out.print('.');
			if(lineNum > 0 && lineNum % 120 == 0) {
				System.out.println(lineNum);
			}
		}
//		try(BufferedReader r = new BufferedReader(new FileReader(fastStateFilename))) {
//			int lineNum = 0;
//			String tmp = null;
//			while( (tmp=r.readLine()) != null) {
//				if(!tmp.startsWith("#")) {
//					String[] parts = tmp.split("\\s+");
//					for(int i = 3; i < parts.length; i++) {
//						counts.inc(Integer.parseInt(parts[i]));
//					}
//					
//					lineNum++;
//					System.out.print('.');
//					if(lineNum > 0 && lineNum % 120 == 0) {
//						System.out.println(lineNum);
//					}
//				}
//			}
//		}
		
		return counts;
	}
	
	private static class TermCount implements Comparable<TermCount> {
		String term;
		int count;
		public TermCount(String term, int count) {
			this.term = term;
			this.count = count;
		}
		
		@Override
		public int compareTo(TermCount o) {
			return Util.compareInts(o.count, this.count);
		}
	}
	
	private static void generate(String fastStateFilename, String topicWordIdxDir, String topicMappingFilename, String outputFilename) throws Exception {
		try(PrintStream w = new PrintStream(new FileOutputStream(outputFilename));
			IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(topicWordIdxDir)))) {
			
			w.println("topicnum,globaltopicnum,word1,word2,word3,word4,word5,word6,word7,word8,word9,word10,\"label\"");
			
			LabelAlphabet labels = new LuceneLabelAlphabet(topicWordIdx);
			
			System.out.print("Deserializing topic mapping...");
			IntIndex topicMapping = (IntIndex) Util.deserialize(topicMappingFilename);
			System.out.println("done.");
			
			System.out.print("Counting topics...");
			IntIntCounter topicCounts = topicCounts(fastStateFilename);
			System.out.println("done.");
			
			List<Entry> topNtopics = topicCounts.fastTopN(100);
	//		Entry[] entries = topicCounts.int2IntEntrySet().toArray(new Entry[0]);
			topicCounts = null;
			String[] terms;
			int[] counts;
			
	//		final int totalCount = totalCount(entries);
			for(int i = 0; i < 100; i++) {
				System.out.println(i);
	//			int randTopic = randomTopic(entries, totalCount);
				int topic = topNtopics.get(i).getIntKey();
				int globalTopic = topicMapping.objectAtI(topic);
				w.print(topic);
				w.print(',');
				w.print(globalTopic);
				
				TermFreqVector tfv = topicWordIdx.getTermFreqVector(globalTopic, Fields.text);
				if(tfv != null) {
					terms = tfv.getTerms();
					counts = tfv.getTermFrequencies();
				} else {
					terms = new String[0];
					counts = new int[0];
				}
				
				TermCount[] termCounts = new TermCount[terms.length];
				for(int termIdx = 0; termIdx < terms.length; termIdx++) {
					termCounts[termIdx] = new TermCount(terms[termIdx], counts[termIdx]);
				}
				Arrays.sort(termCounts);
				
				for(int wordNum = 0; wordNum < 10; wordNum++) {
					w.print(',');
					w.print(termCounts[wordNum].term);
				}
				
				w.print(",\"");
				w.print(labels.lookupObject(globalTopic));
				w.println('"');
			}
		
		}
	}
	
	public static void main(String[] args) throws Exception {
		ExtractorParams ep = new ExtractorParams();
		ep.minCount = 2;
		ep.topicWordIdxName = "wp_lucene4";
		ep.datasetName = "reuters21578";// toy_dataset2 debates2012 sacred_texts state_of_the_union reuters21578
		
		final int run = 17;
		final int iteration = 95;
		
		final String runDir = Paths.runDir(Paths.defaultRunsDir(), run);
		
		String fastStateFilename =    Paths.fastStateFilename(runDir, iteration);
		String topicWordIdxDir =      jhn.Paths.topicWordIndexDir(ep.topicWordIdxName);
		String topicMappingFilename = jhn.Paths.topicMappingFilename(ep);
		String outputFilename =       Paths.topicLabelHitDataFilename(runDir, iteration);
		
		generate(fastStateFilename, topicWordIdxDir, topicMappingFilename, outputFilename);
	}
}
