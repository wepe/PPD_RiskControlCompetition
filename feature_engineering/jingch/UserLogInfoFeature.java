package com.mj.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mj.feature.UserUpdateInfoFeature.UserInfo;

public class UserLogInfoFeature {

	private static Set<String> info1Set = new HashSet<>();
	private static Set<String> info2Set = new HashSet<>();
	private static Map<String, Integer> info1Map = new HashMap<>();
	private static Map<String, Integer> info2Map = new HashMap<>();
	private static Map<String, Integer> info1CountMap = new HashMap<>();
	private static Map<String, Integer> info2CountMap = new HashMap<>();
	private int totalCounts = 0;

	public UserLogInfoFeature() {
	}

	public void outputFeature(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		Map<String, UserLog> map = new HashMap<String, UserLog>();
		boolean init = false;
		if (info1CountMap.size() != 0)
			init = true;
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			if (!init) {
				put(info1CountMap, splits[2].toLowerCase());
				info1Set.add(splits[2].toLowerCase());
				put(info2CountMap, splits[3].toLowerCase());
				info2Set.add(splits[3].toLowerCase());
			}
			put(info1CountMap, splits[2]);
			put(info2CountMap, splits[3]);
			totalCounts++;
			if (map.containsKey(splits[0]))
				map.get(splits[0]).addInfos(splits[2], splits[3], splits[4]);
			else {
				UserLog ul = new UserLog(splits[1]);
				ul.addInfos(splits[2], splits[3], splits[4]);
				map.put(splits[0], ul);
			}
		}
		br.close();
		List<String> list1 = new ArrayList<String>(info1Set);
		List<String> list2 = new ArrayList<String>(info2Set);
		if (!init) {
			int n = 1;
			for (String info : list1) {
				info1Map.put(info, n);
				n++;
			}
			n = 1;
			for (String info : list2) {
				info2Map.put(info, n);
				n++;
			}
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write("Idx,logTimes,logDays,logDayGap,logGapAVG,mostCountCode,leastCountCode,mostCountCate,leastCountCate,codeEntropy,cateEntropy,logDuration");
		bw.flush();
		for (String info : list1) {
			bw.write(",code" + info);
			bw.flush();
		}
		for (String info : list2) {
			bw.write(",category" + info);
			bw.flush();
		}
		for (String info : list1) {
			bw.write(",codeWeightCount" + info);
			bw.flush();
		}
		for (String info : list2) {
			bw.write(",categoryWeightCount" + info);
			bw.flush();
		}
		bw.write("\r\n");
		bw.flush();
		for (String key : map.keySet()) {
			UserLog ul = map.get(key);
			bw.write(key + "," + ul.getLogCount() + "," + ul.getLogDayCount()
					+ "," + ul.getLogMaxGap() + ","
					+ ul.getLogMaxGap() * 1.0 / ul.getLogDayCount() + ","
					+ ul.getMostInfo1() + "," + ul.getLeastInfo1() + ","
					+ ul.getMostInfo2() + "," + ul.getLeastInfo2() + ","
					+ ul.getInfo1Entropy() + "," + ul.getInfo2Entropy() + ","
					+ ul.getDuration());
			bw.flush();
			for (String info : list1) {
				bw.write("," + ul.getInfo1CodeCount(info + ""));
				bw.flush();
			}
			for (String info : list2) {
				bw.write("," + ul.getInfo2CodeCount(info + ""));
				bw.flush();
			}
			for (String info : list1) {
				bw.write("," + ul.getInfo1CodeWeightCount(info + ""));
				bw.flush();
			}
			for (String info : list2) {
				bw.write("," + ul.getInfo2CodeWeightCount(info + ""));
				bw.flush();
			}
			bw.write("\r\n");
			bw.flush();
		}
		bw.close();
	}

	private void put(Map<String, Integer> infoCountMap, String key) {
		if (infoCountMap.containsKey(key))
			infoCountMap.put(key, infoCountMap.get(key) + 1);
		else
			infoCountMap.put(key, 1);
	}

	class UserLog {
		private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		private String time;
		private List<String> logInfo1;
		private List<String> logInfo2;
		private List<String> logInfo3;

		public UserLog(String time) {
			this.time = time;
			logInfo1 = new ArrayList<String>();
			logInfo2 = new ArrayList<String>();
			logInfo3 = new ArrayList<String>();
		}

