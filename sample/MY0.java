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
import java.util.Random;
import java.util.TreeSet;

import proccess.NThreadMatrix;
import proccess.NThreadRNB;
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

public class MY0 {
	/** dismap存放两个实例之间的距离，矩阵的上三角 */
	private  LinkedHashMap<String, Double> dismap;
	/** rnbmap存放实例邻域半径内的邻居 */
	private LinkedHashMap<Integer, List<Integer>> rnbmap;
	private Instances data;
	public MY0(){super();}
	public MY0(Instances ins,int num,double w,int nthread)throws Exception{
		ins.setClassIndex(ins.numAttributes() - 1);
		NumericToNominal numToNom = new NumericToNominal();
		numToNom.setAttributeIndices("" + (ins.classIndex() + 1));
		numToNom.setInputFormat(ins);
		ins = Filter.useFilter(ins, numToNom);
		getDisMatrix(ins,nthread);
		int k = num;// k 表示要增加少数类的个数 ，
//		double w = 0.01;// w表示邻域半径 通常是 0.01 到 0.10 可适当的调整。

		getRNBmap(ins, w,nthread);// 得到邻居

		List<Instance> newins = getClass(ins, k);// newins 表示得到的新采样的数据的集合 ，输出即可。
		for (Instance instance : newins) {
			System.out.println(instance);
			ins.add(instance);
		}
		data=ins;
		dismap=null;
		rnbmap=null;
	}
	public Instances getSampleIns(Instances ins,int num,double w,int nthread)throws Exception{
		ins.setClassIndex(ins.numAttributes() - 1);
		NumericToNominal numToNom = new NumericToNominal();
		numToNom.setAttributeIndices("" + (ins.classIndex() + 1));
		numToNom.setInputFormat(ins);
		ins = Filter.useFilter(ins, numToNom);
		getDisMatrix(ins,nthread);
		int k = num;// k 表示要增加少数类的个数 ，
//		double w = 0.01;// w表示邻域半径 通常是 0.01 到 0.10 可适当的调整。

		getRNBmap(ins, w,nthread);// 得到邻居

		List<Instance> newins = getClass(ins, k);// newins 表示得到的新采样的数据的集合 ，输出即可。
		Instances result=new Instances(ins);
		result.delete();
		for (Instance instance : newins) {
			result.add(instance);
		}
		dismap=null;
		rnbmap=null;
		return result;
	}
	public static void main(String[] args) throws Exception {
//		for(int i=0;i<8;i++)
		int i=7;
		{
			DataSource source = new DataSource("data/data"+i+".csv");
			Instances ins = source.getDataSet();
			ins.setClassIndex(ins.numAttributes() - 1);
			NumericToNominal numToNom = new NumericToNominal();
			numToNom.setAttributeIndices("" + (ins.classIndex() + 1));
			numToNom.setInputFormat(ins);
			ins = Filter.useFilter(ins, numToNom);
			ins.randomize(new Random(1)); // 乱序
//			ins = new Normalizer().normalize(ins);// 归一话
			MY0 my0=new MY0();
			my0.getDisMatrix(ins);// 得到距离矩阵；

			int k = 50;// k 表示要增加少数类的个数 ，
			double w = 0.01;// w表示邻域半径 通常是 0.01 到 0.10 可适当的调整。

			my0.getRNBmap(ins, w);// 得到邻居

			List<Instance> newins = my0.getClass(ins, k);// newins 表示得到的新采样的数据的集合 ，输出即可。
			Instances sa=new Instances(ins);
			sa.delete();
			for (Instance instance : newins) {
				System.out.println(instance);
				sa.add(instance);
			}
			DataSink.write("G:\\比赛\\魔镜杯风控算法大赛\\data\\复赛\\sample"+i+".csv", sa);
			System.out.println("sample"+i+"complete");
		}
		
	}

	public Instances getSampleData()
	{
		try {
			return data;
		} finally {
			System.gc();
		}
	}
	
