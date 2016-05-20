package proccess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import weka.core.Instances;

public class NThreadRNB implements Runnable{
	public Instances data;
	public int start;
	public int end;
	/** dismap存放两个实例之间的距离，矩阵的上三角 */
	private  LinkedHashMap<String, Double> dismap;
	/** map存放实例邻域半径内的邻居 */
	public LinkedHashMap<Integer, List<Integer>> map;
	double w;
	public NThreadRNB(Instances data,int start,int end,LinkedHashMap<String, Double> dismap,double w){
		this.data=data;
		this.start=start;
		this.end=end;
		this.dismap=dismap;
		this.w=w;
		map=new LinkedHashMap<>(end-start);
	}
	@Override
	public void run() {
		for(int i=start;i<end;i++){
			if (data.instance(i).value(data.numAttributes() - 1) == 1) {
				double r = getRadius(i, data, w);
				ArrayList<Integer> list = new ArrayList<>();
				list.add(i);
				for (int j = 0; j < data.numInstances(); j++) {
					if (data.instance(j).value(data.numAttributes() - 1) == 1) {
						if (i != j) {
							double tmpdis = getDisByIndex(i, j);
							if (tmpdis < r) {
								list.add(j);
							}
						}
					}
				}
				map.put(i, list);
			}
		}
	}
	/**
	 * 根据索引获得两个样本间距离
	 * 
	 * @param index1
	 * @param index2
	 * @return
	 */
	private  double getDisByIndex(int index1, int index2) {
		if (dismap.containsKey(index1 + " " + index2)) {
			return dismap.get(index1 + " " + index2);
		} else {
			return dismap.get(index2 + " " + index1);
		}
	}
	private  double getRadius(int i, Instances ins, double w) {
		double r = 0;
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for (int j = 0; j < ins.numInstances(); j++) {
			if (i != j) {
				double tmpdis = getDisByIndex(i, j);
				if (tmpdis > max) {
					max = tmpdis;
				}
				if (tmpdis < min) {
					min = tmpdis;
				}
			}
		}
		r = min + w * (max - min);
		return r;
	}
}