		public void addInfos(String info1, String info2, String info3) {
			logInfo1.add(info1);
			logInfo2.add(info2);
			logInfo3.add(info3);
		}

		/**
		 * 鐧诲綍娆℃暟
		 * 
		 * @return
		 */
		public int getLogCount() {
			return logInfo3.size();
		}

		/**
		 * 鐧诲綍澶╂暟
		 * 
		 * @return
		 */
		public int getLogDayCount() {
			Set<String> set = new HashSet<String>(logInfo3);
			return set.size();
		}

		private int timeDiff(String start, String end) throws ParseException {
			Date date1 = df.parse(start);
			Date date2 = df.parse(end);
			long diff = date2.getTime() - date1.getTime();
			int days = (int) (diff / (1000 * 60 * 60 * 24));
			return days;
		}

		/**
		 * 鏈�澶х櫥褰曢棿闅斿ぉ鏁�
		 * 
		 * @return
		 * @throws ParseException
		 */
		public int getLogMaxGap() throws ParseException {
			String startLogDate = null;
			String endLogDate = null;
			int minGap = Integer.MAX_VALUE;
			int maxGap = Integer.MIN_VALUE;
			for (String day : logInfo3) {
				int gap = timeDiff("2011-01-01", day);
				if (gap < minGap) {
					minGap = gap;
					startLogDate = day;
				}
				if (gap > maxGap) {
					maxGap = gap;
					endLogDate = day;
				}
			}
			int dayGap = timeDiff(startLogDate, endLogDate);
			return dayGap;
		}

		public String getMostInfo1() {
			Map<String, Integer> map = new HashMap<>();
			for (String code : logInfo1)
				put(map, code);
			return getMostKeyCount(map);
		}

		public String getLeastInfo1() {
			Map<String, Integer> map = new HashMap<>();
			for (String code : logInfo1)
				put(map, code);
			return getLeastKeyCount(map);
		}

		public String getLeastInfo2() {
			Map<String, Integer> map = new HashMap<>();
			for (String code : logInfo1)
				put(map, code);
			return getLeastKeyCount(map);
		}

		public String getMostInfo2() {
			Map<String, Integer> map = new HashMap<>();
			for (String code : logInfo2)
				put(map, code);
			return getMostKeyCount(map);
		}

		public int getInfo1CodeCount(String code) {
			int counts = 0;
			for (String s : logInfo1)
				if (s.compareTo(code) == 0)
					counts++;
			return counts;
		}

		public double getInfo1CodeWeightCount(String code) {
			try {
				int counts = getInfo1CodeCount(code);
				return counts * 1.0 * info1CountMap.get(code) / totalCounts;
			} catch (Exception e) {
				System.out.println(code);
				throw e;
			}
		}

		public double getInfo2CodeWeightCount(String code) {
			int counts = getInfo2CodeCount(code);
			return counts * 1.0 * info2CountMap.get(code) / totalCounts;
		}

		public double getInfo1Entropy() {
			double result = 0;
			for (String s : logInfo1) {
				double p = getInfo1CodeCount(s) * 1.0 / logInfo1.size();
				result += (-p * Math.log10(p) / Math.log10(2));
			}
			return result;
		}

		public double getInfo2Entropy() {
			double result = 0;
			for (String s : logInfo2) {
				double p = getInfo2CodeCount(s) * 1.0 / logInfo2.size();
				result += (-p * Math.log10(p) / Math.log10(2));
			}
			return result;
		}

		public int getInfo2CodeCount(String code) {
			int counts = 0;
			for (String s : logInfo2)
				if (s.compareTo(code) == 0)
					counts++;
			return counts;
		}

		public int getDuration() throws ParseException {
			int ret = Integer.MAX_VALUE;
			for (String day : logInfo3) {
				int gap = timeDiff(day, time);
				if (gap < ret) {
					ret = gap;
				}
			}
			return ret;
		}
	}

	public String getMostKeyCount(Map<String, Integer> map) {
		int max = Integer.MIN_VALUE;
		String mostKey = "";
		for (String key : map.keySet())
			if (map.get(key) > max) {
				max = map.get(key);
				mostKey = key;
			}
		return mostKey;
	}

	public String getLeastKeyCount(Map<String, Integer> map) {
		int min = Integer.MAX_VALUE;
		String leastKey = "";
		for (String key : map.keySet())
			if (map.get(key) < min) {
				min = map.get(key);
				leastKey = key;
			}
		return leastKey;
	}
}
