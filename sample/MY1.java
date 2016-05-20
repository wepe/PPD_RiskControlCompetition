package sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import proccess.NThreadMatrix;
import proccess.NThreadRNB;

import java.util.Random;
import java.util.TreeSet;
import util.Normalizer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class MY1 {
	/** dismap存放两个实例之间的距离，矩阵的上三角 */
	private LinkedHashMap<String, Double> dismap;
	/** rnbmap存放实例邻域半径内的邻居 */
	private LinkedHashMap<Integer, List<Integer>> rnbmap;
	/** 正域 */
	private ArrayList<Integer> POS;
	/** 负域 */
	private ArrayList<Integer> NEG;
	/** 边界域 */
	private ArrayList<Integer> BND;
	public Instances data;

	public MY1(Instances ins, double w, int nthread) throws Exception {
		ins.setClassIndex(ins.numAttributes() - 1);
		NumericToNominal numToNom = new NumericToNominal();
		numToNom.setAttributeIndices("" + (ins.classIndex() + 1));
		numToNom.setInputFormat(ins);
		ins = Filter.useFilter(ins, numToNom);
		ins.randomize(new Random(1)); // 乱序
		getDisMatrix(ins, nthread);// 求距离矩阵
		getRNBmap(ins, w, nthread);
		getClass(ins, 5);
		data = gettrain(ins);//得到最好的训练集
		dismap=null;
		rnbmap=null;
		POS=null;
		NEG=null;
		BND=null;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// DataSource source = new
		// DataSource("G:\\比赛\\微额借款用户人品预测\\data\\7234的数据\\7234的数据\\train_xy_r.csv");
		// Instances ins = source.getDataSet();
		// System.out.println(ins.numInstances());
		// ins.setClassIndex(ins.numAttributes() - 1);
		// NumericToNominal numToNom = new NumericToNominal();
		// numToNom.setAttributeIndices("" + (ins.classIndex() + 1));
		// numToNom.setInputFormat(ins);
		// ins = numToNom.useFilter(ins, numToNom);
		// ins.randomize(new Random(1)); // 乱序
		// // ins = new Normalizer().normalize(ins);// 归一化
		//
		// getDisMatrix(ins);// 求距离矩阵
		// getRNBmap(ins, 0.05);// w 0.01-0.05
		// getClass(ins, 3); // 改一下k的值 3--5
		// Instances finaltrain = gettrain(ins); /////////////// 得到的最后的训练集
		// System.out.println(finaltrain.numInstances());
		// DataSink.write("G:\\比赛\\微额借款用户人品预测\\data\\7234的数据\\7234的数据\\sample.csv",
		// finaltrain);

	}

	/*
	 * 多线程计算
	 */
	public void getRNBmap(Instances ins, double w, int nthred) throws Exception {
		rnbmap = new LinkedHashMap<Integer, List<Integer>>(ins.numInstances());
		// 划分数据
		int size = ins.numInstances() / nthred;
		// 多线程计算
		ArrayList<NThreadRNB> list = new ArrayList<>();
		ArrayList<Thread> threads = new ArrayList<>();
		for (int i = 1; i <= nthred; i++) {
			NThreadRNB nThreadRNB = new NThreadRNB(ins, (i - 1) * size, i * size, dismap, w);
			list.add(nThreadRNB);
			Thread thread = new Thread(nThreadRNB);
			threads.add(thread);
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();// 等待执行完成
		}
		// 合并数据
		for (NThreadRNB nRnb : list) {
			rnbmap.putAll(nRnb.map);
		}
	}

	/*
	 * 多线程计算
	 */
	public void getDisMatrix(Instances ins, int nthred) throws Exception {
		dismap = new LinkedHashMap<String, Double>(ins.numInstances()*ins.numInstances());
		// 划分数据
		int size = ins.numInstances() / nthred;
		ArrayList<NThreadMatrix> list = new ArrayList<>();
		ArrayList<Thread> threads = new ArrayList<>();
		for (int i = 1; i <= nthred; i++) {
			NThreadMatrix nThreadTest = new NThreadMatrix(ins, (i - 1) * size, i * size);
			list.add(nThreadTest);
			Thread thread = new Thread(nThreadTest);
			threads.add(thread);
			thread.start();
		}
		for (Thread thread : threads) {
			thread.join();// 等待执行完成
		}
		// 合并
		for (NThreadMatrix nTest : list) {
			dismap.putAll(nTest.map);
		}
	}

	public Instances getSampleData() {
		try {
			return data;
		} finally {
			System.gc();
		}
	}

	public Instances gettrain(Instances tra) {
		TreeSet<Integer> delSet = new TreeSet<Integer>();
		List<Instance> newins = new ArrayList<Instance>();
		for (Integer index : BND) {
			Map<Integer, Double> map = new LinkedHashMap<Integer, Double>();
			for (int i = 0; i < tra.numInstances(); i++) {
				if (index != i) {
					if (NEG.contains(i)) {
						continue;
					}
					if (POS.contains(i)) {
						continue;
					}

					double value = getDisByIndex(index, i);

					map.put(i, value);
				}
			}

			Map<Integer, Double> newmap = sortMap(map);
			int class0 = 0;
			int class1 = 0;
			Instance[] temp = new Instance[3];
			Instance[] tempxiao = new Instance[3];
			int[] lin = new int[3];
			Iterator<Integer> it = newmap.keySet().iterator();
			for (int i = 0; i < 3; i++) {
				lin[i] = it.next();
			}
			int k = 0;
			int kxiao = 0;

			for (int j = 0; j < lin.length; j++) {
				if (tra.instance(lin[j]).value(tra.numAttributes() - 1) == 0) {
					temp[k] = tra.instance(lin[j]);
					k++;
					class0++;
				} else {
					tempxiao[kxiao] = tra.instance(lin[j]);
					kxiao++;
					class1++;
				}
			}

			Instance instan = new Instance(tra.numAttributes());
			if (class0 == 3) {
				if (tra.instance(index).value(tra.numAttributes() - 1) == 0) {
					delSet.add(index);
					for (int j = 0; j < lin.length; j++) {
						delSet.add(lin[j]);
					}
				}

				if (tra.instance(index).value(tra.numAttributes() - 1) == 1) {
					Instance ins1 = temp[0];
					Instance ins2 = temp[1];
					Instance ins3 = temp[2];
					for (int m = 0; m < tra.numAttributes() - 1; m++) {
						if (tra.attribute(m).isNumeric()) {
							double newAttrValue = (1.0 / 3.0) * (ins1.value(m) + ins2.value(m) + ins3.value(m));

							instan.setValue(m, newAttrValue);
						}

					}
					instan.setValue(instan.numAttributes() - 1, 0);
					newins.add(instan);
					for (int j = 0; j < lin.length; j++) {
						if (tra.instance(lin[j]).value(tra.numAttributes() - 1) == 0) {
							delSet.add(lin[j]);
						}
					}
				}
			}

			if (class0 == 2) {
				if (tra.instance(index).value(tra.numAttributes() - 1) == 0) {
					Instance ins1 = temp[0];
					Instance ins2 = temp[1];
					Instance ins3 = tra.instance(index);
					for (int m = 0; m < tra.numAttributes() - 1; m++) {
						if (tra.attribute(m).isNumeric()) {
							double newAttrValue = (1.0 / 3.0) * (ins1.value(m) + ins2.value(m) + ins3.value(m));

							instan.setValue(m, newAttrValue);
						}

					}
					instan.setValue(instan.numAttributes() - 1, 0);// 3-0
					newins.add(instan);
					delSet.add(index);
					for (int j = 0; j < lin.length; j++) {
						if (tra.instance(lin[j]).value(tra.numAttributes() - 1) == 0) {
							delSet.add(lin[j]);
						}
					}

				}

				if (tra.instance(index).value(tra.numAttributes() - 1) == 1) {
					Instance ins1 = tempxiao[0];
					Instance ins2 = tra.instance(index);
					for (int m = 0; m < tra.numAttributes(); m++) {
						if (tra.attribute(m).isNumeric()) {
							double newAttrValue = (1.0 / 2.0) * (ins1.value(m) + ins2.value(m));
							instan.setValue(m, newAttrValue);
						}

					}
					instan.setValue(instan.numAttributes() - 1, 1);
					newins.add(instan);

				}
			}

			if (class0 == 1) {

				if (tra.instance(index).value(tra.numAttributes() - 1) == 0) {

					Instance ins1 = temp[0];
					Instance ins2 = tra.instance(index);
					for (int m = 0; m < tra.numAttributes() - 1; m++) {
						if (tra.attribute(m).isNumeric()) {
							double newAttrValue = (1.0 / 2.0) * (ins1.value(m) + ins2.value(m));
							instan.setValue(m, newAttrValue);
						}

					}
					instan.setValue(instan.numAttributes() - 1, 0);

					delSet.add(index);
					newins.add(instan);
					for (int j = 0; j < lin.length; j++) {
						if (tra.instance(lin[j]).value(tra.numAttributes() - 1) == 0) {
							delSet.add(lin[j]);
						}
					}

				}
				if (tra.instance(index).value(tra.numAttributes() - 1) == 1) {
					for (int j = 0; j < lin.length; j++) {
						if (tra.instance(lin[j]).value(tra.numAttributes() - 1) == 0) {
							delSet.add(lin[j]);
						}
					}
				}

			}

			if (class0 == 0) {
				if (tra.instance(index).value(tra.numAttributes() - 1) == 0) {
					delSet.add(index);
				}

			}

		}

		for (Integer key : NEG) {
			delSet.add(key);
		}

		Iterator<Integer> iter = delSet.descendingIterator();
		while (iter.hasNext()) {
			tra.delete(iter.next());
		}

		for (int i = 0; i < newins.size(); i++) {
			tra.add(newins.get(i));

		}

		return tra;
	}

	/**
	 * 根据map的value排序
	 */
	public static Map sortMap(Map oldMap) {
		ArrayList<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(oldMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
				if ((o2.getValue() - o1.getValue()) > 0)
					return 1;
				else if ((o2.getValue() - o1.getValue()) == 0)
					return 0;
				else
					return -1;
			}

		});
		// 将排序好的放入新的map
		Map newMap = new LinkedHashMap();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;
	}

	/**
	 * 分 正域，负域和边界域
	 */
	public void getClass(Instances ins, int k) {
		NEG = new ArrayList<Integer>();
		POS = new ArrayList<Integer>();
		BND = new ArrayList<Integer>();
		for (Integer key : rnbmap.keySet()) {
			List<Integer> list = rnbmap.get(key);
			int big = 0;
			int small = 0;
			for (Integer index : list) {
				if (ins.instance(index).value(ins.numAttributes() - 1) == 0) {
					big++;
				} else {
					small++;
				}
			}

			double valueb = (double) big / small;
			double valueS = (double) small / big;
			if (ins.instance(key).value(ins.numAttributes() - 1) == 0) {
				if (valueb >= k) {
					POS.add(key);
				} else if (valueS >= k) {
					NEG.add(key);
				} else {
					BND.add(key);
				}
			}

			if (ins.instance(key).value(ins.numAttributes() - 1) == 1) {
				if (valueS >= k) {
					POS.add(key);
				} else if (valueb >= k) {
					NEG.add(key);
				} else {
					BND.add(key);
				}
			}

			Collections.sort(NEG, Collections.reverseOrder());
		}

	}

	/**
	 * 得每个样本邻居
	 * 
	 * @param r
	 *            邻域半径
	 * 
	 */
	public void getRNBmap(Instances ins, double w) {
		rnbmap = new LinkedHashMap<Integer, List<Integer>>();
		for (int i = 0; i < ins.numInstances(); i++) {
			// if (ins.instance(i).value(ins.numAttributes() - 1) == 1) {//
			// ////////////////////////////////////////////
			double r = getRadius(i, ins, w);
			List<Integer> list = new ArrayList();
			for (int j = 0; j < ins.numInstances(); j++) {
				if (i != j) {
					double tmpdis = getDisByIndex(i, j);
					if (tmpdis < r) {
						list.add(j);
					}
				}
			}
			rnbmap.put(i, list);
			// }
		}
	}

	/**
	 * 根据索引获得两个样本间距离
	 * 
	 * @param index1
	 * @param index2
	 * @return
	 */
	private double getDisByIndex(int index1, int index2) {
		if (dismap.containsKey(index1 + " " + index2)) {
			return dismap.get(index1 + " " + index2);
		} else {
			return dismap.get(index2 + " " + index1);
		}
	}

	/**
	 * 计算距离矩阵，只存放上三角
	 */
	public void getDisMatrix(Instances ins) {
		dismap = new LinkedHashMap<String, Double>();
		for (int i = 0; i < ins.numInstances() - 1; i++) {
			for (int j = i + 1; j < ins.numInstances(); j++) {
				double dist = CalDistance(ins.instance(i), ins.instance(j));
				dismap.put(i + " " + j, dist);
			}
		}

	}

	private double getRadius(int i, Instances ins, double w) {
		double r = 0;
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (int j = 0; j < ins.numInstances(); j++) {
			if (i != j) {
				double tmpdis = getDisByIndex(i, j);
				if (tmpdis > max) {
					max = tmpdis;
				}
				if (tmpdis < min) {
					min = tmpdis;
				}
			}
		}
		r = min + w * (max - min);
		return r;
	}

	/**
	 * 计算两个样本点的距离
	 */
	private double CalDistance(Instance ins1, Instance ins2) {
		double dis = 0;
		for (int j = 0; j < ins1.numAttributes() - 1; j++) {
			if (ins1.isMissing(j) || ins2.isMissing(j)) {
				dis = dis + 1;
			} else if ((!ins1.attribute(j).isNumeric()) && (!ins2.attribute(j).isNumeric())) {
				if (ins1.value(j) != ins2.value(j)) {
					dis = dis + 1;
				}
			} else if (ins1.attribute(j).isNumeric() && ins2.attribute(j).isNumeric()) {
				double t = Math.abs(ins1.value(j) - ins2.value(j));
				dis = dis + t * t;
			}
		}
		dis = Math.sqrt(dis);
		return dis;
	}
}
