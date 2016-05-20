package proccess;

import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;

public class NThreadMatrix implements Runnable{
	public HashMap<String, Double> map;
	public Instances data;
	public int start;
	public int end;
	public NThreadMatrix(Instances data,int start,int end){
		this.data=data;
		this.start=start;
		this.end=end;
		map=new HashMap<>((end-start)*data.numInstances());
	}
	@Override
	public void run() {
		for(int i=start;i<end;i++)
		{
			for(int j=0;j<data.numInstances();j++){
				double dist = CalDistance(data.instance(i), data.instance(j));
				map.put(i + " " + j, dist);
			}
		}
	}
	/**
	 * 计算两个样本点的距离
	 */
	private  double CalDistance(Instance ins1, Instance ins2) {
		double dis = 0;
		for (int j = 0; j < ins1.numAttributes() - 1; j++) {
			if (ins1.isMissing(j) || ins2.isMissing(j)) {
				dis = dis + 1;
			} else if ((!ins1.attribute(j).isNumeric()) && (!ins2.attribute(j).isNumeric())) {
				if (ins1.value(j) != ins2.value(j)) {
					dis = dis + 1;
				}
			} else if (ins1.attribute(j).isNumeric() && ins2.attribute(j).isNumeric()) {
				double t = Math.abs(ins1.value(j) - ins2.value(j));
				dis = dis + t * t;
			}
		}
		dis = Math.sqrt(dis);
		return dis;
	}
}
