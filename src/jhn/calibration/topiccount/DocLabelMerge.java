package jhn.calibration.topiccount;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import jhn.util.RandUtil;
import jhn.util.Util;
import jhn.validation.LabelSource;
import jhn.validation.Paths;
import jhn.validation.StandardDocLabelSource;

public class DocLabelMerge extends CalibrationMerge<String> {

	private String[] docFilenames;
	public DocLabelMerge(String datasetName, int comparisonsPerPair) {
		super(Paths.topicCountCalibrationLauDocLabelsDir(datasetName),
				Paths.topicCountCalibrationMergedLauDocLabelsFilename(datasetName),
				comparisonsPerPair);
		
		final List<String> filenames = new ArrayList<>();
		InstanceList data = InstanceList.load(new File(jhn.Paths.malletDatasetFilename(datasetName)));
		for(Instance inst : data) {
			filenames.add(inst.getSource().toString());
		}
		this.docFilenames = filenames.toArray(new String[0]);
	}
	
	@Override
	protected String headerLine() {
		StringBuilder header = new StringBuilder();
		header.append("docFilename,docText,");
		
		final String[] sides = new String[]{"1","2"};
		for(String side : sides) {
			header.append("topicCount").append(side).append(',');
			header.append("run").append(side).append(',');
		}
		
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

	private static final int MIN_CHARS = 80;
	@Override
	protected String mergeLine(Labels<String> labels, final int topicCount1, final int run1, final int topicCount2, final int run2) throws Exception {
		StringBuilder line = new StringBuilder();
		String filename;
		String[] parts;
		String shortFilename;
		String docText;
		do {
			filename = randomFilename();
			parts = filename.split("/");
			shortFilename = parts[parts.length - 1];
			docText = Util.readFile(filename);
		} while(docText.length() < MIN_CHARS);
		
		String label1 = labels.getLabels(topicCount1, run1).labels(filename, 1)[0];
		String label2 = labels.getLabels(topicCount2, run2).labels(filename, 1)[0];
		
		field(line, filename);
		field(line, cleanDocText(docText));
		field(line, topicCount1);
		field(line, run1);
		field(line, topicCount2);
		field(line, run2);
		field(line, label1);
		field(line, wpLinkify(label1));
		field(line, label2);
		field(line, wpLinkify(label2), true, false);
		
		return line.toString();
	}
	
	private static void field(StringBuilder sb, int x) {
		field(sb, x, false, true);
	}
	
	private static void field(StringBuilder sb, int x, boolean quote, boolean comma) {
		if(quote) {
			sb.append('"');
		}
		
		sb.append(x);
		
		if(quote) {
			sb.append('"');
		}
		
		if(comma) {
			sb.append(',');
		}
	}
	
	private static void field(StringBuilder sb, String s) {
		field(sb, s, true, true);
	}
	
	private static void field(StringBuilder sb, String s, boolean quote, boolean comma) {
		if(quote) {
			sb.append('"');
		}
		
		sb.append(s);
		
		if(quote) {
			sb.append('"');
		}
		
		if(comma) {
			sb.append(',');
		}
	}
	
	private static String cleanDocText(String docText) throws UnsupportedEncodingException {
		return URLEncoder.encode(docText, "utf-8").replaceAll("\\n", "<br/>");
	}
	
	private static String wpLinkify(String label) {
		return "http://en.wikipedia.org/wiki/" + label.replace(" ", "_");
	}

	@Override
	protected LabelSource<String> createLabelSource(File file) throws IOException {
		return new StandardDocLabelSource(file.getPath());
	}

	protected String randomFilename() {
		return RandUtil.randItem(docFilenames);
	}
	
	public static void main(String[] args) throws Exception {
		final int comparisonsPerPair = 10;
//		String datasetName = "reuters21578";
		String datasetName = "state_of_the_union";

		CalibrationMerge<String> tccm = new DocLabelMerge(datasetName, comparisonsPerPair);
		tccm.run();
	}
}
