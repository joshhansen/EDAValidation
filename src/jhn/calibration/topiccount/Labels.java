package jhn.calibration.topiccount;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import jhn.validation.LabelSource;

/** topicCount -> run -> label source */
class Labels<Key> {
	Int2ObjectMap<Int2ObjectMap<LabelSource<Key>>> labels = new Int2ObjectOpenHashMap<>();
	
	public void setLabelSource(int topicCount, int run, LabelSource<Key> source) {
		getLabels(topicCount).put(run, source);
	}
	
	private Int2ObjectMap<LabelSource<Key>> getLabels(int topicCount) {
		Int2ObjectMap<LabelSource<Key>> map = labels.get(topicCount);
		if(map==null) {
			map = new Int2ObjectOpenHashMap<>();
			labels.put(topicCount, map);
		}
		return map;
	}
	
	LabelSource<Key> getLabels(int topicCount, int run) {
		return getLabels(topicCount).get(run);
	}
}