package feature;

import java.io.BufferedReader;
import java.io.PrintWriter;

import util.Util;

public class FeatureTransform {
	public static void main(String[] args)throws Exception {
		String in="d:/test_170.csv";
		String out="d:/trans/test_trans";
		for(int i=0;i<8;i++){
			transform(in, out+i+".csv",i);
			System.out.println(i);
		}
	}
	public static void transform(String in,String out,int trans)throws Exception{
		PrintWriter pt=Util.writeFile(out);
		BufferedReader br=Util.readFile(in);
		String line=br.readLine();
		pt.println(line);
		while((line=br.readLine())!=null)
		{
			pt.println(transform(line, trans,0));
		}
		br.close();
		pt.close();
	}
	public static String transform(String s,int trans,int idx){
		String t[]=s.split(",");
		double tt[]=new double[t.length];
		switch (trans) {
		case 0:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=isNaN(sigmod(x))?-1:sigmod(x);
			}
			break;
		case 1:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=isNaN(tanh(x))?(x>0?1:-1):tanh(x);
			}
			break;
		case 2:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=isNaN(log(x))?-1:log(x);
			}
			break;
		case 3:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=isNaN(sinh(x))?-1:sinh(x);
			}
			break;
		case 4:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=isNaN(cosh(x))?-1:cosh(x);
			}
			break;
		case 5:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=(isNaN(coth(x))?-1:coth(x))==Double.MAX_VALUE?100:isNaN(coth(x))?-1:coth(x);
			}
			break;
		case 6:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=isNaN(sech(x))?-1:sech(x);
			}
			break;
		case 7:
			for(int i=idx;i<t.length-1;i++)
			{
				double x=Double.parseDouble(t[i]);
				tt[i]=(isNaN(csch(x))?-1:csch(x))==Double.MAX_VALUE?100:isNaN(csch(x))?-1:csch(x);
			}
			break;
		default:
			break;
		}
		try {
			tt[t.length-1]=Double.parseDouble(t[t.length-1]);
			
		} catch (Exception e) {
			tt[t.length-1]=-1;
		}
		return Util.array2string(tt).replace("Infinity", "100");
	}
	
	
	public static double sigmod(double x) //0~1
	{
		return 1/(1+Math.exp(-x));
	}
	public static double tanh(double x){ //-1~1
		return (Math.exp(x)-Math.exp(-x))/(Math.exp(x)+Math.exp(-x));
	}
	public static double log(double x){ //
		return Math.log(x+1);
	}
	public static double sinh(double x){
		return (Math.exp(x)-Math.exp(-x))/2;
	}
	public static double cosh(double x){
		return (Math.exp(x)+Math.exp(-x))/2;
	}
	public static double coth(double x){
		return 1/(tanh(x)+0.01);
	}
	public static double sech(double x){
		return 1/(cosh(x)+0.01);
	}
	public static double csch(double x){
		return 1/(sinh(x)+0.01);
	}
	public static boolean isNaN(double v) {
        return (v != v);
    }
}
