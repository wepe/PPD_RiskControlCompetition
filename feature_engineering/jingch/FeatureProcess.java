package com.mj.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureProcess {
	public FeatureProcess() {
	}

	public void splitFeatureFile(String inputFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bws[] = new BufferedWriter[8];
		for (int i = 1; i <= 7; i++)
			bws[i] = new BufferedWriter(new FileWriter(
					"E:/mojing/third_part_rate_" + i + ".csv"));
		String line = br.readLine();
		String splits[] = line.split(",");
		for (int i = 1; i <= 7; i++) {
			bws[i].write("Idx");
			bws[i].flush();
		}
		for (int i = 1; i < splits.length; i++) {
			bws[((i - 1) % 7) + 1].write("," + splits[i]);
			bws[((i - 1) % 7) + 1].flush();
		}
		for (int i = 1; i <= 7; i++) {
			bws[i].write("\r\n");
			bws[i].flush();
		}
		int n = 0;
		while ((line = br.readLine()) != null) {
			splits = line.split(",");
			n++;
			for (int i = 1; i <= 7; i++) {
				bws[i].write(splits[0]);
				bws[i].flush();
			}
			for (int i = 1; i < splits.length; i++) {
				bws[((i - 1) % 7) + 1].write("," + splits[i]);
				bws[((i - 1) % 7) + 1].flush();
			}
			for (int i = 1; i <= 7; i++) {
				bws[i].write("\r\n");
				bws[i].flush();
			}
			System.out.println(n + " lines solved");
		}
		br.close();
		for (int i = 1; i <= 7; i++) {
			bws[i].close();
		}
	}

	public void ensembleFeature(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		String line = br.readLine();
		List<Integer> thirdPartIndex = new ArrayList<>();
		String splits[] = line.split(",");
		for (int i = 0; i < splits.length; i++)
			if (splits[i].contains("ThirdParty_Info"))
				thirdPartIndex.add(i);
		bw.write("Idx");
		bw.flush();
		for (int i = 0; i < thirdPartIndex.size(); i++)
			for (int j = i + 1; j < thirdPartIndex.size(); j++) {
				bw.write(",rate" + i + j);
				bw.flush();
			}
		bw.write("\r\n");
		bw.flush();
		int n = 0;
		while ((line = br.readLine()) != null) {
			splits = line.split(",");
			bw.write(splits[0]);
			bw.flush();
			n++;
			for (int i = 0; i < thirdPartIndex.size(); i++) {
				float x = -1;
				if (splits[thirdPartIndex.get(i)].trim().length() != 0) {
					x = Float.parseFloat(splits[thirdPartIndex.get(i)].trim());
				}
				for (int j = i + 1; j < thirdPartIndex.size(); j++) {
					float y = -1;
					if (splits[thirdPartIndex.get(j)].trim().length() != 0) {
						y = Float.parseFloat(splits[thirdPartIndex.get(j)]
								.trim());
					}
					bw.write("," + rate(x, y));
					bw.flush();
				}
			}
			bw.write("\r\n");
			bw.flush();
			System.out.println(n + " lines solved split length = "
					+ splits.length);
		}
		br.close();
		bw.close();
	}

	private float rate(float x, float y) {
		if (y <= 0)
			return -1;
		else
			return x / y;
	}

	public void dataViewer(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			Calendar c = Calendar.getInstance();
			Long ms = Long.parseLong(splits[0]) * 24 * 3600 * 1000;
			Date d = new Date(ms);
			c.setTime(d);
			bw.write(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
					+ "-" + c.get(Calendar.DATE) + "," + line + "\r\n");
			bw.flush();
		}
		br.close();
		bw.close();
	}

	public void ListingInfoEncode(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		String line = br.readLine();
		int n = 1;
		Map<Integer, Integer[]> countMap = new HashMap<>();
		Map<String, Integer> codeMap = new HashMap<>();
		Map<String, Integer> idxMap = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			if (!codeMap.containsKey(splits[1])) {
				codeMap.put(splits[1], n);
				n++;
			}
			idxMap.put(splits[0], codeMap.get(splits[1]));
			if (!countMap.containsKey(codeMap.get(splits[1]))) {
				Integer counts[] = new Integer[2];
				counts[0] = 0;
				counts[1] = 0;
				countMap.put(codeMap.get(splits[1]), counts);
			}
			if (Integer.parseInt(splits[2]) < 0)
				continue;
			Integer counts[] = countMap.get(codeMap.get(splits[1]));
			counts[Integer.parseInt(splits[2])] += 1;
			countMap.put(codeMap.get(splits[1]), counts);
		}
		br.close();
		bw.write("Idx");
		bw.flush();
		for (int i = 1; i <= n; i++) {
			bw.write(",block" + i);
			bw.flush();
		}
		bw.write(",block_bad_rate\r\n");
		bw.flush();
		for (String Idx : idxMap.keySet()) {
			int b = idxMap.get(Idx);
			bw.write(Idx);
			bw.flush();
			for (int i = 1; i <= n; i++) {
				if (b == i) {
					bw.write(",1");
					bw.flush();
				} else {
					bw.write(",0");
					bw.flush();
				}
			}
			Integer counts[] = countMap.get(b);
			bw.write("," + counts[1] * 1.0 / (counts[0] + counts[1]) + "\r\n");
			bw.flush();
		}
		bw.close();
	}

	public void badRate(String inputFile, String outputFile, int delta,
			String dataFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		String splits[] = line.split(",");
		int listIndex = 0;
		int targetIdx = 0;
		for (int i = 0; i < splits.length; i++)
			if (splits[i].compareTo("ListingInfo") == 0)
				listIndex = i;
			else if (splits[i].compareTo("target") == 0)
				targetIdx = i;
		Map<Integer, Integer[]> countMap = new HashMap<>();
		while ((line = br.readLine()) != null) {
			splits = line.split(",");
			if (!countMap.containsKey(Integer.parseInt(splits[listIndex]))) {
				Integer counts[] = new Integer[2];
				counts[0] = 0;
				counts[1] = 0;
				countMap.put(Integer.parseInt(splits[listIndex]), counts);
			}
			if (Float.parseFloat(splits[targetIdx]) < 0)
				continue;
			Integer counts[] = countMap
					.get(Integer.parseInt(splits[listIndex]));
			counts[(int) Float.parseFloat(splits[targetIdx])] += 1;
			countMap.put(Integer.parseInt(splits[listIndex]), counts);
		}
		br.close();
		List<DataNode> list = new ArrayList<>();
		for (Integer key : countMap.keySet())
			list.add(new DataNode(key, countMap.get(key)[1],
					countMap.get(key)[0]));
		list.sort(new Comparator<DataNode>() {
			@Override
			public int compare(DataNode o1, DataNode o2) {
				return o1.getDay().compareTo(o2.getDay());
			}
		});
		Map<Integer, Float> fMap = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			int pos = 0;
			int neg = 0;
			int sign = 1;
			for (int j = 0; j < delta; j++) {
				sign *= -1;
				int tmp = i + sign * j;
				if (tmp < 0 || tmp >= list.size())
					continue;
				pos += list.get(tmp).getPos();
				neg += list.get(tmp).getNeg();
			}
			fMap.put(list.get(i).getDay(), (float) (pos * 1.0 / (pos + neg)));
		}
		br = new BufferedReader(new FileReader(dataFile));
		line = br.readLine();
		splits = line.split(",");
		for (int i = 0; i < splits.length; i++)
			if (splits[i].compareTo("ListingInfo") == 0)
				listIndex = i;
			else if (splits[i].compareTo("target") == 0)
				targetIdx = i;
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write("Idx,bad_rate_" + delta + "\r\n");
		bw.flush();
		while ((line = br.readLine()) != null) {
			splits = line.split(",");
			bw.write(splits[0] + ","
					+ fMap.get(Integer.parseInt(splits[listIndex])) + "\r\n");
			bw.flush();
		}
		br.close();
		bw.close();
	}

	public class DataNode {
		private Integer day;
		private int pos_count;
		private int neg_count;

		public DataNode(Integer d, int p, int n) {
			day = d;
			pos_count = p;
			neg_count = n;
		}

		public int getPos() {
			return pos_count;
		}

		public int getNeg() {
			return neg_count;
		}

		public Integer getDay() {
			return day;
		}
	}

	public void numericalFeatureProcess(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy");
		Map<String, Float> map1 = new HashMap<>();
		Map<String, Float> map0 = new HashMap<>();
		Map<String, Integer> encodeMap = new HashMap<>();
		int n = 1;
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			int label = (int) Float.parseFloat(splits[splits.length - 2]);
			Date date = null;
			if (label >= 0)
				date = df.parse(splits[splits.length - 1]);
			else
				date = df1.parse(splits[splits.length - 1]);
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			String key = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH);
			if (!encodeMap.containsKey(key)) {
				encodeMap.put(key, n);
				n++;
			}
			if (!map1.containsKey(key))
				map1.put(key, 0.0f);
			if (!map0.containsKey(key))
				map0.put(key, 0.0f);
			if (label == 1) {
				map1.put(key, map1.get(key) + 1);
			} else if (label == 0) {
				map0.put(key, map0.get(key) + 1);
			}
		}
		for (String key : map1.keySet())
			System.out.println(key + " : " + map1.get(key)
					/ (map1.get(key) + map0.get(key)));
		int timeFeature[] = new int[encodeMap.size() + 1];
		for (int i = 0; i < timeFeature.length; i++)
			timeFeature[i] = 0;
		br.close();
		br = new BufferedReader(new FileReader(inputFile));
		line = br.readLine();
		bw.write(line + ",week_day,month,bad_rate");
		bw.flush();
		for (int i = 1; i <= encodeMap.size(); i++) {
			for (String key : encodeMap.keySet())
				if (encodeMap.get(key) == i) {
					bw.write("," + key);
					bw.flush();
				}

		}
		bw.write("\r\n");
		bw.flush();
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			int label = (int) Float.parseFloat(splits[splits.length - 2]);
			Date date = null;
			if (label >= 0)
				date = df.parse(splits[splits.length - 1]);
			else
				date = df1.parse(splits[splits.length - 1]);
			bw.write(splits[0]);
			bw.flush();
			long s = date.getTime();
			int days = (int) (s / (1000 * 60 * 60 * 24));
			for (int i = 1; i < splits.length - 1; i++) {
				bw.write("," + splits[i]);
				bw.flush();
			}
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			String key = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH);
			double bad_rate = -1;
			if (map1.containsKey(key))
				bad_rate = map1.get(key) / (map1.get(key) + map0.get(key));
			bw.write("," + days + "," + c.get(Calendar.DAY_OF_WEEK) + ","
					+ c.get(Calendar.MONTH) + "," + bad_rate);
			bw.flush();
			timeFeature[encodeMap.get(key)] = 1;
			for (int i = 1; i < timeFeature.length; i++) {
				bw.write("," + timeFeature[i]);
				bw.flush();
			}
			bw.write("\r\n");
			bw.flush();
			for (int i = 0; i < timeFeature.length; i++)
				timeFeature[i] = 0;
		}
		br.close();
		bw.close();
	}

	public void oneHotEncode(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		int featureValueMax[] = new int[38];
		for (int i = 0; i < featureValueMax.length; i++)
			featureValueMax[i] = 0;
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			for (int i = 1; i <= 37; i++)
				if (Integer.parseInt(splits[i]) > featureValueMax[i])
					featureValueMax[i] = Integer.parseInt(splits[i]);
		}
		int features[][] = new int[38][];
		for (int i = 0; i <= 37; i++)
			features[i] = new int[featureValueMax[i] + 1];
		for (int i = 0; i < features.length; i++)
			for (int j = 0; j < features[i].length; j++)
				features[i][j] = 0;
		br.close();
		br = new BufferedReader(new FileReader(inputFile));
		line = br.readLine();
		String names[] = line.split(",");
		for (int i = 1; i <= 37; i++)
			System.out.println(names[i] + " : " + features[i].length);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write(names[0]);
		bw.flush();
		for (int i = 1; i <= 37; i++)
			if (features[i].length > 100)
				continue;
			else
				for (int j = 1; j < features[i].length; j++) {
					bw.write("," + names[i] + "_" + j);
					bw.flush();
				}
		bw.write("\r\n");
		bw.flush();
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			for (int i = 1; i <= 37; i++)
				features[i][Integer.parseInt(splits[i])] = 1;
			bw.write(splits[0]);
			bw.flush();
			for (int i = 1; i <= 37; i++)
				if (features[i].length > 100)
					continue;
				else
					for (int j = 1; j < features[i].length; j++) {
						bw.write("," + features[i][j]);
						bw.flush();
					}
			bw.write("\r\n");
			bw.flush();
			for (int i = 0; i < features.length; i++)
				for (int j = 0; j < features[i].length; j++)
					features[i][j] = 0;
		}
		br.close();
		bw.close();
	}

	public void encodeFeatureValue(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		Map<Integer, Map<String, Integer>> map = new HashMap<Integer, Map<String, Integer>>();
		int mapValues[] = new int[38];
		for (int i = 0; i < mapValues.length; i++)
			mapValues[i] = 1;
		for (int i = 1; i <= 37; i++)
			map.put(i, new HashMap<>());
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			for (int i = 1; i <= 37; i++) {
				Map<String, Integer> columnMap = map.get(i);
				if (columnMap.containsKey(splits[i].trim()))
					continue;
				columnMap.put(splits[i].trim(), mapValues[i]);
				mapValues[i] += 1;
				map.put(i, columnMap);
			}
		}
		br.close();
		for (int i = 1; i <= 37; i++) {
			Map<String, Integer> columnMap = map.get(i);
			for (String s : columnMap.keySet())
				System.out.print(s + ",");
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}
		br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		line = br.readLine();
		bw.write(line + "\r\n");
		bw.flush();
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			bw.write(splits[0]);
			bw.flush();
			for (int i = 1; i <= 37; i++) {
				bw.write("," + map.get(i).get(splits[i].trim()));
				bw.flush();
			}
			bw.write("\r\n");
			bw.flush();
		}
		br.close();
		bw.close();
	}
}
