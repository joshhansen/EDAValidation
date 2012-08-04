package jhn.eda.hit;

import java.io.File;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import jhn.assoc.AssociationMeasure;
import jhn.assoc.AverageWordWordPMI;
import jhn.lauetal.LauEtAl;
import jhn.lauetal.Options;
//import jhn.lauetal.ts.GoogleTitleSearcher;
import jhn.lauetal.tc.HTTPTitleChecker;
import jhn.lauetal.tc.LuceneTitleChecker;
import jhn.lauetal.tc.OrderedTitleChecker;
import jhn.lauetal.tc.TitleChecker;
import jhn.lauetal.ts.LuceneTitleSearcher;
import jhn.lauetal.ts.MediawikiTitleSearcher;
import jhn.lauetal.ts.OrderedTitleSearcher;
import jhn.lauetal.ts.TitleSearcher;
import jhn.lauetal.ts.UnionTitleSearcher;
import jhn.util.Config;
import jhn.util.Log;


public class TopicCountCalibration_RunLau implements AutoCloseable {
	private LauEtAl lauEtAl;
	
	public void init() throws Exception {
		IndexReader topicWordIdx = IndexReader.open(FSDirectory.open(new File(jhn.eda.Paths.topicWordIndexDir("wp_lucene4"))));
		IndexReader titleIdx = IndexReader.open(FSDirectory.open(new File(jhn.Paths.titleIndexDir())));
		final String linksDir = jhn.eda.Paths.indexDir("page_links");
		final String artCatsDir = jhn.eda.Paths.indexDir("article_categories");
		
		Config conf = new Config();
		conf.putInt(Options.PROP_PMI_MAX_HITS, 1000);
		conf.putInt(Options.TITLE_SEARCHER_TOP_N, 10);
		conf.putDouble(Options.MIN_AVG_RACO, 0.1);
		conf.putInt(Options.NUM_FALLBACK_CANDIDATES, 5);
		conf.putInt(Options.TITLE_UNION_TOP_N, 10);
		conf.putBool(Options.SPOOF_DELAY, false);
		
		Log log = new Log(System.out, Paths.topicCountCalibrationLogFilename());
		log.println("Lau, et al. configuration:");
		log.println(conf);
		
		String wordIdxFilename = jhn.Paths.outputDir("JhnCommon") + "/word_sets/chunks/19.set";
		String countsDbFilename = jhn.Paths.outputDir("JhnCommon") + "/counts/counts.sqlite3";
		String cocountsDbFilename = jhn.Paths.outputDir("JhnCommon") + "/cocounts/cocounts.sqlite3";
		AssociationMeasure<String,String> assocMeasure = new AverageWordWordPMI(wordIdxFilename, countsDbFilename, cocountsDbFilename);
		
		OrderedTitleSearcher ts1 = new MediawikiTitleSearcher(conf.getInt(Options.TITLE_SEARCHER_TOP_N));
		OrderedTitleSearcher ts2 = new LuceneTitleSearcher(topicWordIdx, conf.getInt(Options.TITLE_SEARCHER_TOP_N));
//		OrderedTitleSearcher ts3 = new GoogleTitleSearcher(conf.getInt(Options.TITLE_SEARCHER_TOP_N));
		
		TitleSearcher ts = new UnionTitleSearcher(conf.getInt(Options.TITLE_UNION_TOP_N), ts1, ts2);
		TitleChecker tc = new OrderedTitleChecker(new LuceneTitleChecker(titleIdx), new HTTPTitleChecker());
		lauEtAl = new LauEtAl(conf, log, linksDir, artCatsDir, jhn.lauetal.Paths.chunkerFilename(),
				jhn.lauetal.Paths.posTaggerFilename(), assocMeasure, ts, tc);
	}

	private void runLau(int numTopics, int run) throws Exception {
		String keysFilename = Paths.topicCountCalibrationKeysFilename(numTopics, run);
		String topicLabelsFilename = Paths.topicCountCalibrationLauLabelsFilename(numTopics, run);
		lauEtAl.labelAllTopics(keysFilename, topicLabelsFilename);
	}
	
	public void run() throws Exception {
		for(int numTopics : new int[] {200, 100, 50, 20, 10}) {
			for(int run : new int[] {1, 0, 2, 3, 4}) {
				runLau(numTopics, run);
			}
		}
	}
	
	@Override
	public void close() throws Exception {
		lauEtAl.close();
	}
	
	public static void main(String[] args) throws Exception {
		try(TopicCountCalibration_RunLau tcc_rl = new TopicCountCalibration_RunLau()) {
			tcc_rl.init();
			tcc_rl.run();
		}
	}
}
