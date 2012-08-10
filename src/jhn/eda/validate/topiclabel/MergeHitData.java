package jhn.eda.validate.topiclabel;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

import jhn.eda.validate.Models;

public class MergeHitData {
	private static final Random rand = new Random();
	private static void merge(String edaFilename, String lauEtAlFilename, String outputFilename) throws IOException {
		BufferedReader r1 = new BufferedReader(new FileReader(edaFilename));
		BufferedReader r2 = new BufferedReader(new FileReader(lauEtAlFilename));
		r1.readLine();
		r2.readLine();
		
		PrintStream w =  new PrintStream(new FileOutputStream(outputFilename));
		w.print("model1,model2,");
		w.print("model1topicnum,model1word1,model1word2,model1word3,model1word4,model1word5,model1word6,model1word7,model1word8,model1word9,model1word10,model1label,");
		w.println("model2topicnum,model2word1,model2word2,model2word3,model2word4,model2word5,model2word6,model2word7,model2word8,model2word9,model2word10,model2label");
		
		String edaLine = null;
		String lauLine;
		
		Models firstModel;
		Models secondModel;
		String firstModelFields;
		String secondModelFields;
		
		while( (edaLine=r1.readLine()) != null) {
			lauLine = r2.readLine();
			if(rand.nextBoolean()) {
				firstModel = Models.EDA;
				secondModel = Models.LAU_ET_AL;
				firstModelFields = edaLine;
				secondModelFields = lauLine;
			} else {
				firstModel = Models.LAU_ET_AL;
				secondModel = Models.EDA;
				firstModelFields = lauLine;
				secondModelFields = edaLine;
			}
			
			w.append(firstModel.toString()).append(',');
			w.append(secondModel.toString()).append(',');
			w.print(firstModelFields);
			w.print(',');
			w.println(secondModelFields);
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		merge(jhn.Paths.outputDir("EDA") + "/runs/17/hit_data_it95.hit.csv",
			  jhn.Paths.outputDir("LauEtAl") + "/reuters-labels.hit.csv",
		      jhn.Paths.outputDir("EDAValidation") + "/merged_reuters.hit.csv");
	}
}
