package jhn.validation.lau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;

import jhn.io.DocLabelsFileWriter;
import jhn.label.topic.StandardTopicLabelSource;
import jhn.label.topic.TopicLabelSource;

/**
 * Given doc-topic counts and topic labels, generate document labels
 *
 */
public class LauTopicLabelsToDocLabels {
	public static void generate(String datasetName, String docTopicsDir) throws Exception {
		System.out.println(docTopicsDir);
		for(File docTopicsFile : new File(docTopicsDir).listFiles()) {
			System.out.println("\t"+docTopicsFile);
			String name = docTopicsFile.getName().split("[.]")[0];
			Matcher m = jhn.validation.Paths.NAME_RGX.matcher(name);
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			String topicLabelsFilename = jhn.validation.Paths.lauTopicLabelsFilename(datasetName, topicCount, run);

			
			TopicLabelSource tls = new StandardTopicLabelSource(topicLabelsFilename);
			
			String outputFilename = jhn.validation.Paths.lauDocLabelsFilename(datasetName, topicCount, run);
			
			
			
			try(BufferedReader r = new BufferedReader(new FileReader(docTopicsFile));
					DocLabelsFileWriter w = new DocLabelsFileWriter(outputFilename)) {
				String line;
				while( (line=r.readLine()) != null) {
					if(!line.startsWith("#")) {
						String[] parts = line.split("\t");
						w.startDocument(Integer.parseInt(parts[0]), parts[1]);
						
						// Labels:
						for(int i = 2; i < parts.length; i += 2) {
							int topicNum = Integer.parseInt(parts[i]);
							w.label(tls.labels(topicNum, 1)[0]);
						}
						w.endDocument();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		String datasetName = "reuters21578_noblah";
		String docTopicsDir = jhn.validation.Paths.lauDocTopicsDir(datasetName);
		generate(datasetName, docTopicsDir);
	}
}
