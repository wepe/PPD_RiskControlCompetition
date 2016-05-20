package feature;


import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import util.Util;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

public class CombineFeature {
	public static void main(String[] args) throws Exception {
		Instances train = DataSource.read("G:\\比赛\\微额借款用户人品预测\\train.arff");
		train.setClassIndex(train.numAttributes()-1);
		
		String in = "G:\\比赛\\微额借款用户人品预测\\train_x.csv";
//		 String y="G:\\比赛\\微额借款用户人品预测\\train_y.csv";
		String out = "G:\\比赛\\微额借款用户人品预测\\test_x_1300.csv";
		muti_feature_calc(in, out);
//		 fun(in, y);
//		muti_feature(in, out);
//		gen_test();
	}

	// 生成预测集特征
	public static void gen_test() throws Exception {
		BufferedReader br = Util.readFile("G:\\比赛\\微额借款用户人品预测\\test_x_1w.csv");
		String line = "";
		ArrayList<String> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		br = Util.readFile("G:\\比赛\\微额借款用户人品预测\\train_x_comb.csv");
		line = br.readLine();
		PrintWriter pt = Util.writeFile("G:\\比赛\\微额借款用户人品预测\\test_x_comb.csv");
		ArrayList<Integer> ind = new ArrayList<>();
		{
			String temp[] = line.replaceAll("x", "").split(",");
			for (int i = 1; i < temp.length-1; i++) {
				ind.add(Integer.parseInt(temp[i]));
			}
		}
		for (String s : list) {
			String temp[] = s.split(",");
			StringBuffer sb = new StringBuffer(temp[0]);
			for(int index:ind)
			{
				sb.append(","+temp[index]);
			}
			pt.println(sb.toString());
		}
		pt.close();
	}

	// 计算每个特征的degree
	public static void fun(String in, String y) throws Exception {
		BufferedReader br = Util.readFile(y);
		String line = br.readLine();
		double b[] = new double[15000];
		int index = 0;
		while ((line = br.readLine()) != null) {
			double x = Double.parseDouble(line.split(",")[1]);
			b[index] = x;
			index++;
		}
		br = Util.readFile(in);
		line = br.readLine();
		ArrayList<String> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		br.close();
		for (int i = 1; i < 1139; i++) {
			double a[] = new double[15000];
			index = 0;
			for (String s : list) {
				try {
					double x = Double.parseDouble(s.split(",")[i]);
					DecimalFormat df = new DecimalFormat("0.0");
					a[index] = Double.parseDouble(df.format(x));
					index++;
				} catch (Exception e) {
					double x = -1;
					a[index] = x;
					index++;
				}
			}
			System.out.println(degree(a, b));
		}

	}

	// 乘法特征生成
	public static void muti_feature(String in, String out) throws Exception {
		BufferedReader br = Util.readFile(in);
		PrintWriter pt = Util.writeFile(out);
		pt.println(gen_head());// 添加特征名

		String line = br.readLine();
		ArrayList<String> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			list.add(line);
		}

