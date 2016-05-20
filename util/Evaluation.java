package util;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.dmlc.xgboost4j.DMatrix;

public class Evaluation {
	public static void main(String[] args)throws Exception {
		String test="G:\\比赛\\微额借款用户人品预测\\test_res.csv";
//		String test="G:\\比赛\\微额借款用户人品预测\\result_1228.csv";
		String answer="G:\\比赛\\微额借款用户人品预测\\off_answer.csv";
//		String answer="G:\\比赛\\微额借款用户人品预测\\guess_y.csv";
		eval(test, answer);
		System.out.println( auc(test, answer));
	}

	// test数据格式为 uid,score
	// answer数据格式为uid,label
	public static void eval(String test,String answer)throws Exception
	{
		BufferedReader br=Util.readFile(test);
		BufferedReader br2=Util.readFile(answer);
		HashMap<String, String> map_ans_0=new HashMap<>();//存答案0
		HashMap<String, String> map_ans_1=new HashMap<>();//存答案1
		HashMap<String, String> map_test=new HashMap<>();//存测试数据
		String line="";
		//先读入answer数据
		while((line=br2.readLine())!=null)
		{
			 String temp[]=line.split(",");
			 if (temp[1].equals("0")) {
				 map_ans_0.put(temp[0],temp[1]);
			}else {
				map_ans_1.put(temp[0],temp[1]);
			}
		}
		//读入test数据
		while((line=br.readLine())!=null)
		{
			 String temp[]=line.split(",");
			 map_test.put(temp[0],temp[1]);
		}
		//组成样本对
		ArrayList<String> list=new ArrayList<>();
		for(String key0:map_ans_0.keySet())
			for(String  key1:map_ans_1.keySet())
			{
				line=key0+","+key1;
				list.add(line);
			}
		//计算得分
		double score=0;
		for(String s:list)
		{
			String temp[]=s.split(",");
			String neg=temp[0];
			String pos=temp[1];
			double p_score=Double.parseDouble(map_test.get(pos));
			double n_score=Double.parseDouble(map_test.get(neg));
			if (p_score>n_score) {
				score+=1;
			}else if (p_score==n_score) {
				score+=0.5;
			}
		}
		System.out.println(score/list.size());
	}
	
	
	//初始化得分
	// test数据格式为 uid,score
	// answer数据格式为uid,label
	public static ArrayList<Score> init(String test,String answer)throws Exception
	{
		ArrayList<Score> list=new ArrayList<>();
		BufferedReader br=Util.readFile(test);
		BufferedReader br2=Util.readFile(answer);
		HashMap<String, Double> t_m=new HashMap<>();
		HashMap<String, Integer> a_m=new HashMap<>();
		String line=br.readLine();
		while((line=br.readLine())!=null)
		{
			String temp[]=line.split(",");
			String id=temp[0];
			double score=Double.parseDouble(temp[1]);
			t_m.put(id, score);
		}
		while((line=br2.readLine())!=null)
		{
			String temp[]=line.split(",");
			String id=temp[0];
			int label=Integer.parseInt(temp[1]);
			a_m.put(id, label);
		}
		for(String key:t_m.keySet())
		{
			Score score=new Score(key, t_m.get(key), a_m.get(key));
			list.add(score);
		}
		list.sort(new Comparator<Score>() {
			@Override
			public int compare(Score o1, Score o2) {
				return new Double(o1.score).compareTo(new Double(o2.score));
			}
		});
		int i=1;
		for(Score score:list)
		{
			score.rank=i++;
		}
//		for(Score score:list)
//		{
//			System.out.println(score);
//		}
		return list;
	}
	
	//计算auc
	public static double auc(String test,String answer)throws Exception
	{
		ArrayList<Score> list=init(test, answer);
		int arg[]=arg(list);
		double sum=arg[0];
		double n0=arg[1];
		double n1=arg[2];
		return (sum-n1*(n1+1)/2)/n0/n1;
	}
	public static double auc(ArrayList<Score> list)throws Exception
	{
		list.sort(new Comparator<Score>() {
			@Override
			public int compare(Score o1, Score o2) {
				return new Double(o1.score).compareTo(new Double(o2.score));
			}
		});
		int i=1;
		for(Score score:list)
		{
			score.rank=i++;
		}
		int arg[]=arg(list);
		double sum=arg[0];
		double n0=arg[1];
		double n1=arg[2];
		return (sum-n1*(n1+1)/2)/n0/n1;
	}
	//计算arg sum,n0,n1
	public static int[] arg(ArrayList<Score> list)
	{
		int sum=0;
		int n1=0;
		int arg[]=new int[3];
		for(Score score:list)
		{
			if (score.label==1) {
				sum+=score.rank;
				n1++;
			}
		}
		arg[0]=sum;
		arg[1]=list.size()-n1; //n0
		arg[2]=n1;//n1
		return arg;
	}
	public static double auc(float[][] predicts, DMatrix dmat)throws Exception
	{
		float labels[]=dmat.getLabel();
		ArrayList<Score> list=new ArrayList<>();
		for(int i=0;i<predicts.length;i++)
		{
			Score score=new Score("0", predicts[i][0],(int)labels[i]);
			list.add(score);
		}
		
		return auc(list);
	}
}
