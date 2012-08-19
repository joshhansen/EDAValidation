package jhn.validation.lau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;

import jhn.validation.StandardTopicLabelsSource;
import jhn.validation.TopicLabelsSource;

/**
 * Given doc-topic counts and topic labels, generate document labels
 *
 */
public class GenerateDocLabels {
	public static void generate(String datasetName, String docTopicsDir) throws FileNotFoundException, IOException {
		for(File docTopicsFile : new File(docTopicsDir).listFiles()) {
			String name = docTopicsFile.getName().split("[.]")[0];
			Matcher m = jhn.validation.Paths.NAME_RGX.matcher(name);
			m.matches();
			int topicCount = Integer.parseInt(m.group(1));
			int run = Integer.parseInt(m.group(2));
			
			//topicLabelsDir + "/" + name + jhn.validation.Paths.TOPIC_LABELS_EXT;
			String topicLabelsFilename = jhn.validation.Paths.topicCountCalibrationLauTopicLabelsFilename(datasetName, topicCount, run);

			
			TopicLabelsSource tls = new StandardTopicLabelsSource(null, topicLabelsFilename);
			
//			String outputFilename = outputDir + "/" + name + jhn.validation.Paths. ".doc_labels";
			String outputFilename = jhn.validation.Paths.topicCountCalibrationLauDocLabelsFilename(datasetName, topicCount, run);
			
			try(BufferedReader r = new BufferedReader(new FileReader(docTopicsFile));
					PrintWriter w = new PrintWriter(outputFilename)) {
				
				w.println("#docNum,filename,label1,label2,label3,label4,label5,label6,label7,label8,label9,label10");
				
				String line;
				while( (line=r.readLine()) != null) {
					if(!line.startsWith("#")) {
						String[] parts = line.split("\t");
						w.print(parts[0]);//docNum
						w.print(',');
						w.print(parts[1]);//filename
						
						// Labels:
						for(int i = 2; i < parts.length; i += 2) {
							int topicNum = Integer.parseInt(parts[i]);
							w.print(',');
							w.print('"');
							w.print(tls.labels(topicNum, 1)[0]);
							w.print('"');
						}
						w.println();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String datasetName = "reuters21578_noblah";
		String docTopicsDir = jhn.validation.Paths.topicCountCalibrationDocTopicsDir(datasetName);
		generate(datasetName, docTopicsDir);
	}
}
