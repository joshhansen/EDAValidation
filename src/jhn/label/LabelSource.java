package jhn.label;

public interface LabelSource<K> {
	String[] labels(K labelKey, int numLabels);
}
