package jhn.validation;

public interface LabelSource<K> {
	String[] labels(K labelKey, int numlabels);
}
