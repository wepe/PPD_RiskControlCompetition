package feature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import org.dmlc.xgboost4j.Booster;

import util.Util;
import weka.core.Instances;

public class FeatureSelect {
	public static void main(String[] args)throws Exception {
		String dataPath="G:\\比赛\\魔镜杯风控算法大赛\\data\\复赛\\train_all.csv";
		String outPath="G:\\比赛\\魔镜杯风控算法大赛\\data\\复赛\\feaselect_train.csv";
		String modelPath="data/sample.model";
		int threshold=100;
		feature_select(dataPath, outPath, modelPath, threshold);
	}
	//选择特征
	public static void feature_select(String  dataPath,String outPath,String modelPath,int threshold)throws Exception
	{
		Booster booster =new Booster(null, modelPath);
		Map<String, Integer> map= booster.getFeatureScore();
		ArrayList<Integer> list=new ArrayList<>(map.size());
		for(String key:map.keySet())
		{
			if (map.get(key)<threshold) {
				list.add(Integer.parseInt(key.replace("f", "")));
			}
		}
		list.trimToSize();
		Instances data=Util.getInstances(dataPath);
		list.sort(new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o2-o1;
			}
			
		});
		for(int i:list)
		{
			data.deleteAttributeAt(i);
		}
		Util.saveIns(data, outPath);
	}
}
