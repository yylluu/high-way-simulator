package traffic.yl768.cs.njit.edu;

import java.awt.Color;
import java.awt.Graphics;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;


/**
 * 
 * @author yuan
 *
 */
public class Lane {
	
	private Node src = null;
	private Node dst = null;
	int timestep = 0;
	int heatingTime = 90000;
	int bufferLength = 2000;
	
	private double x = 0.0;
	public double y = 0.0;
	
	private double dt = 0.1;
	private double distance;
	private int limit = 23; //22.35 meter/second = 50 mile/hour
	private double desireSpeed = 0;

	
	public int passedCarCounter = 0;
	public long totalPassingTime = 0;
	public int averagePassingTime = 0;
	public int laneChangingCounter = 0;
	public double totalFuelConsumption = 0;
	public int totalWaitingTime = 0;
	public int totalStopCount = 0;
	
	private int [] lightPosition;
	
	private Lane left = null;
	private Lane right = null;
	
	Vehicle leader = null;
	List<Light> lights = new ArrayList<Light>();
	List<Vehicle> myVehicles = new LinkedList<Vehicle>();
	
	Map<String, Double> v_a_fuel;
	
	//Constructor
	public Lane(double x, double y, double dt, int limit, double distance, int [] lightPosition, int heatingTime, double lambda, Map<String, Double> v_a_fuel) {
		super();
		this.x = x;
		this.y = y;
		this.dt = dt;
		this.limit = limit;
		this.distance = distance;
		this.lightPosition = lightPosition;
		this.heatingTime = heatingTime;
		this.bufferLength = lightPosition[0];
		this.lamda = lambda;
		this.v_a_fuel = v_a_fuel;
	}
	
	
	//
	public void setLights(List<Light> lights){
		this.lights = lights;
	}
	
	public void setDesireSpeed(double desireSpeed){
		this.desireSpeed = desireSpeed;
	}
	
