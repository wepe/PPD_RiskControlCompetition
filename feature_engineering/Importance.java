package feature;


import java.util.Map;

import org.dmlc.xgboost4j.Booster;

public class Importance {
	public static void main(String[] args)throws Exception {
		String in="data/sample.model";
		printImportance(in);
	}
	//输出特征重要性
	public static void printImportance(String path)throws Exception
	{
		Booster booster =new Booster(null, path);
		Map<String, Integer> map= booster.getFeatureScore();
		for(String key:map.keySet())
		{
			System.out.println(key+","+map.get(key));
		}
	}
}
