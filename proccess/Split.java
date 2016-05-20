package proccess;

import java.io.PrintWriter;
import java.util.ArrayList;

import model.SampleCV;
import sample.MY0;
import util.Util;
import weka.core.Instances;

public class Split {
	public Instances data;
	public Instances samples;
	public Split(String in)throws Exception{
		data=Util.getInstances(in);
	}
	public static void main(String[] args) throws Exception{
		Split split=new Split("D:/ty/8file_train.csv");
		split.sample();
	}
	
	//����
	public void sample()throws Exception{
		ArrayList<Instances>  list=new ArrayList<>();
		for(int i=0;i<8;i++)
		{
			Instances ins=new Instances(data);
			ins.delete();
			list.add(ins);
		}
		for(int i=0;i<data.numInstances();i++)
		{
			int index=i%8;
			list.get(index).add(data.instance(i));
		}
		System.err.println("split complete!");
		
		for(int i=0;i<8;i++)
		{
			System.out.println(list.get(i).numInstances());
			Util.saveIns(list.get(i), "data/data"+i+".csv");
			System.err.println("sample " +i+" complete!");
		}
	}
}
