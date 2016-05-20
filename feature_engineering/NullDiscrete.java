package feature;

import java.io.BufferedReader;
import java.io.PrintWriter;

import util.Util;

public class NullDiscrete {
	public static void main(String[] args) throws Exception{
		String in="d:/temp/ui20.csv";
		String out="d:/temp/ui20_discrete.csv";
		fun(in, out);
	}
	
	public static void fun(String in,String out)throws Exception
	{
		BufferedReader br=Util.readFile(in);
		PrintWriter pt=Util.writeFile(out);
		String line=br.readLine();
		while((line=br.readLine())!=null)
		{
			double discret[]=new double[8];
			String t[]=line.split(",");
			pt.println(t[0]+","+t[2]+","+tans_info2(discret, Double.parseDouble(t[2])));
		}
		pt.close();
	}
	
	public static String trans_null(int a[],int num){
		a[(num/20)>7?7:(num/20)]=1;
		return Util.array2string(a);
	}
	public static String tans_info2(double a[],double num){
		a[(int)(num/2)]=1;
		return Util.array2string(a);
	}
	
}
