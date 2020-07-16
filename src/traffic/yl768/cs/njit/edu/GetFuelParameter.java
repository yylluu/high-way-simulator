package traffic.yl768.cs.njit.edu;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;


public class GetFuelParameter {
	
	
	
	
	public static void main (String[] args){
		Map<String, Double> v_a_fuel = getFuelParameter();
		
		double v = 21.54353;
		double acc = 2.261123;
		double acc1 = acc;
		if(acc1 < -4){
			acc1 = -4;
		} else if (acc1 > 2) {
			acc1 = 2;
		}
		if(v < 0){
			v = 0;
		} else if (v > 30) {
			v = 30;
		}
		
		ArrayList<Double> status = new ArrayList<Double>();
		status.add(Math.floor(v * 2) / 2d);
		status.add(Math.floor(acc1 * 20) / 20d);

		System.out.print(status.toString());
		System.out.print("" + v_a_fuel.get(status.toString()));
	}
	
	
	static Map<String, Double> getFuelParameter() {
		Map<String, Double> v_a_fuel = new HashMap<String, Double>();
		String line;
		BufferedReader br;
		ArrayList<Double> v_a = new ArrayList<Double>();
		v_a.add(0.0);
		v_a.add(0.0);
		double fuel;
		try {
		    br = new BufferedReader(new InputStreamReader(new FileInputStream("v_a_fuel"), Charset.forName("UTF-8")));
		    while ((line = br.readLine()) != null) {
		        // Deal with the line
		    	String delims = "\t";
		    	String[] tokens = line.split(delims);
		    	v_a.clear();
		    	v_a.add(0, Double.parseDouble(tokens[0]));
		    	v_a.add(1, Double.parseDouble(tokens[1]));
		    	fuel = Double.parseDouble(tokens[2]);
		    	v_a_fuel.put(v_a.toString(), fuel);
		    }
		} catch(Exception e){
			System.err.println("Error when reading v_a_fuel");
			e.printStackTrace();
		}
		return v_a_fuel;
	}
}
