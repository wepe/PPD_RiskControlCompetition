package com.mj.feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeTool {
	public MergeTool() {
	}

	public void getPredict(String idxFile, String scoreFile, String output)
			throws Exception {
		BufferedReader br1 = new BufferedReader(new FileReader(idxFile));
		BufferedReader br2 = new BufferedReader(new FileReader(scoreFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		bw.write("Idx,score\r\n");
		bw.flush();
		String line1 = br1.readLine();
		String headNames[] = line1.split(",");
		int n = 0;
		for (; n < headNames.length; n++)
			if (headNames[n].compareTo("target") == 0)
				break;
		String line2 = null;
		while ((line1 = br1.readLine()) != null) {
			if (Float.parseFloat(line1.split(",")[n]) < 0) {
				line2 = br2.readLine();
				bw.write(line1.split(",")[0] + "," + line2.trim() + "\r\n");
				bw.flush();
			}
		}
		br1.close();
		br2.close();
		bw.close();
	}

	private Map<String, Double> getScoreMap(String fileName) throws Exception {
		Map<String, Double> map1 = new HashMap<>();
		BufferedReader br1 = new BufferedReader(new FileReader(fileName));
		String line = br1.readLine();
		while ((line = br1.readLine()) != null) {
			String splts[] = line.split(",");
			map1.put(splts[0], Double.parseDouble(splts[1]));
		}
		br1.close();
		return map1;
	}

	public void mergeAVG() throws Exception {
		Map<String, Double> map1 = getScoreMap("E:/mojing/results/main/cv7822.csv");
		Map<String, Double> map2 = getScoreMap("E:/mojing/results/main/file8_1439_feature_sample.csv");
		Map<String, Double> map3 = getScoreMap("E:/mojing/results/main/model_6_graphlab_xgboost_04_17_23_49_32.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"E:/mojing/results/main/score_avg.csv"));
		bw.write("Idx,score\r\n");
		bw.flush();
		for (String key : map1.keySet()) {
			bw.write(key + ","
					+ (map1.get(key) + map2.get(key) + map3.get(key)) / 3
					+ "\r\n");
			bw.flush();
		}
		bw.close();
	}

	public void mergeRank() throws Exception {
		Map<String, Node> nodeMap1 = getMap("E:/mojing/results/main/cv7791_777.csv");
		Map<String, Node> nodeMap2 = getMap("E:/mojing/results/main/score_avg.csv");
		Map<String, Node> nodeMap3 = getMap("E:/mojing/results/main/muti_xgb_svm_7806.csv");
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(
				"E:/mojing/results/main/rank_avg.csv"));
		bw1.write("Idx,score\r\n");
		bw1.flush();
		Map<String, Double> mergeMap = new HashMap<>();
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (String uid : nodeMap1.keySet()) {
			double score = 0.1 * nodeMap1.get(uid).getScore() + 0.7
					* nodeMap2.get(uid).getScore() + 0.2
					* nodeMap3.get(uid).getScore();
			if (score > max)
				max = score;
			if (score < min)
				min = score;
			mergeMap.put(uid, score);
		}
		System.out.println(max);
		System.out.println(min);
		BufferedReader br = new BufferedReader(new FileReader(
				"E:/mojing/results/main/muti_xgb_svm_7806.csv"));
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] splits = line.split(",");
			bw1.write(splits[0] + "," + (mergeMap.get(splits[0]) - min)
					/ (max - min) + "\r\n");
			bw1.flush();
		}
		br.close();
		bw1.close();
	}

	private Map<String, Node> getMap(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		List<Node> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			String[] splits = line.split(",");
			Node node = new Node(splits[0], Double.parseDouble(splits[1]));
			list.add(node);
		}
		br.close();
		list.sort(new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		Map<String, Node> nodeMap = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setRank(i + 1);
			list.get(i).setScore((i + 1 - 1) * 1.0 / (list.size() - 1));
			nodeMap.put(list.get(i).getUid(), list.get(i));
		}
		return nodeMap;
	}

	private Map<String, Node> getMap_reverse(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		List<Node> list = new ArrayList<>();
		while ((line = br.readLine()) != null) {
			String[] splits = line.split(",");
			Node node = new Node(splits[0], Double.parseDouble(splits[1]));
			list.add(node);
		}
		br.close();
		list.sort(new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		Map<String, Node> nodeMap = new HashMap<>();
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setRank(i + 1);
			list.get(i).setScore(1.0 / (i + 1));
			nodeMap.put(list.get(i).getUid(), list.get(i));
		}
		return nodeMap;
	}
}
