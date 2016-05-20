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

public class UserUpdateInfoFeature {
	private static Set<String> infoSet;
	private static Map<String, Integer> infoCountMap = new HashMap<>();
	private static Map<String, Integer> infoMap = new HashMap<>();
	private int totalCounts = 0;

	public UserUpdateInfoFeature() {
		infoSet = new HashSet<>();
	}

	private void put(Map<String, Integer> infoCountMap, String key) {
		if (infoCountMap.containsKey(key))
			infoCountMap.put(key, infoCountMap.get(key) + 1);
		else
			infoCountMap.put(key, 1);
	}

	public void outputFeature(String inputFile, String outputFile,
			String mapFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		String line = br.readLine();
		Map<String, UserInfo> map = new HashMap<String, UserInfo>();
		boolean init = false;
		if (infoCountMap.size() != 0)
			init = true;
		while ((line = br.readLine()) != null) {
			String splits[] = line.split(",");
			if (!init) {
				put(infoCountMap, splits[2].toLowerCase());
				infoSet.add(splits[2].toLowerCase());
			}
			totalCounts++;
			if (map.containsKey(splits[0]))
				map.get(splits[0]).addInfos(splits[2].toLowerCase(), splits[3]);
			else {
				UserInfo ui = new UserInfo(splits[1]);
				ui.addInfos(splits[2].toLowerCase(), splits[3]);
				map.put(splits[0], ui);
			}
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write("Idx,updateTimes,updateDays,updateDayGap,updateGapAVG,mostCountInfo,leastCountInfo,infoEntropy,infoUpdateDuration");
		bw.flush();
		List<String> list = new ArrayList<String>(infoSet);
		if (!init) {
			int n = 1;
			for (String info : list) {
				infoMap.put(info, n);
				n++;
			}
		}
		for (String info : list) {
			bw.write("," + info);
			bw.flush();
		}
		for (String info : list) {
			bw.write(",weight" + info);
			bw.flush();
		}
		BufferedWriter infoKeyValue = new BufferedWriter(
				new FileWriter(mapFile));
		for (String key : infoMap.keySet()) {
			infoKeyValue.write(key + ":" + infoMap.get(key) + "\r\n");
			infoKeyValue.flush();
		}
		infoKeyValue.close();
		bw.write("\r\n");
		bw.flush();
		for (String key : map.keySet()) {
			UserInfo ui = map.get(key);
			bw.write(key + "," + ui.getUpdateCount() + ","
					+ ui.getUpdateDayCount() + "," + ui.getLogMaxGap() + ","
					+ ui.getLogMaxGap() * 1.0 / ui.getUpdateDayCount() + ","
					+ infoMap.get(ui.getMostInfo()) + ","
					+ infoMap.get(ui.getLeastInfo()) + ","
					+ ui.getInfoEntropy() + "," + ui.getDuration());
			bw.flush();
			for (String info : list) {
				bw.write("," + ui.getInfoCount(info));
				bw.flush();
			}
			for (String info : list) {
				bw.write("," + ui.getInfoWeightCount(info));
				bw.flush();
			}
			bw.write("\r\n");
			bw.flush();
		}
		bw.close();
	}

	class UserInfo {
		private String time;
		private List<String> updateInfo;
		private List<String> logDate;
		private DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

		public UserInfo(String time) {
			this.time = time;
			updateInfo = new ArrayList<>();
			logDate = new ArrayList<>();
		}

		public void addInfos(String info, String date) {
			updateInfo.add(info);
			logDate.add(date);
		}

		/**
		 * 淇敼娆℃暟
		 * 
		 * @return
		 */
		public int getUpdateCount() {
			return logDate.size();
		}

		/**
		 * 鐧诲綍澶╂暟
		 * 
		 * @return
		 */
		public int getUpdateDayCount() {
			Set<String> set = new HashSet<String>(logDate);
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
			for (String day : logDate) {
				int gap = timeDiff("2011/01/01", day);
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

		public String getMostInfo() {
			Map<String, Integer> map = new HashMap<>();
			for (String code : updateInfo)
				put(map, code);
			return getMostKeyCount(map);
		}

		public String getLeastInfo() {
			Map<String, Integer> map = new HashMap<>();
			for (String code : updateInfo)
				put(map, code);
			return getLeastKeyCount(map);
		}

		public int getInfoCount(String code) {
			int counts = 0;
			for (String s : updateInfo)
				if (s.compareTo(code) == 0)
					counts++;
			return counts;
		}

		public double getInfoWeightCount(String code) {
			try {
				int counts = getInfoCount(code);
				return counts * 1.0 * infoCountMap.get(code) / totalCounts;
			} catch (Exception e) {
				System.out.println(code);
				throw e;
			}
		}

		public double getInfoEntropy() {
			double result = 0;
			for (String s : updateInfo) {
				double p = getInfoCount(s) * 1.0 / updateInfo.size();
				result += (-p * Math.log10(p) / Math.log10(2));
			}
			return result;
		}

		public int getDuration() throws ParseException {
			int ret = Integer.MAX_VALUE;
			for (String day : logDate) {
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