		br = Util.readFile("G:\\比赛\\微额借款用户人品预测\\test_x_comb_feature_score.csv");
		ArrayList<String> comb = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			String temp[] = line.split(",|:| ");
			comb.add(temp[0] + "," + temp[1]);
		}

		br = Util.readFile("G:\\比赛\\微额借款用户人品预测\\train_y.csv");
		br.readLine();
		System.out.println(list.size());
		int count=1;
		for (String s : list) {
			String temp[] = s.split(",");
			StringBuffer sb = new StringBuffer(temp[0]);
			for (String para : comb) {
				int i = Integer.parseInt(para.split(",")[0]);
				int j = Integer.parseInt(para.split(",")[1]);
				try {
					int num=(int) (Math.pow(Double.parseDouble(temp[i]), 2) +Math.pow(Double.parseDouble(temp[j]), 2));
					sb.append("," +  num);
				} catch (Exception e) {
					sb.append("," + -1);
				}
			}
			sb.append("," + br.readLine().split(",")[1]);
			pt.println(sb.toString());
			System.out.println(count++);
		}
		pt.close();
	}

	// 乘法特征计算
	public static void muti_feature_calc(String in, String out) throws Exception {
		BufferedReader br = Util.readFile(in);
		PrintWriter pt = Util.writeFile(out);
		String line = br.readLine();
		ArrayList<String> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			list.add(line);
		}

		br = Util.readFile("G:\\比赛\\微额借款用户人品预测\\features_type.csv");
		// 如果是数值特征，保存下标
		int index = 0;
		ArrayList<Integer> ind = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			if (line.split('"' + "")[3].equals("numeric")) {
				ind.add(index);
			}
			index++;
		}
		// 转成按列存储
		double data[][] = new double[15000][1139];
		int row = 0;
		for (String s : list) {
			String temp[] = s.split(",");
			for (int i : ind) {
				try {
					data[row][i] = Double.parseDouble(temp[i]);
				} catch (Exception e) {
					data[row][i] = 0;
				}
			}
			row++;
		}

		Instances train = DataSource.read("G:\\比赛\\微额借款用户人品预测\\train.arff");
		train.setClassIndex(train.numAttributes()-1);
		System.out.println(train.numClasses());
		// 数值特征两两相乘,计算a[]
		double a[] = new double[15000];
		index = 0;
		HashMap<String, Double> map=new HashMap<>();
		for (int i = 0; i < ind.size(); i++) {
			for (int j = i + 1; j < ind.size(); j++) {
				for (int k = 0; k < 15000; k++) {
					double y=Math.pow(data[k][ind.get(i)], 2)+Math.pow(data[k][ind.get(j)],2);
					a[index]=y;
					index++;
				}
				index = 0;
				System.out.println(train.numAttributes());
				Instances temp=addfeature(train, a);
				System.out.println(temp.numAttributes());
				temp.setClassIndex(temp.numAttributes()-1);
				System.out.println(temp.classIndex());
				System.out.println(temp.numClasses());
				double infogain = UseInfoGain.computeInfoGain(temp, temp.attribute(0));
				map.put(ind.get(i) + "," + ind.get(j), infogain);
			}
		}

		LinkedHashMap<String, Double> newmap=new LinkedHashMap<>();
		newmap=sortMap(map);
		Iterator<Entry<String, Double>> iterator=newmap.entrySet().iterator();
		while (iterator.hasNext()) { 
			Map.Entry entry = (Map.Entry) iterator.next(); 
			Object key = entry.getKey(); 
			Object val = entry.getValue(); 
			System.out.println(key+":"+val);
			} 
		// for(String s:list)
		// {
		// String temp[]=s.split(",");
		// StringBuffer sb=new StringBuffer();
		// for(int i=0;i<ind.size();i++)
		// for(int j=i+1;j<ind.size();j++)
		// {
		// try {
		// sb.append(","+Double.parseDouble(temp[ind.get(i)])*Double.parseDouble(temp[ind.get(j)]));
		// } catch (Exception e) {
		// sb.append(","+-1);
		// }
		// }
		// pt.println(sb.substring(1));
		// }
		br.close();
		pt.close();
	}

	public static LinkedHashMap<String, Double> sortMap(HashMap<String, Double> oldMap) {
		ArrayList<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(oldMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if ((o2.getValue() - o1.getValue()) > 0)
					return 1;
				else if ((o2.getValue() - o1.getValue()) == 0)
					return 0;
				else
					return -1;
			}

		});
		// 将排序好的放入新的map
		LinkedHashMap<String, Double> newMap = new LinkedHashMap<String, Double>();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;
	}
	
	public static Instances addfeature(Instances data,double a[])throws Exception
	{
		Add filter = new Add();
		String options[] = new String[4];
		options[0] = "-N";
		options[1] = "f_new";
		options[2] = "-C";
		options[3] = "first";
		filter.setOptions(options);
		filter.setInputFormat(data);
		data = Filter.useFilter(data, filter);
		for(int i=0;i<a.length;i++)
		{
			data.instance(i).setValue(data.numAttributes()-1, a[i]);
		}
		return data;
	}
	
	// 计算特征纯度
	public static double degree(double a[], double b[]) {
		int size=20;
		ArrayList<Double> aa=new ArrayList<>();
		for(double d:a)
		{
			aa.add(d);
		}
		double max=Collections.max(aa);
		double min=Collections.min(aa);
		double para=(max-min)/size;
		for(int i=0;i<a.length;i++)
		{
			a[i]=(int)(aa.get(i)/para);
		}
		HashMap<Double, ArrayList<Double>> map = new HashMap<>();
		for (int i = 0; i < a.length; i++) {
			if (map.containsKey(a[i])) {
				map.get(a[i]).add(b[i]);
			} else {
				ArrayList<Double> list = new ArrayList<>();
				list.add(b[i]);
				map.put(a[i], list);
			}
		}
		double degree = 0;
		for (ArrayList<Double> list : map.values()) {
			double n = 0;
			for (double k : list) {
				n += k;
			}
			double p = n / list.size();
			double en = 2 * p * (1 - p);
			degree += en * list.size() / a.length;
		}

		return 1 / degree;
	}

	// 生成特征名
	public static String gen_head() {
		StringBuffer sb = new StringBuffer("uid");
		for (int i = 1; i < 1301; i++) {
			sb.append(",x" + i);
		}
		sb.append(",label");
		return sb.toString();
	}
}

