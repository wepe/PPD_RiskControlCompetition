package util;


public class Score{
	public String id;
	public double score;
	public int rank;
	public int label;
	public Score(String id, double score, int label) {
		super();
		this.id = id;
		this.score = score;
		this.label = label;
	}
	
	public Score(String id, double score) {
		super();
		this.id = id;
		this.score = score;
	}
	public String toLine()
	{
		return id+","+score;
	}
	@Override
	public String toString() {
		return "Score [id=" + id + ", score=" + score + ", rank=" + rank + ", label=" + label + "]";
	}
	
	
}
