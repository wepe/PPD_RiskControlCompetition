package util;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class Normalizer {
	public Instances normalize(Instances ins) throws Exception{
		Normalize normalize = new Normalize();
		normalize.setInputFormat(ins);
		return Filter.useFilter(ins, normalize);
	}
}