	/**
	 * 对邻域中少数类样本采样
	 * 
	 * @return
	 */
	public  List<Instance> getClass(Instances ins, int k) {
		int a = 0;
		List<Instance> newins = new ArrayList<Instance>();
		for (Integer key : rnbmap.keySet()) {
			List<Integer> list = rnbmap.get(key);
			if (list.size() != 0) {
				Instance inskey = ins.instance(list.get(0));
				if (list.size() > 3) {
					for (int i = 1; i < 3; i++) {
						Instance instance = new Instance(ins.numAttributes());
						Instance insvalue = ins.instance(list.get(i));
						for (int m = 0; m < instance.numAttributes() - 1; m++) {
							Random rand = new Random(1);
							double rnd = rand.nextDouble();
							double newAttrValue = inskey.value(m) + (rnd) * (insvalue.value(m) - inskey.value(m));
							instance.setValue(m, newAttrValue);
						}
						instance.setValue(instance.numAttributes() - 1, 1);
						if (a < k) {
							a++;
							newins.add(instance);

						}
					}
				} else {

					for (int i = 1; i < list.size(); i++) {
						Instance instance = new Instance(ins.numAttributes());
						Instance insvalue = ins.instance(list.get(i));
						for (int m = 0; m < instance.numAttributes() - 1; m++) {
							Random rand = new Random(1);
							double rnd = rand.nextDouble();
							double newAttrValue = inskey.value(m) + (rnd) * (insvalue.value(m) - inskey.value(m));
							instance.setValue(m, newAttrValue);
						}
						instance.setValue(instance.numAttributes() - 1, 1);
						if (a < k) {
							a++;
							newins.add(instance);

						}
					}
				}

			}
		}
		return newins;
	}

	/**
	 * 得每个样本邻居
	 * 
	 * @param r
	 *            邻域半径
	 * 
	 */
	public  void getRNBmap(Instances ins, double w) {
		rnbmap = new LinkedHashMap<Integer, List<Integer>>();
		for (int i = 0; i < ins.numInstances(); i++) {
			if (ins.instance(i).value(ins.numAttributes() - 1) == 1) {
				double r = getRadius(i, ins, w);
				ArrayList<Integer> list = new ArrayList<>();
				list.add(i);
				for (int j = 0; j < ins.numInstances(); j++) {
					if (ins.instance(j).value(ins.numAttributes() - 1) == 1) {
						if (i != j) {
							double tmpdis = getDisByIndex(i, j);
							if (tmpdis < r) {
								list.add(j);
							}
						}
					}
				}
				rnbmap.put(i, list);
			}
		}
	}

	/*
	 * 多线程计算
	 * */
	public void getDisMatrix(Instances ins,int nthred)throws Exception {
		dismap = new LinkedHashMap<String, Double>(ins.numInstances()*ins.numInstances());
		//划分数据
		int size=ins.numInstances()/nthred;
		ArrayList<NThreadMatrix> list=new ArrayList<>();
		ArrayList<Thread> threads=new ArrayList<>();
		for(int i=1;i<=nthred;i++)
		{
			NThreadMatrix nThreadTest=new NThreadMatrix(ins, (i-1)*size, i*size);
			list.add(nThreadTest);
			Thread thread=new Thread(nThreadTest);
			threads.add(thread);
			thread.start();
		}
		for(Thread thread:threads)
		{
			thread.join();//等待执行完成
		}
		//合并
		for(NThreadMatrix nTest:list)
		{
			dismap.putAll(nTest.map);
		}
	}
	/*
	 * 多线程计算
	 * */
	public  void getRNBmap(Instances ins, double w,int nthred)throws Exception {
		rnbmap = new LinkedHashMap<Integer, List<Integer>>(ins.numInstances());
		//划分数据
		int size=ins.numInstances()/nthred;
		//多线程计算
		ArrayList<NThreadRNB> list=new ArrayList<>();
		ArrayList<Thread> threads=new ArrayList<>();
		for(int i=1;i<=nthred;i++)
		{
			NThreadRNB nThreadRNB=new NThreadRNB(ins, (i-1)*size, i*size,dismap,w);
			list.add(nThreadRNB);
			Thread thread=new Thread(nThreadRNB);
			threads.add(thread);
			thread.start();
		}
		for(Thread thread:threads)
		{
			thread.join();//等待执行完成
		}
		//合并数据
		for(NThreadRNB nRnb:list){
			rnbmap.putAll(nRnb.map);
		}
	}
	
	/**
	 * 根据索引获得两个样本间距离
	 * 
	 * @param index1
	 * @param index2
	 * @return
	 */
	private  double getDisByIndex(int index1, int index2) {
		if (dismap.containsKey(index1 + " " + index2)) {
			return dismap.get(index1 + " " + index2);
		} else {
			return dismap.get(index2 + " " + index1);
		}
	}

	/**
	 * 计算距离矩阵，只存放上三角
	 */
	public  void getDisMatrix(Instances ins) {
		dismap = new LinkedHashMap<String, Double>();
		for (int i = 0; i < ins.numInstances() - 1; i++) {
			for (int j = i + 1; j < ins.numInstances(); j++) {
				double dist = CalDistance(ins.instance(i), ins.instance(j));
				dismap.put(i + " " + j, dist);
			}
		}

	}
	
	
	
	private  double getRadius(int i, Instances ins, double w) {
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
	private  double CalDistance(Instance ins1, Instance ins2) {
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
