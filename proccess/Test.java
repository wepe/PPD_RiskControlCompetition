package proccess;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import util.Util;

public class Test {
	public static void main(String[] args) throws Exception{
		fun10();
	}
	
	//SQL创建训练集表
	public static void fun1()
	{
		String head="用户标识,终端首次使用日期,最后使用时期,终端品牌,型号,是否智能,是否支持gps,是否支持摄像,前置摄像,双卡,屏幕像素,cpu类型,cpu频率Hz,屏幕大小,内存大小,操作系统类型,操作系统版本,上市时间,上市价格,当前价格,是否支持NFC,是否lte,手机类型";
		String feature[]=head.split(",");
		String type=" varchar(20)";
		StringBuffer sb=new StringBuffer();
		for(String f:feature)
		{
			sb.append(","+f+type);
//			sb.append(",log(1+"+f+")");
		}
		System.out.println(sb.substring(1));
	}
	//把测试集的label填充为-1
	public static void fun2()throws Exception
	{
		BufferedReader br=Util.readFile("D:\\temp\\PPD_Master_GBK_2_Test_Set.csv");
		PrintWriter pt=Util.writeFile("D:\\temp\\test.csv");
		String line="";
		while((line=br.readLine())!=null)
		{
			String features[]=line.split(",");
			StringBuffer sb=new StringBuffer();
			for(int i=0;i<features.length-1;i++)
			{
				sb.append(","+features[i]);
			}
			sb.append(",-1");
			sb.append(","+datareverse(features[features.length-1]));
			pt.println(sb.substring(1));
		}
		pt.close();
	}
	//日期反转
	public static String datareverse(String date)
	{
		String temp[]=date.split("/");
		StringBuffer sb=new StringBuffer();
		for(int i=temp.length-1;i>=0;i--)
		{
			sb.append("/"+temp[i]);
		}
		return sb.substring(1);
	}
	//拼接SQL语句，有哪些操作以及次数的向量
	public static void fun3()throws Exception
	{
		BufferedReader br=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\log_feature\\LogInfo2.csv");
		String line="";
		while((line=br.readLine())!=null)
		{
			System.out.println(",CASE WHEN LogInfo2='"+line+"' THEN num ELSE 0 END AS LogInfo2_"+line);
		}
	}
	//拼接SQL语句，更新次数最多的项目
	public static void fun4()throws Exception
	{
		BufferedReader br=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\log_feature\\max_upd_items.csv");
		String line="";
		while((line=br.readLine())!=null)
		{
			System.out.println(",CASE WHEN UserupdateInfo1='"+line+"' THEN 1 ELSE 0 END AS is"+line+"_max_upd_item");
		}
	}
	//拼接SQL语句,选取子表所有属性
	public static void fun5()
	{
		for(int i=1;i<=28;i++)
		{
			System.out.print(",a"+i+".*");
		}
	}
	//中国城市人口密度排名
	public static void fun6()throws Exception
	{
		BufferedReader br=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\中国各城市人口密度排名.txt");
		PrintWriter pt=Util.writeFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\城市人口密度.csv");
		PrintWriter pt2=Util.writeFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\省份人口密度.csv");
		String line=br.readLine();
		String t[]=line.split(" ");
		HashMap<String, String> map=new HashMap<>();
		HashMap<String, ArrayList<Double>> city=new HashMap<>();
		for(int i=0;i<t.length;i++)
		{
			i++;
			map.put(t[i++], t[i+1]);
			i++;
		}
		for(int i=0;i<t.length;i++)
		{
			i++;
			i++;
			if (city.containsKey(t[i])) {
				city.get(t[i]).add(Double.parseDouble(t[i+1]));
			}else {
				ArrayList<Double> list=new ArrayList<>();
				list.add(Double.parseDouble(t[i+1]));
				city.put(t[i], list);
			}
//			System.out.println(t[i]+","+ t[i+1]);
			i++;
		}
		System.out.println(city.size());
		for(String key:city.keySet())
		{
			ArrayList<Double> list=city.get(key);
			double sum=0;
			for(double d:list)
			{
				sum+=d;
			}
			pt2.println(key+","+sum/list.size());
			System.out.println(key+","+sum/list.size());
		}
		pt2.close();
		for(String key:map.keySet())
		{
			pt.println(key+","+map.get(key));
//			System.out.println(key+":"+map.get(key));
		}
		pt.close();
	}
	//城市人口排名
	public static void fun7()throws Exception
	{
		BufferedReader br=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\城市人口排名.csv");
		PrintWriter pt=Util.writeFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\城市人口.csv");
		PrintWriter pt2=Util.writeFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\省份平均人口.csv");
		String line="";
		HashMap<String, String> city=new HashMap<>();
		HashMap<String, ArrayList<Double>> shen=new HashMap<>();
		while((line=br.readLine())!=null)
		{
			String t[]=line.split(" ");
			String key=t[1].replace("辖区", "").replace("市", "");
//			System.out.println(t[1].replace("辖区", "")+","+t[4]);
			city.put(key, t[4]);
			String sheng=t[2];
			if (shen.containsKey(sheng)) {
				shen.get(sheng).add(Double.parseDouble(t[4]));
			}else {
				ArrayList<Double> list=new ArrayList<>();
				list.add(Double.parseDouble(t[4]));
				shen.put(sheng, list);
			}
		}
		for(String key:city.keySet())
		{
			pt.println(key+","+city.get(key));
		}
		for(String key:shen.keySet())
		{
			ArrayList<Double> list=shen.get(key);
			double sum=0;
			for(double d:list)
			{
				sum+=d;
			}
			pt2.println(key+","+sum/list.size());
			System.out.println(key+","+sum/list.size());
		}
		pt.close();
		pt2.close();
	}
	//生成城市人口密度特征，城市人口特征
	public static void fun8()throws Exception
	{
		BufferedReader train=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\PPD-First-Round-Data-Update\\Training Set\\PPD_Training_Master_GBK_3_1_Training_Set.csv");
		BufferedReader test=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\PPD-First-Round-Data-Update\\Test Set\\PPD_Master_GBK_2_Test_Set.csv");
		String line=train.readLine();
		BufferedReader density=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\城市人口密度.csv");
		BufferedReader human_num=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\城市人口.csv");
		BufferedReader density_sheng=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\省份人口密度.csv");
		HashMap<String, String> density_sheng_map=new HashMap<>();
		while((line=density_sheng.readLine())!=null)
		{
			String t[]=line.split(",");
			density_sheng_map.put(t[0].replace("省", ""), t[1]);
//			System.out.println(t[0].replace("省", "")+","+t[1]);
		}
		BufferedReader human_num_sheng=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\省份平均人口.csv");
		HashMap<String, String> human_num_sheng_map=new HashMap<>();
		while((line=human_num_sheng.readLine())!=null)
		{
			String t[]=line.split(",");
			human_num_sheng_map.put(t[0].replace("省", ""), t[1]);
			System.out.println(t[0].replace("省", "")+","+t[1]);
		}
		HashMap<String, String> density_map=new HashMap<>();
		while((line=density.readLine())!=null)
		{
			String t[]=line.split(",");
			density_map.put(t[0], t[1]);
		}
		HashMap<String, String> human_num_map=new HashMap<>();
		while((line=human_num.readLine())!=null)
		{
			String t[]=line.split(",");
			human_num_map.put(t[0], t[1]);
		}
		HashMap<String, String> city2sheng=fun9();
		PrintWriter pt1=Util.writeFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\bryan_city_feature.csv");
//		PrintWriter pt2=Util.writeFile("");
		while((line=train.readLine())!=null)
		{
			String t[]=line.split(",");
			String Idx=t[0];
			String city1=t[2];
			String city2=t[4];
			String d11=(density_map.get(city1)!=null?density_map.get(city1):density_sheng_map.get(city2sheng.get(city1)));
			String d12=human_num_map.get(city1)!=null?human_num_map.get(city1):human_num_sheng_map.get(city2sheng.get(city1));
			String d21=density_map.get(city2)!=null?density_map.get(city2):density_sheng_map.get(city2sheng.get(city2));
			String d22=human_num_map.get(city2)!=null ? human_num_map.get(city2):human_num_sheng_map.get(city2sheng.get(city2));
			pt1.println(Idx+","+d11+","+d12+","+d21+","+d22);
			
//			pt1.println(Idx+","+(density_map.get(city1)!=null?density_map.get(city1):density_sheng_map.get(city2sheng.get(city1)))+","
//			+human_num_map.get(city1)!=null?human_num_map.get(city1):human_num_sheng_map.get(city2sheng.get(city1))+","
//			+density_map.get(city2)!=null?density_map.get(city2):density_sheng_map.get(city2sheng.get(city2))+","
//			+human_num_map.get(city2)!=null ? human_num_map.get(city2):human_num_sheng_map.get(city2sheng.get(city2)));
//			System.out.println(Idx+","+density_map.get(city1)!=null?density_map.get(city1):density_sheng_map.get(city2sheng.get(city1))+","
//					+human_num_map.get(city1)!=null?human_num_map.get(city1):human_num_sheng_map.get(city2sheng.get(city1))+","
//							+density_map.get(city2)!=null?density_map.get(city2):density_sheng_map.get(city2sheng.get(city2))+","
//							+human_num_map.get(city2)!=null ? human_num_map.get(city2):human_num_sheng_map.get(city2sheng.get(city2)));
		}
		pt1.close();
	}
	
	//生成城市对应的省map
	public static HashMap<String, String> fun9()throws Exception
	{
		HashMap<String,String> map=new HashMap<>();
		BufferedReader br=Util.readFile("G:\\比赛\\魔镜杯风控算法大赛\\data\\城市人口排名.csv");
		String line="";
		while((line=br.readLine())!=null)
		{
			String t[]=line.split(" ");
			String city=t[1].replace("辖区", "").replace("市", "");
			String sheng=t[2].replace("省", "").replace("市", "");
			System.out.println(city+","+sheng);
		}
		return map;
	}
	//生成特征名
	public static void fun10(){
		StringBuffer sb=new StringBuffer("Idx");
		for(int i=0;i<75;i++)
		{
			sb.append(",f"+i);
		}
		System.out.println(sb.toString());
	}
}
