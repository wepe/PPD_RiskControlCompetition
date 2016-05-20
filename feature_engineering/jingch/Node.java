package com.mj.feature;

public class Node {
	private String uid;
	private int rank;
	private Double value;
	private Double score;

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Node(String uid, Double value) {
		this.uid = uid;
		this.value = value;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getUid() {
		return uid;
	}

	public Double getValue() {
		return value;
	}

}
