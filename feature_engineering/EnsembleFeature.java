package feature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class EnsembleFeature {
	public static void main(String[] args) throws Exception{
		String in="D:/temp/trans/numerical_feature.csv";
		String out="D:/temp/trans/muti.csv";
//		ensembleFeature(in, out);
		splitFeatureFile(out);
	}
	public static void ensembleFeature(String inputFile, String outputFile)
			throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		String line = br.readLine();
		ArrayList<Integer> thirdPartIndex = new ArrayList<>();
		String splits[] = line.split(",");
		for (int i = 0; i < splits.length; i++)
			if (splits[i].contains("ThirdParty_Info"))
				thirdPartIndex.add(i);
		bw.write("Idx");
		bw.flush();
		for (int i = 0; i < thirdPartIndex.size(); i++)
			for (int j = i + 1; j < thirdPartIndex.size(); j++) {
				bw.write(",muti" + i + j);
				bw.flush();
			}
		bw.write("\r\n");
		bw.flush();
		int n = 0;
		while ((line = br.readLine()) != null) {
			splits = line.split(",");
			bw.write(splits[0]);
			bw.flush();
			n++;
			for (int i = 0; i < thirdPartIndex.size(); i++) {
				float x = -1;
				if (splits[thirdPartIndex.get(i)].trim().length() != 0) {
					x = Float.parseFloat(splits[thirdPartIndex.get(i)].trim());
				}
				for (int j = i + 1; j < thirdPartIndex.size(); j++) {
					float y = -1;
					if (splits[thirdPartIndex.get(j)].trim().length() != 0) {
						y = Float.parseFloat(splits[thirdPartIndex.get(j)]
								.trim());
					}
					bw.write("," + muti(x, y));
					bw.flush();
				}
			}
			bw.write("\r\n");
			bw.flush();
			System.out.println(n + " lines solved split length = "
					+ splits.length);
		}
		br.close();
		bw.close();
	}

	private static float rate(float x, float y) {
		if (y <= 0)
			return -1;
		else
			return x / y;
	}

	private static float muti(float x, float y) {
		if (y <= 0)
			return -1;
		else
			return (float) Math.log(x * y);
	}
	
	public static void splitFeatureFile(String inputFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		BufferedWriter bws[] = new BufferedWriter[8];
		for (int i = 1; i <= 7; i++)
			bws[i] = new BufferedWriter(new FileWriter(
					"D:/temp/trans/muti_" + i + ".csv"));
		String line = br.readLine();
		String splits[] = line.split(",");
		for (int i = 1; i <= 7; i++) {
			bws[i].write("Idx");
			bws[i].flush();
		}
		for (int i = 1; i < splits.length; i++) {
			bws[((i - 1) % 7) + 1].write("," + splits[i]);
			bws[((i - 1) % 7) + 1].flush();
		}
		for (int i = 1; i <= 7; i++) {
			bws[i].write("\r\n");
			bws[i].flush();
		}
		int n = 0;
		while ((line = br.readLine()) != null) {
			splits = line.split(",");
			n++;
			for (int i = 1; i <= 7; i++) {
				bws[i].write(splits[0]);
				bws[i].flush();
			}
			for (int i = 1; i < splits.length; i++) {
				bws[((i - 1) % 7) + 1].write("," + splits[i]);
				bws[((i - 1) % 7) + 1].flush();
			}
			for (int i = 1; i <= 7; i++) {
				bws[i].write("\r\n");
				bws[i].flush();
			}
			System.out.println(n + " lines solved");
		}
		br.close();
		for (int i = 1; i <= 7; i++) {
			bws[i].close();
		}
	}

}