	//paint
	public void draw(Graphics g){
		Color c = g.getColor();
		g.setColor(Color.GRAY);
		g.fillRect((int) x, (int) y, (int)distance, 10);
		g.setColor(c);
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			itr.next().draw(g);
		}
	}
	
	
	//
	public void nextTimeStep(){
		if(myVehicles.size() > 1200){
			System.out.print("Error: too much congestion.");
			System.exit(-1);
		}
		//appImpact_afap_sa_penalty();
		//appImpact_afap_sa_cluster();
		//appImpact_afap();
		//appImpact_afap_sa();
		//appImpact_glosa();
		appImpact_glosa_cluster();
		carRemove();
		laneChange();
		carFollowing();
		carForward();
		vehicleGenerator();
		timestep++;
	}
	
	
	//add any vehicle
	public void addVehicle(Vehicle v){
		Vehicle myVehicle = null; 
		int afterVehicleIndex = 0;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next(); 
			if(myVehicle.getX() < v.getX()){
				break;
			}
			afterVehicleIndex++;
		}
		myVehicles.add(afterVehicleIndex, v);
	}
	
	
	
	//delete any vehicle
	public void deleteVehicle(Vehicle v){
		myVehicles.remove(v);
	}
	
	
	
	//set the left and right lane;
	public void setNextLanes(Lane left, Lane right){
		this.left = left;
		this.right = right;
	}
	
	
	
	//Find the frontVehicle
	public Vehicle frontVehicle(Vehicle v){
		Vehicle myVehicle;
		Vehicle frontVehicle = null;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next(); 
			if(myVehicle.getX() >= v.getX() && myVehicle!=v) {
				frontVehicle = myVehicle;
			} else {
				break;
			}
		}
		return frontVehicle;
	}
	
	
	
	//Find the index of frontVehicle
	public int frontVehicle(int pos){
		int i = -1;
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next(); 
			if(myVehicle.getX() > pos) {
				i++;
			} else {
				break;
			}
		}
		return i;
	}
	
	
	public Vehicle nonObstructFront(Vehicle v){
		Vehicle myVehicle = frontVehicle(v);
		if(!myVehicle.isObstructor())
			return myVehicle;
		else
			return frontVehicle(myVehicle);
	}
	
	
	
	//find the afterVehicle
	public Vehicle afterVehicle(Vehicle v) {
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()) {
			myVehicle = itr.next();
			if(myVehicle.getX() < v.getX()) {
				return myVehicle;
			}
		}
		return null;
	}
	
	
	
	//find the index of afterVehicle
	public int afterVehicle(int pos) {
		int i = 0;
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()) {
			myVehicle = itr.next();
			if(myVehicle.getX() <= pos) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	

	
	public Light nextLight(Vehicle myVehicle){
		Light nextLight = null;
		Iterator<Light> it = lights.iterator();
		while(it.hasNext()){
			Light myLight = it.next();
			if(myLight.getX() > myVehicle.getX()){
				nextLight = myLight;
				break;
			}
		}
		return nextLight;
	}
	
	public Light nextLight(Light light){
		Light nextLight = null;
		Iterator<Light> it = lights.iterator();
		while(it.hasNext()){
			Light myLight = it.next();
			if(myLight.getX() > light.getX()){
				nextLight = myLight;
				break;
			}
		}
		return nextLight;
	}
	
	
	public void carForward(){
		Vehicle myVehicle;
		Vehicle frontVehicle = null;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			frontVehicle = this.frontVehicle(myVehicle);
			myVehicle.forward(frontVehicle);
		}
	}
	
	
	
	public void carFollowing(){
		Vehicle myVehicle;
		Vehicle frontVehicle = null;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			if(myVehicle.isObstructor()) {
				myVehicle.brake();
				//continue;
			}
			else {
				myVehicle.setAcc(myVehicle.newAccIDM(frontVehicle));
				//myVehicle.setAcc(myVehicle.newAccVDIFF(frontVehicle));
				//myVehicle.setAcc(myVehicle.newAcc(frontVehicle));
				//myVehicle.newSpeed(frontVehicle);
			}
			if(timestep%10==0 && !myVehicle.isObstructor() && !myVehicle.isShadow() && myVehicle.getX()>bufferLength+10){
				//			if(!myVehicle.isObstructor() && !myVehicle.isShadow() && myVehicle.getX()>bufferLength+10){
				myVehicle.updateCompositeAcc();
				myVehicle.increaseFuelConsumption();
			}
			frontVehicle = myVehicle;
		}
	}
	
	
	public void carRemove(){
		ListIterator<Vehicle> itr = myVehicles.listIterator();
		while(itr.hasNext()){
			Vehicle myVehicle = itr.next();
			//carRemove method is called for each time step as well, it is okay to add increase time counter here
			if(!myVehicle.isObstructor() && !myVehicle.isShadow() && myVehicle.getX()>bufferLength+50) {
				myVehicle.increaseEclipasedTimeCounter();
				myVehicle.increaseWaitingTime();
				int pos = (((int)myVehicle.getX() - bufferLength - 50) / 5) * 5 + bufferLength + 50;
				Double [] v_pos;
				try {
					v_pos = Scenario.v_pos.get(pos);
					v_pos[0] = v_pos[0] * v_pos[1] + myVehicle.getSpeed();
					v_pos[1]++;
					v_pos[0] = v_pos[0] / v_pos[1];
				} catch (Exception e) {
					//System.out.println(" error ");
				}
			}
			if(myVehicle.getX() > distance + 50){
				if (!myVehicle.isShadow() && timestep>heatingTime  && !myVehicle.isObstructor() ){
					totalFuelConsumption += myVehicle.fuelConsumption;
					totalPassingTime += myVehicle.eclipasedTimeCounter;
					totalWaitingTime += myVehicle.waitingTime;
					totalStopCount += myVehicle.stopCount;
					averagePassingTime = (int) (0.1 * totalPassingTime / (++passedCarCounter));
					laneChangingCounter += myVehicle.laneChangeCounter;
				}
				itr.remove();
			}
		}
	}
	
	
	
	public void laneChange(){
		ListIterator<Vehicle> itr = myVehicles.listIterator();
		while(itr.hasNext()){
			Vehicle myVehicle = itr.next();
			if(myVehicle.isObstructor()) 
				continue;
			//�����źŵƵƷ�Χ������
			boolean isNearLight = false;
			double position = 0;
			for (int i = 0; i < this.lightPosition.length; i++){
				position += lightPosition[i];
				int dx1 = (int) (position - myVehicle.getX());
				if (dx1 < 80 && dx1 > -40){
					isNearLight = true;
					break;
				}
			}
			if(myVehicle.getX() > bufferLength-500 && myVehicle.getSpeed() > 5 && !myVehicle.isShadow() && !isNearLight){
				Vehicle shadow = myVehicle.changeLaneMOBIL_IDM(left, right, this, this.frontVehicle(myVehicle), this.afterVehicle(myVehicle));
				//Vehicle shadow = myVehicle.changeLaneMOBIL_VDIFF(left, right, this, this.frontVehicle(myVehicle), this.afterVehicle(myVehicle));
				if (shadow != null){
					itr.remove();
					//itr.add(shadow);
//					if(timestep>heatingTime)
//						laneChangingCounter++;
				}
			}
			if(myVehicle.shadowCounter>1){
				myVehicle.shadowCounter--;
			}
			if(myVehicle.shadowCounter==1){
				itr.remove();
			}
		}
	}
	
	
	
	private int N; // in unit of dt
	private int minGap = 15; // in unit of dt
	private double lamda = 1.0; 
	private boolean waitingGenerator = false;
	private Vehicle newVehicle = null;
	private double vsafe = 20;
	public double appPercentage = 1;
	//public long seed;
	public void vehicleGenerator(){
		if(newVehicle != null){
			vsafe = Math.sqrt(16 + newVehicle.getSpeed()*newVehicle.getSpeed() + 8*(newVehicle.getX()-4));
			//minGap = 20;
		}
		if(!waitingGenerator){
			RealRandom rand = new RealRandom();
	        double n = - (1 / lamda) * Math.log(rand.nextDouble());
	        N = minGap + (int)(n/dt);
	        waitingGenerator = true;
//System.out.println("New vehicle waiting " + N);
		} else if (N==0){

			double v0 = 23;
			if(desireSpeed==0){
				RealRandom rand = new RealRandom();
				//int z1 = rand.nextInt();
				//rand = new Random(z1);
				double z2 = rand.nextDouble();
				
				//The distribution of desired velocity
				if(z2<0.5)
					v0 = 0.44704*150.0*(0.183333333 + Math.sqrt(0.033611111 - (2.5-z2)/75.0));
				else {
					v0 = 0.44704*150.0*(0.183333333 + Math.sqrt(0.033611111 - (1.5+z2)/75.0));
					v0 = 0.44704*80 - v0;
				}
			} else {
				v0 = desireSpeed*0.44704;
			}
			
			
			//The percentage of application users
			if (new RealRandom().nextDouble() < this.appPercentage)
				newVehicle = new Vehicle(x, y+4, dt, vsafe, v0, true);
			else
				newVehicle = new Vehicle(x, y+4, dt, vsafe, v0, false);
//System.out.println("New vehicle desire speed " + v0);
			newVehicle.bufferLength = bufferLength;
			this.addVehicle(newVehicle);
			waitingGenerator = false;

		} else {
			N--;
		}
	}
	
	
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	public void appImpact_afap(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1){
				continue;
			}
			myVehicle.setDesireSpeed(limit);
		}
	}
	
	
	
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	public void appImpact_afap_sa(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1 || myVehicle.getX()<bufferLength){
				continue;
			}
			//if pass the last light, reset desire speed;
			Light nextLight = this.nextLight(myVehicle);
			if(nextLight==null) {
				myVehicle.resetDesireSpeed();
				continue;
			}
			//distance to next light
			double distance2Light = nextLight.getX() - (int) myVehicle.getX();
			double interestingTime = 0;
			//if next light is green;
			if(nextLight.getStatus()==Color.GREEN) {
				if( (distance2Light) / limit < (double) (nextLight.getCounter()+ Light.YELLOW_DUTY) / 10.0 ) {
					//If this user can pass before the end of GREEN duty, then go;
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else {
					interestingTime = (double) (Light.RED_DUTY + Light.YELLOW_DUTY + nextLight.getCounter()) / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				}
			} else if (nextLight.getStatus()==Color.YELLOW){
				if ((distance2Light) / limit < (double) (nextLight.getCounter()) / 10.0){
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					//appSpeed = 0.4*appSpeed + 0.6*limit;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else
					interestingTime = (double) (Light.RED_DUTY + nextLight.getCounter()) / 10.0;
			} else if (nextLight.getStatus()==Color.RED){
				interestingTime = (double) (nextLight.getCounter()) / 10.0;
			}
			double appSpeed = (distance2Light) / interestingTime;
						
			double minLimit = 30;
			double beta = 1.0;
			if (beta*appSpeed <= minLimit*0.44704){
				myVehicle.setDesireSpeed(minLimit*0.44704);
			} else if (beta*appSpeed < limit){
				myVehicle.setDesireSpeed(beta*appSpeed);
			} else {
				myVehicle.setDesireSpeed(limit);
			}
			
		}
	}
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	public void appImpact_afap_sa_penalty(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1 || myVehicle.getX()<bufferLength){
				continue;
			}
			//if pass the last light, reset desire speed;
			Light nextLight = this.nextLight(myVehicle);
			if(nextLight==null) {
				myVehicle.resetDesireSpeed();
				continue;
			}
			//distance to next light
			double distance2Light = nextLight.getX() - (int) myVehicle.getX();
			double interestingTime = 0;
			//if next light is green;
			if(nextLight.getStatus()==Color.GREEN) {
				if( (distance2Light) / (0.9*limit) < (double) (nextLight.getCounter()+ Light.YELLOW_DUTY) / 10.0 ) {
					//If this user can pass before the end of GREEN duty, then go;
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					//The key formula
					//appSpeed = 0.4*appSpeed + 0.6*limit;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else {
					interestingTime = (double) (Light.RED_DUTY + Light.YELLOW_DUTY + nextLight.getCounter()) / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				}
			} else if (nextLight.getStatus()==Color.YELLOW){
				if ((distance2Light) / (0.9*limit) < (double) (nextLight.getCounter()) / 10.0){
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					//appSpeed = 0.4*appSpeed + 0.6*limit;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else
					interestingTime = (double) (Light.RED_DUTY + nextLight.getCounter()) / 10.0;
			} else if (nextLight.getStatus()==Color.RED){
				interestingTime = (double) (nextLight.getCounter()) / 10.0;
			}
			double appSpeed = (distance2Light) / interestingTime;
						
			double minLimit = 30;
			double beta = 1.0;
			if (beta*appSpeed <= minLimit*0.44704){
				myVehicle.setDesireSpeed(minLimit*0.44704);
			} else if (beta*appSpeed < limit){
				myVehicle.setDesireSpeed(beta*appSpeed);
			} else {
				myVehicle.setDesireSpeed(limit);
			}
			
		}
	}
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	public void appImpact_afap_acc(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1 || myVehicle.getX()<bufferLength){
				continue;
			}
			//if pass the last light, reset desire speed;
			Light nextLight = this.nextLight(myVehicle);
			if(nextLight==null) {
				myVehicle.resetDesireSpeed();
				continue;
			}
			//distance to next light
			double distance2Light = nextLight.getX() - (int) myVehicle.getX();
			double interestingTime = 0;
			//if next light is green;
			if(nextLight.getStatus()==Color.GREEN) {
				if(canPassCriteia(myVehicle.getSpeed(), (double)limit, myVehicle.maxAcc, (double) (nextLight.getCounter()+ Light.YELLOW_DUTY) / 10.0,distance2Light)) {
					//If this user can pass before the end of GREEN duty, then go;
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					//The key formula
					//appSpeed = 0.4*appSpeed + 0.6*limit;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else {
					interestingTime = (double) (Light.RED_DUTY + Light.YELLOW_DUTY + nextLight.getCounter()) / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				}
			} else if (nextLight.getStatus()==Color.YELLOW){
				if(canPassCriteia(myVehicle.getSpeed(), (double)limit, myVehicle.maxAcc, (double) (nextLight.getCounter()) / 10.0,distance2Light)) {
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					//appSpeed = 0.4*appSpeed + 0.6*limit;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else
					interestingTime = (double) (Light.RED_DUTY + nextLight.getCounter()) / 10.0;
			} else if (nextLight.getStatus()==Color.RED){
				interestingTime = (double) (nextLight.getCounter()) / 10.0;
			}
			double appSpeed = (distance2Light) / interestingTime;
			//System.out.println(appSpeed);
			if (appSpeed <= 30*0.44704){
				myVehicle.setDesireSpeed(30*0.44704);
			} else if (appSpeed < limit){
				myVehicle.setDesireSpeed(appSpeed);
			} else {
				myVehicle.setDesireSpeed(limit);
			}
		}
	}
	
	
	private boolean canPassCriteia(double v0, double vl, double a, double ti, double d){
		double t1 = (vl - v0) / a;
		if(t1 > ti){
			if(v0*ti+0.5*a*ti*ti>d)
				return true;
			else 
				return false;
		} else {
			if(v0*t1+0.5*a*t1*t1+vl*(ti-t1)>d)
				return true;
			else
				return false;
		}
	}
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
    public long uploadingCount = 0;
	public void appImpact_afap_sa_cluster(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1 || myVehicle.getX()<bufferLength){
				continue;
			}
			//if pass the last light, reset desire speed;
			Light nextLight = this.nextLight(myVehicle);
			if(nextLight==null) {
				myVehicle.resetDesireSpeed();
				continue;
			}
			//distance to next light
			double distance2Light = nextLight.getX() - (int) myVehicle.getX();
			double interestingTime = 0;
			//if next light is green;
			if(nextLight.getStatus()==Color.GREEN) {
				if(myVehicle.clusterID==null || myVehicle.clusterID.canClusterPass == true || myVehicle.getX() > myVehicle.clusterID.nextLight.getX()){
					if( (distance2Light) / limit < (double) (nextLight.getCounter()+ Light.YELLOW_DUTY) / 10.0 ) {
						//If this user can pass before the end of GREEN duty, then go;
						interestingTime = (double) nextLight.getCounter() / 10.0;
						double appSpeed = (distance2Light) / interestingTime;
						//The key formula
						//appSpeed = 0.4*appSpeed + 0.6*limit;
						appSpeed = 0.0*appSpeed + 1.0*limit;
						myVehicle.setDesireSpeed(appSpeed);
						continue;
					} else {
						interestingTime = (double) (Light.RED_DUTY + Light.YELLOW_DUTY + nextLight.getCounter()) / 10.0;
						double appSpeed = (distance2Light) / interestingTime;
						if (appSpeed <= 30*0.44704) {
							appSpeed = 30 * 0.44704;
						} else if (appSpeed > 50*0.44704) {
							appSpeed = 50 * 0.44704;
						}
						myVehicle.setDesireSpeed(appSpeed);
						if(myVehicle.clusterID != null && myVehicle.getX() < myVehicle.clusterID.nextLight.getX()) {
							myVehicle.clusterID.canClusterPass = false;
							myVehicle.clusterID.setClusterDesiredSpeed(appSpeed);
							uploadingCount++;
						}
						continue;
					}
				} else {
					interestingTime = (double) (Light.RED_DUTY + Light.YELLOW_DUTY + nextLight.getCounter()) / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					if (appSpeed <= 30*0.44704) {
						appSpeed = 30 * 0.44704;
					} else if (appSpeed > 50*0.44704) {
						appSpeed = 50 * 0.44704;
					}
					if (appSpeed < myVehicle.clusterDesiredSpeed) {
						myVehicle.setDesireSpeed(appSpeed);
						//System.out.println(appSpeed);
						myVehicle.clusterID.setClusterDesiredSpeed(appSpeed);
						uploadingCount++;
						//System.out.println(appSpeed);
					} else {
						myVehicle.setDesireSpeed(myVehicle.clusterDesiredSpeed);
					}
					continue;
				}
			} else if (nextLight.getStatus()==Color.YELLOW){
				if ((distance2Light) / limit < (double) (nextLight.getCounter()) / 10.0){
					interestingTime = (double) nextLight.getCounter() / 10.0;
					double appSpeed = (distance2Light) / interestingTime;
					//appSpeed = 0.4*appSpeed + 0.6*limit;
					appSpeed = 0.0*appSpeed + 1.0*limit;
					myVehicle.setDesireSpeed(appSpeed);
					continue;
				} else
					interestingTime = (double) (Light.RED_DUTY + nextLight.getCounter()) / 10.0;
			} else if (nextLight.getStatus()==Color.RED){
				interestingTime = (double) (nextLight.getCounter()) / 10.0;
			}
			double appSpeed = (distance2Light) / interestingTime;
						
			double minLimit = 30;
			double beta = 1.0;
			if (beta*appSpeed <= minLimit*0.44704){
				myVehicle.setDesireSpeed(minLimit*0.44704);
			} else if (beta*appSpeed < limit){
				myVehicle.setDesireSpeed(beta*appSpeed);
			} else {
				myVehicle.setDesireSpeed(limit);
			}
			
		}
	}
	

	
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	
	
	
	
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	public void appImpact_glosa(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1 || myVehicle.getX()<bufferLength){
				continue;
			}
			//if pass the last light, reset desire speed;
			Light nextLight = this.nextLight(myVehicle);
			if(nextLight==null) {
				myVehicle.resetDesireSpeed();
				continue;
			}
			//distance to next light
			double distance2Light = nextLight.getX() - (int) myVehicle.getX();
			double interestingTime = 0;
			
			double vMin, vMax;
			if(nextLight.getStatus()==Color.RED){
				interestingTime = (nextLight.getCounter() + Light.GREEN_DUTY) / 10.0;
				vMin = distance2Light / interestingTime;
				interestingTime = (nextLight.getCounter() + dischargeHeadway(myVehicle, nextLight)) / 10.0;
				vMax = distance2Light / interestingTime;
				redefineDesireSpeed(myVehicle, vMin, vMax);
				continue;
			} else if (nextLight.getStatus()==Color.GREEN){
				interestingTime = (nextLight.getCounter() + Light.YELLOW_DUTY) / 10.0;
				vMin = distance2Light / interestingTime;
				if(vMin < limit){
					vMax = limit;
					if(vMin < 18)
						vMin = 18;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					//myVehicle.advisorySpeed = limit;
					//myVehicle.setDesireSpeed(limit);
					continue;
				} else {
					interestingTime = (Light.YELLOW_DUTY + Light.RED_DUTY + nextLight.getCounter() + Light.GREEN_DUTY)/ 10.0;
					vMin = distance2Light / interestingTime;
					interestingTime = (Light.YELLOW_DUTY + Light.RED_DUTY + nextLight.getCounter() + dischargeHeadway(myVehicle, nextLight))/ 10.0;
					vMax = distance2Light / interestingTime;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					continue;
				} 
			} else {
				interestingTime = (nextLight.getCounter()) / 10.0;
				vMin = distance2Light / interestingTime;
				if(vMin < limit){
					vMax = limit;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					continue;
				} else {
					interestingTime = (Light.RED_DUTY + nextLight.getCounter() + Light.GREEN_DUTY )/ 10.0;
					vMin = distance2Light / interestingTime;
					interestingTime = (Light.RED_DUTY + nextLight.getCounter() + dischargeHeadway(myVehicle, nextLight))/ 10.0;
					vMax = distance2Light / interestingTime;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					continue;
				} 
			}
			
		}
	}
	
	void redefineDesireSpeed (Vehicle myVehicle, double vMin, double vMax) {
		if(myVehicle.advisorySpeed < vMin || myVehicle.advisorySpeed > vMax){
			if (30*0.44704 <= vMax && vMax <= limit){
				myVehicle.advisorySpeed = vMax;
			} else if (vMin <= limit && limit <= vMax) {
				myVehicle.advisorySpeed = limit;
			} else if (vMax < 30*0.44704){
				myVehicle.advisorySpeed = 30*0.44704;				
			}
			myVehicle.setDesireSpeed(myVehicle.advisorySpeed);
		}
	}
	
	double dischargeHeadway (Vehicle v, Light nextLight) {
		double dischargeHeadway = 0.0;
		Light lastLight = null;
		int headway = 0;
		for (int i = lights.size() - 1; i >= 0; i--) {
			if ( lights.get(i).getX() < nextLight.getX()){
				lastLight = lights.get(i);
			}
		}
		if (lastLight != null){
			for (int i = 0; i < myVehicles.size(); i++) {
				if (myVehicles.get(i).getX() >= lastLight.getStopX() && myVehicles.get(i).getX() < nextLight.getStopX() && myVehicles.get(i).getSpeed() < 1.0){
					if(v.getX() < myVehicles.get(i).getX())
						headway++;
					else
						break;
				}
			}
		} else {
			for (int i = 0; i < myVehicles.size(); i++) {
				if (myVehicles.get(i).getX() < nextLight.getStopX() && myVehicles.get(i).getSpeed() < 1.0){
					if(v.getX() < myVehicles.get(i).getX())
						headway++;
					else
						break;
				}
			}
		}
		switch (headway) {
		    case 0: dischargeHeadway = 0.0; break;
		    case 1: dischargeHeadway = 0.5; break;
		    case 2: dischargeHeadway = 0.8; break;
		    default: dischargeHeadway = 0.8 + 0.2 * (headway - 2);
		}
		return 10 * dischargeHeadway;
	}
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	
	
	
	
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	public void appImpact_glosa_cluster(){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = myVehicles.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			//if not an app user, or an obstructer or a shadow or in the buffer 
			if(myVehicle.isAppUser==false || myVehicle.isObstructor() || myVehicle.shadowCounter>=1 || myVehicle.getX()<bufferLength){
				continue;
			}
			//if pass the last light, reset desire speed;
			Light nextLight = this.nextLight(myVehicle);
			if(nextLight==null) {
				myVehicle.resetDesireSpeed();
				continue;
			}
			//distance to next light
			double distance2Light = nextLight.getX() - (int) myVehicle.getX();
			double interestingTime = 0;
			
			double vMin, vMax;
			if(nextLight.getStatus()==Color.RED){
				interestingTime = (nextLight.getCounter() + Light.GREEN_DUTY + dischargeHeadway(myVehicle, nextLight)) / 10.0;
				vMin = distance2Light / interestingTime;
				interestingTime = (nextLight.getCounter()) / 10.0;
				vMax = distance2Light / interestingTime;
				redefineDesireSpeed(myVehicle, vMin, vMax);
				continue;
			} else if (nextLight.getStatus()==Color.GREEN){
				interestingTime = (nextLight.getCounter() + Light.YELLOW_DUTY) / 10.0;
				vMin = distance2Light / interestingTime;
				if(vMin < limit){
					vMax = limit;
					if(vMin < 18)
						vMin = 18;
					redefineDesireSpeed_cluster(myVehicle, vMin, vMax);
					//myVehicle.advisorySpeed = limit;
					//myVehicle.setDesireSpeed(limit);
					continue;
				} else {
					interestingTime = (Light.GREEN_DUTY + Light.YELLOW_DUTY + Light.RED_DUTY + nextLight.getCounter())/ 10.0;
					vMin = distance2Light / interestingTime;
					interestingTime = (Light.YELLOW_DUTY + Light.RED_DUTY + nextLight.getCounter() + dischargeHeadway(myVehicle, nextLight))/ 10.0;
					vMax = distance2Light / interestingTime;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					continue;
				} 
			} else {
				interestingTime = (nextLight.getCounter()) / 10.0;
				vMin = distance2Light / interestingTime;
				if(vMin < limit){
					vMax = limit;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					continue;
				} else {
					interestingTime = (Light.GREEN_DUTY + Light.RED_DUTY + nextLight.getCounter())/ 10.0;
					vMin = distance2Light / interestingTime;
					interestingTime = (Light.RED_DUTY + nextLight.getCounter() + dischargeHeadway(myVehicle, nextLight))/ 10.0;
					vMax = distance2Light / interestingTime;
					redefineDesireSpeed(myVehicle, vMin, vMax);
					continue;
				} 
			}
			
		}
	}
	
	void redefineDesireSpeed_cluster (Vehicle myVehicle, double vMin, double vMax) {
		if(myVehicle.advisorySpeed < vMin || myVehicle.advisorySpeed > vMax){
			if (30*0.44704 <= vMax && vMax <= limit){
				myVehicle.advisorySpeed = vMax;
			} else if (vMin <= limit && limit <= vMax) {
				myVehicle.advisorySpeed = limit;
			} else if (vMax < 30*0.44704){
				myVehicle.advisorySpeed = 30*0.44704;				
			}
			myVehicle.setDesireSpeed(myVehicle.advisorySpeed);
		}
		if(myVehicle.clusterID!=null && myVehicle.getX() < myVehicle.clusterID.nextLight.getX()){
			if (myVehicle.clusterDesiredSpeed == null || myVehicle.advisorySpeed < myVehicle.clusterDesiredSpeed) {
				myVehicle.setDesireSpeed(myVehicle.advisorySpeed);
				myVehicle.clusterID.setClusterDesiredSpeed(myVehicle.advisorySpeed);
			} else {
				myVehicle.setDesireSpeed(myVehicle.clusterDesiredSpeed);
			}
		}
	}
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	/******************************************************************************************************/
	
	
	
	
}