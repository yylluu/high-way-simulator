package main.traffic.yl768.cs.njit.edu;

import traffic.yl768.cs.njit.edu.Scenario;


public class NonGUI {
	
	private static Scenario scen;
	
	public static void main(String[] args) {
		double lambda, appPer, desireSpeed=0;
		if(args.length==3){
			try {
				lambda = Double.parseDouble(args[0]);
				appPer = Double.parseDouble(args[1]);
				desireSpeed = Double.parseDouble(args[2]);
			} catch (Exception e) {
				lambda = 0.6666667;
				appPer = 0.0;
				desireSpeed = 30;
			}
		}else {
			try {
				lambda = Double.parseDouble(args[0]);
				appPer = Double.parseDouble(args[1]);
			} catch (Exception e) {
				lambda = 0.6666667;
				appPer = 1.0;
				//desireSpeed = 50;
			}
		}
		//System.out.println(""+lambda+" "+appPer);
		scen = new Scenario();
		scen.setAppPercentage(appPer);
		scen.setLambda(lambda);
		if(desireSpeed!=0){
			scen.setDesireSpeed(desireSpeed);
		}
		scen.heatingTime=27000;
		scen.initiate();
		new Thread (new ScenarioThread()).start();
	}
	
	private static void showData() {
	     System.out.print(scen.timestep + "\t");
	     System.out.print(scen.averagePassingTime + "\t");
	     //System.out.print(scen.totalFuelConsumption + "\t");
	     System.out.print(scen.averageFuelConsumption + "\t");
	     System.out.print(scen.totalLaneChange + "\t");
	     System.out.print(scen.averageWaitingTime + "\t");
	     System.out.print(scen.averageStopCount + "\t");
	     System.out.print(scen.totalPassedCars + "\n");

	}
	
	private static void showCongestion(){
		System.out.println("position speed");
		Double[] v_pos;
		for (int pos = scen.ROAD_LENGTH[0] + 50; pos <= scen.distance + 50; pos += 5){
			v_pos = scen.v_pos.get(pos);
			System.out.print(pos  + "\t");
			System.out.print(v_pos[0]  + "\n");
		}

	}
	
	
	static class ScenarioThread implements Runnable{
		public void run() {
		    System.out.println("timestep averagePassingTime averageFuelConsumption totalLaneChange averageWaitingTime averageStopCount totalPassedCars ");
			while(scen.timestep < 523000 ) {
				if(scen.timestep%2700==0 && scen.timestep > scen.heatingTime) {
					showData();
				}
				scen.nextScenario1();
			}
			System.out.println("Total uploading count: " + scen.totalUploadingCount);
			showCongestion();
		}
	}
	

}
