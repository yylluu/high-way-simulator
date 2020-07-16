package traffic.yl768.cs.njit.edu;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

public class Scenario extends Frame {
	
	public static final int SCREEN_WIDTH = 1200;
	public static final int SCREEN_HIGHT = 600;
	public static int distance;
	
	private double appPercentage = 1;
	private double lambda = 0.6666666666667;
	private double desireSpeed = 0;

	//public static final int[] ROAD_LENGTH = {950, 1380, 980, 860, 2600, 615, 920, 1050, 575, 750};
	public static final int[] ROAD_LENGTH = {2000, 950, 1380, 980, 860, 2600, 615, 920, 1050, 575, 750}; //First 1000 used for buffering
	
	public static int limit = 23; //22.35 meter/second = 50 mile/hour
	
	public int totalLaneChange = 0;
	public int averagePassingTime = 0;
	public long totalPassingTime = 0;
	public int totalPassedCars = 0;
	public double totalFuelConsumption = 0;
	public double averageFuelConsumption = 0;
	public int totalWaitingTime = 0;
	public double averageWaitingTime = 0;
	public int totalStopCount = 0;
	public double averageStopCount = 0;
	public int timestep = 0;
	public int heatingTime = 95000;
	public int totalUploadingCount = 0;
	
	Vehicle myVehicle;
	Lane myLane;
	Light myLight;
	Light prevLight = null;
	
	Image offScreenImage = null;
	
	private double dt = 0.1;
	
	List<Node> grid = new ArrayList<Node>();
	List<Lane> lanes = new ArrayList<Lane>();
	List<Light> lights = new ArrayList<Light>();
	
	public static Map<String, Double> v_a_fuel;
	public static Map<Integer, Double[]> v_pos = new HashMap<Integer, Double[]>();

	public static void main (String[] args){
		Scenario scen = new Scenario();
		scen.initiate();
		scen.lauchFrame();
	}
	
	public void setAppPercentage(double percentage){
		this.appPercentage = percentage;
	}
	
	public void setDesireSpeed(double desireSpeed){
		this.desireSpeed = desireSpeed;
	}
	
	
	public void setLambda(double lambda){
		this.lambda = lambda;
	}
	
	public void initiate(){
		
		distance = 0;
		for (int i = 0; i < ROAD_LENGTH.length;i++){
			distance += ROAD_LENGTH[i];
		}
		
		Lane left = new Lane(0, 250, dt, limit, distance, ROAD_LENGTH, heatingTime, lambda, v_a_fuel);
		left.setLights(lights); //left.seed = 11111;
		if(desireSpeed!=0) left.setDesireSpeed(desireSpeed);
		left.appPercentage = appPercentage;
		lanes.add(left);
		Lane middle = new Lane(0, 261, dt, limit, distance, ROAD_LENGTH, heatingTime, lambda, v_a_fuel);
		middle.setLights(lights); //middle.seed = 22222;
		if(desireSpeed!=0) middle.setDesireSpeed(desireSpeed);
		middle.appPercentage = appPercentage;
		lanes.add(middle); 
		Lane right = new Lane(0, 272, dt, limit, distance, ROAD_LENGTH, heatingTime, lambda, v_a_fuel);
		right.setLights(lights); //right.seed = 33333;
		if(desireSpeed!=0) right.setDesireSpeed(desireSpeed);
		right.appPercentage = appPercentage;
		lanes.add(right);
		
		left.setNextLanes(null, middle);
		middle.setNextLanes(left, right);
		right.setNextLanes(middle, null);
		
		distance = 0;
		
		for (int i = 0; i < ROAD_LENGTH.length;i++){
			distance += ROAD_LENGTH[i];
			myLight = new Light(distance, 239, (ArrayList<Lane>)lanes);
			myLight.prevLight = prevLight;
			lights.add(myLight);
			prevLight = myLight;
		}
		prevLight = null;
		
		v_a_fuel = GetFuelParameter.getFuelParameter();
		
		for (int pos = ROAD_LENGTH[0] + 50; pos <= distance + 50; pos += 5){
			Double[] zeros = {0d, 0d};
			v_pos.put(pos, zeros);
		}
		
	}
	
	
	
	public void nextTimeStep(){
	
		timestep++;
		this.totalLaneChange = 0;
		this.totalPassedCars = 0;
		this.averagePassingTime = 0;
		this.totalFuelConsumption = 0;
		this.averageFuelConsumption = 0;
		this.totalPassingTime = 0;
		this.totalLaneChange = 0;
		this.totalWaitingTime = 0;
		this.totalStopCount = 0;
		this.totalUploadingCount = 0;
		

		Iterator<Light> it = lights.iterator();
		while(it.hasNext()){
			Light myLight = it.next();
			myLight.nextTimeStep();
			if (isCritical(myLight)) {
				createClusters(myLight);
			} else if(isCriticalEnd(myLight)) {
				dismissClusters(myLight);
			}
		}
		
		Iterator<Lane> itr = lanes.iterator();
		while(itr.hasNext()){
			Lane lane = itr.next();
			lane.nextTimeStep();
			this.totalPassingTime += lane.totalPassingTime;
			this.totalPassedCars += lane.passedCarCounter;
			this.totalLaneChange += lane.laneChangingCounter;
			this.totalFuelConsumption += lane.totalFuelConsumption;
			this.totalWaitingTime += lane.totalWaitingTime;
			this.totalStopCount += lane.totalStopCount;
			this.totalUploadingCount += lane.uploadingCount;
		}
		
		if(this.totalPassedCars!=0) {
			this.averagePassingTime = (int) (this.totalPassingTime / this.totalPassedCars);
			this.averageFuelConsumption = this.totalFuelConsumption / this.totalPassedCars;
			this.averageWaitingTime = 0.1 * this.totalWaitingTime / this.totalPassedCars;
			this.averageStopCount = (double) this.totalStopCount / this.totalPassedCars;
		}
	}
	
	
	
