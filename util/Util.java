package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.dmlc.xgboost4j.Booster;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

public class Util {
	public static BufferedReader readFile(String src) throws Exception {
		// return new BufferedReader(new FileReader(new File(src)));
		return new BufferedReader(new InputStreamReader(new FileInputStream(new File(src)), "UTF-8"));
	}

	// 合并数据
	public static Instances addAll(Instances ins1, Instances ins2) {
		for (int i = 0; i < ins2.numInstances(); i++) {
			ins1.add(ins2.instance(i));
		}
		return ins1;
	}

	public static PrintWriter writeFile(String dst) throws Exception {
		return new PrintWriter(new FileWriter(dst));
	}

	public static boolean saveArrayList(ArrayList<String> list, String dst) throws Exception {
		PrintWriter pt = writeFile(dst);
		int i = 0;
		while (i < list.size()) {
			pt.println(list.get(i));
			i++;
		}
		pt.close();
		return true;
	}

	public static ArrayList<String> readToArrayList(String src) throws Exception {
		BufferedReader br = readFile(src);
		ArrayList<String> list = new ArrayList<>();
		String line = "";
		while ((line = br.readLine()) != null) {
			list.add(line.trim());
		}
		br.close();
		return list;
	}

	public static HashMap<String, Integer> readToHashMap(String src) throws Exception {
		BufferedReader br = readFile(src);
		HashMap<String, Integer> map = new HashMap<>();
		String line = "";
		while ((line = br.readLine()) != null) {
			map.put(line, 0);
		}
		br.close();
		return map;
	}

	// 返回数据的实例
	public static Instances getInstances(String in) throws Exception {
		// DataSource source = new DataSource(in);
		// Instances ins = source.getDataSet();
		// return ins;
		return DataSource.read(in);
	}

	// 删除标签属性
	public static Instances deleteClass(Instances ins) throws Exception {
		Remove filter = new Remove();
		String options[] = new String[2];
		options[0] = "-R";
		options[1] = "last";
		filter.setOptions(options);
		filter.setInputFormat(ins);
		return Filter.useFilter(ins, filter);
	}

	// 将标签列离散化
	public static Instances discLabel(Instances ins) throws Exception {
		NumericToNominal filter = new NumericToNominal();
		String option[] = new String[2];
		option[0] = "-R";
		option[1] = "last";
		filter.setOptions(option);
		filter.setInputFormat(ins);
		ins = Filter.useFilter(ins, filter);
		ins.setClassIndex(ins.numAttributes() - 1);
		return ins;
	}

	// 数组归一化
	public static double[] normalize(double data[]) {
		double max = 0;
		double min = data[0];
		for (double d : data) {
			if (max < d) {
				max = d;
			}
			if (min > d) {
				min = d;
			}
		}
		double grap = max - min;
		double result[] = new double[data.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (data[i] - min) / grap;
		}
		return result;
	}

	public static ArrayList<Double> normalize(ArrayList<Double> data) {
		double max = 0;
		double min = data.get(0);
		for (double d : data) {
			if (max < d) {
				max = d;
			}
			if (min > d) {
				min = d;
			}
		}
		double grap = max - min;
		ArrayList<Double> result = new ArrayList<>();
		for (int i = 0; i < data.size(); i++) {
			result.add((data.get(i) - min) / grap);
		}
		return result;
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	// 保存Instances
	public static void saveIns(Instances data, String out) throws Exception {
		DataSink.write(out, data);
	}

	// 选择特征
	public static void feature_select(String dataPath, String outPath, String modelPath, int threshold)
			throws Exception {
		Booster booster = new Booster(null, modelPath);
		Map<String, Integer> map = booster.getFeatureScore();
		ArrayList<Integer> list = new ArrayList<>(map.size());
		for (String key : map.keySet()) {
			if (map.get(key) < threshold) {
				list.add(Integer.parseInt(key.replace("f", "")));
			}
		}
		list.trimToSize();
		Instances data = Util.getInstances(dataPath);
		for (int i : list) {
			data.deleteAttributeAt(i);
		}
		Util.saveIns(data, outPath);
	}

	public static String array2string(int a[]) {
		StringBuffer sb = new StringBuffer(a[0] + "");
		for (int i = 1; i < a.length; i++) {
			sb.append("," + a[i]);
		}
		return sb.toString();
	}

	public static String array2string(double a[]) {
		StringBuffer sb = new StringBuffer(a[0] + "");
		for (int i = 1; i < a.length; i++) {
			sb.append("," + a[i]);
		}
		return sb.toString();
	}
	
	public static void onehot(String in, String out) throws Exception {
		BufferedReader br = Util.readFile(in);
		PrintWriter pt = Util.writeFile(out);
		String line = br.readLine();
		int size = line.split(",").length;
		ArrayList<String> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			list.add(line);
		}
		HashMap<String, StringBuffer> map = new HashMap<>();
		for (int i = 1; i < size; i++) {
			HashSet<String> set = new HashSet<>();
			for (String l : list) {
				set.add(l.split(",")[i]);
			}
			System.out.println(set.size());
			HashMap<String, Integer> index = new HashMap<>();// 映射索引
			int ind = 0;
			for (String a : set) {
				index.put(a, ind);
				ind++;
			}
			for (String l : list) {
				int a[] = new int[set.size()];
				String key = l.split(",")[0];

				a[index.get(l.split(",")[i])] = 1;
				if (map.containsKey(key)) {
					map.get(key).append("," + array2string(a));
				} else {
					StringBuffer sb = new StringBuffer();
					sb.append("," + array2string(a));
					map.put(key, sb);
				}
			}
		}

		for (String key : map.keySet()) {
			pt.println(key + map.get(key));
		}
		pt.close();
	}

	// 合并数据,默认第一列为id
	public static void combine(String f1, String f2, String out) throws Exception {
		BufferedReader br1 = readFile(f1);
		BufferedReader br2 = readFile(f2);
		PrintWriter pt = writeFile("data/temp.csv");
		String line = br1.readLine();
		int size1=line.split(",").length;
		int size2= br2.readLine().split(",").length;
		StringBuffer head=new StringBuffer();
		for(int i=0;i<size1+size2;i++)
		{
			head.append(",f"+i);
		}
		pt.println(head.substring(1));
		HashMap<String, StringBuffer> map1 = new HashMap<>();
		HashMap<String, String> map2 = new HashMap<>();
		while ((line = br1.readLine()) != null) {
			StringBuffer sb = new StringBuffer(line);
			map1.put(line.split(",")[0], sb);
		}
		while ((line = br2.readLine()) != null) {
			map2.put(line.split(",")[0], line);
		}
		int k=0;
		for (String key : map1.keySet()) {
			pt.println(map1.get(key).append(","+map2.get(key)));
			if(k%10==0)
			{
				System.out.println(100*k/map1.size()+"%");
			}
			k++;
		}
		pt.close();
		Instances data = getInstances("data/temp.csv");
		Instances t = getInstances(f1);
		data.deleteAttributeAt(t.numAttributes());
		data.deleteAttributeAt(0);
		saveIns(data, out);
	}

	public static void main(String[] args) throws Exception {
		combine("d:/temp/train_new_id.csv", "G:\\比赛\\魔镜杯风控算法大赛\\data\\复赛\\listInfo\\listinfo_onehot.csv", "d:/temp/f_900.csv");
	}
}
