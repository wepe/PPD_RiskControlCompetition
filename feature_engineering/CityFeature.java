package feature;

import java.io.BufferedReader;
import java.io.PrintWriter;

import util.Util;

public class CityFeature {
	public static void main(String[] args)throws Exception {
		fun("d:/temp/city/city.csv", "d:/temp/city/city_diff.csv");
//		System.out.println("锦州市".contains("广州"));
	}
	public static void fun(String in,String out)throws Exception{
		BufferedReader br=Util.readFile(in);
		String line=br.readLine();
		PrintWriter pt=Util.writeFile(out);
		while((line=br.readLine())!=null){
			int a[]=new int[6];
			String t[]=line.split(",");
			if (t[1].equals(t[2])) {
				a[0]=1;
			}
			if (t[1].equals(t[4])) {
				a[1]=1;
			}
			if (t[6].contains(t[1])) {
				System.out.println(t[6]+","+t[1]);
				a[2]=1;
			}
			if (t[2].equals(t[4])) {
				a[3]=1;
			}
			if (t[6].contains(t[2])) {
				a[4]=1;
			}
			if (t[6].contains(t[4])) {
				a[5]=1;
			}
			pt.println(t[0]+","+Util.array2string(a));
		}
		pt.close();
	}
}