	public static boolean isCritical (Light nextLight) {
		int critical = 30 * 10;
		if (nextLight.getStatus()==Color.GREEN && nextLight.getCounter()==critical)
			return true;
		else
			return false;
	}
	
	
	
	public static boolean isCriticalEnd (Light nextLight) {
		int endCritical = 2 * 10;
		if (nextLight.getStatus()==Color.GREEN && nextLight.getCounter()==endCritical)
			return true;
		else
			return false;
	}
	
	
	
	ArrayList <Cluster> clusters = new ArrayList <Cluster>();
	int cluster_size = 100;
	private void createClusters(Light nextLight) {
		if(nextLight.prevLight==null) {
			//System.out.println("no previous one.");
			return;
		}
		List<Vehicle> vehicles = null;
		for (int d = nextLight.getX(); d > nextLight.prevLight.getX(); d -= cluster_size){
			vehicles = betweenVehicles(d-cluster_size, d);
			if(vehicles != null && vehicles.size() != 0) {
				Cluster cluster = new Cluster(nextLight, vehicles);
				cluster.setClusterID();
				clusters.add(cluster);
				//System.out.println("Cluster size is " + vehicles.size());
			}
		}
	}
	
	
	private void dismissClusters(Light nextLight) {
		if(nextLight.prevLight==null){
			return;
		}
		Cluster myCluster;
		Iterator<Cluster> itr = clusters.iterator();
		while(itr.hasNext()){
			myCluster = itr.next();
			if(myCluster.nextLight==nextLight){
				myCluster.resetVehicles();
				itr.remove();
			}
		}
	}
	
	
	private List<Vehicle> betweenVehicles (int fromPos, int toPos){
		int toIndexLeft = lanes.get(0).frontVehicle(fromPos);
		int fromIndexLeft = lanes.get(0).afterVehicle(toPos);
		int toIndexMiddle = lanes.get(1).frontVehicle(fromPos);
		int fromIndexMiddle = lanes.get(1).afterVehicle(toPos);
		int toIndexRight = lanes.get(2).frontVehicle(fromPos);
		int fromIndexRight = lanes.get(2).afterVehicle(toPos);
		ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

		for(int i = fromIndexLeft; i <= toIndexLeft; i++){
			vehicles.add(lanes.get(0).myVehicles.get(i));
		}
		for(int i = fromIndexMiddle; i <= toIndexMiddle; i++){
			vehicles.add(lanes.get(1).myVehicles.get(i));
		}
		for(int i = fromIndexRight; i <= fromIndexRight; i++){
			vehicles.add(lanes.get(2).myVehicles.get(i));
		}

		if (vehicles.size()==0)
			return null;
		else
			return vehicles;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	
	public void lauchFrame() {
		this.setLocation(100, 100);
		this.setSize(SCREEN_WIDTH, SCREEN_HIGHT);
		this.setTitle("NJIT Traffic Sim");
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setVisible(true);
		new Thread(new ScenarioThread()).start();
	}
		
	
	private class ScenarioThread implements Runnable {
		public void run() {
			while(true){
				nextTimeStep();
				repaint();
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void paint(Graphics g) {
		Iterator<Lane> itr = lanes.iterator();
		while(itr.hasNext()){
			itr.next().draw(g);
		}
		Iterator<Light> it = lights.iterator();
		while(it.hasNext()){
			it.next().draw(g);
		}
	}
	
	
	public void update(Graphics g) {
		if(offScreenImage == null){
			offScreenImage = this.createImage((int)distance+100, SCREEN_HIGHT);
		}
		Graphics gOffScreen = offScreenImage.getGraphics();
		Color c = gOffScreen.getColor();
		gOffScreen.setColor(Color.WHITE);
		gOffScreen.fillRect(0, 0, (int)distance+100, SCREEN_HIGHT);
		gOffScreen.setColor(c);
		paint(gOffScreen);
		g.drawImage(offScreenImage, 0, 0, null);
	}
	
	
	public void update1(Graphics g, JFrame jFrame) {
		if(offScreenImage == null){
			offScreenImage = jFrame.createImage((int)distance+100, SCREEN_HIGHT);
		}
		Graphics gOffScreen = offScreenImage.getGraphics();
		Color c = gOffScreen.getColor();
		gOffScreen.setColor(Color.WHITE);
		gOffScreen.fillRect(0, 0, (int)distance+100, SCREEN_HIGHT);
		gOffScreen.setColor(c);
		paint(gOffScreen);
		g.drawImage(offScreenImage, 0, 0, null);
	}
	
	
	public Image nextScenario(Graphics g, JFrame jFrame){
		nextTimeStep();
		update1(g, jFrame);
		return offScreenImage;
	}
	
	public void nextScenario1(){
		nextTimeStep();
	}

	
}
