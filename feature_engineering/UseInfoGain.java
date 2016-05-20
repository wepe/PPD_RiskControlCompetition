package feature;

import java.util.Enumeration;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class UseInfoGain {
	/**
	 * Computes information gain for an attribute.
	 *
	 * @param data
	 *            the data for which info gain is to be computed
	 * @param att
	 *            the attribute
	 * @return the information gain for the given attribute and data
	 * @throws Exception
	 *             if computation fails
	 */
	public static double computeInfoGain(Instances data, Attribute att) throws Exception {
		double infoGain = computeEntropy(data);
		Instances[] splitData = splitData(data, att);
		for (int j = 0; j < data.numDistinctValues(att); j++) {
			if (splitData[j].numInstances() > 0) {
				infoGain -= ((double) splitData[j].numInstances() / (double) data.numInstances())
						* computeEntropy(splitData[j]);
			}
		}
		return infoGain;
	}

	/**
	 * Computes the entropy of a dataset.
	 * 
	 * @param data
	 *            the data for which entropy is to be computed
	 * @return the entropy of the data's class distribution
	 * @throws Exception
	 *             if computation fails
	 */
	public static double computeEntropy(Instances data) throws Exception {
		double[] classCounts = new double[data.numClasses()];
		Enumeration instEnum = data.enumerateInstances();
		while (instEnum.hasMoreElements()) {
			Instance inst = (Instance) instEnum.nextElement();
			classCounts[(int) inst.classValue()]++;
		}
		double entropy = 0;
		for (int j = 0; j < data.numClasses(); j++) {
			if (classCounts[j] > 0) {
				entropy -= classCounts[j] * Utils.log2(classCounts[j]);
			}
		}
		entropy /= (double) data.numInstances();
		return entropy + Utils.log2(data.numInstances());
	}

	/**
	 * Splits a dataset according to the values of a nominal attribute.
	 *
	 * @param data
	 *            the data which is to be split
	 * @param att
	 *            the attribute to be used for splitting
	 * @return the sets of instances produced by the split
	 */
	public static Instances[] splitData(Instances data, Attribute att) {
		int size=data.numDistinctValues(att);
		Instances[] splitData = new Instances[size];
		for (int j = 0; j < size; j++) {
			splitData[j] = new Instances(data, data.numInstances());
		}
		Enumeration instEnum = data.enumerateInstances();
		while (instEnum.hasMoreElements()) {
			Instance inst = (Instance) instEnum.nextElement();
			splitData[(int) inst.value(att)].add(inst);
		}
		for (int i = 0; i < splitData.length; i++) {
			splitData[i].compactify();
		}
		return splitData;
	}
	public static void main(String[] args)throws Exception {
//		long t3=System.currentTimeMillis();
//		Instances train = DataSource.read("E:\\Program Files\\Weka-3-7\\data\\soybean.arff");
		Instances train = DataSource.read("G:\\比赛\\微额借款用户人品预测\\train_xy.csv");
		train.setClassIndex(train.numAttributes()-1);
		NumericToNominal filter = new NumericToNominal();
		String options2[] = new String[2];
		options2[0] = "-R";
		options2[1] = "last";
		filter.setOptions(options2);
		filter.setInputFormat(train);
		train=Filter.useFilter(train, filter);
		int index=10;
		System.out.println(UseInfoGain.computeInfoGain(train, train.attribute(index)));
//		long t1=System.currentTimeMillis();
		InfoGainEval in=new InfoGainEval();
		System.out.println(in.computeInfoGain(train, index)) ;
//		long t2=System.currentTimeMillis();
//		System.out.println("加载时间"+(t1-t3));
//		System.out.println("特征评估时间"+(t2-t1));
	}
}
