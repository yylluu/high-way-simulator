package traffic.yl768.cs.njit.edu;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


public class Vehicle {
	
	private final int shadowTimer = 25;
	
	//Physical characters
	private double x = 0.0; //current x
	private double y = 0.0; //current y
	private double v = 20.0; //current speed
	private double acc = 0.0;
	private double length = 5.0; //meter

	private double maxSpeed = 200;

	public double maxDec = -10;
	public double maxAcc = 1.5;

	//Time step
	private double dt = 0.1; //second
	
	//Driver characters
	private double v0 = 16.0; //meter/second
	private double v0_ = 30;
	private double reactionTime = 1.0; //second
	
	//App
	double advisorySpeed = 16.0;
	
	//IDM parameters
	private double t = 1.2; //second
	private double a = 1.5;
	private double b = 2.0;
	private double s0 = 1.6;
	private double s1 = 0;
	
	//VIDFF parameters
	private double lint = 5.23;
	private double beta = 2.14;
	private double tao = 4.9;
	private double lambda = 0.536;
	
	public int shadowCounter = 0;
	private boolean obstructor = false;
	
	public double[] pastSpeed = {v,v,v,v,v,v,v,v,v,v};
	public double[] pastAcc = {0,0,0,0,0,0,0,0,0,0};
	public double[] oldAcc = {0,0,0,0,0,0,0,0,0,0};
	public double compositeAcc = 0;
	public double[] pastLocation = {x,x-v*dt,x-2*v*dt,x-3*v*dt,x-4*v*dt,x-5*v*dt,x-6*v*dt,x-7*v*dt,x-8*v*dt,x-9*v*dt};
	
	public int bufferLength;

	
	public boolean isAppUser = false;
	
	//Car-following
	public Color color = Color.BLUE;
	public int eclipasedTimeCounter = 0;
	public double fuelConsumption = 0.0;
	public double laneChangeCounter = 0;
	public int waitingTime = 0;
	
	//Cluster
	public Cluster clusterID = null;
	public Double clusterDesiredSpeed = null;
	
	//Constructor
	public Vehicle(double x, double y, double dt, double v, double acc, double v0, int shadowCounter, Color color, boolean isAppUser){
		super();
		Random rand = new Random();
		double z = rand.nextDouble();
		reactionTime = reactionTime + z;
		this.x = x;
		this.y = y;
		this.dt = dt;
		this.v = v;
		this.v0 = v0;
		this.v0_ = v0;
		this.acc = acc;
		this.shadowCounter = shadowCounter;
		this.color = color;
		this.isAppUser = isAppUser;
	}
	
	//Constructor
	public Vehicle(double x, double y, double dt, double v, double acc, double v0, int shadowCounter, Color color){
		this(x, y, dt, v, acc, v0, shadowCounter, color, false);
	}
	
	//Constructor
	public Vehicle(double x, double y, double dt, double v, double v0, boolean isAppUser){
		this(x, y, dt, v, 0, v0, 0, Color.BLUE, isAppUser);
	}
	
	//Constructor
	public Vehicle(double x, double y, double dt, double v, double v0){
		this(x, y, dt, v, 0, v0, 0, Color.BLUE);
	}
	
	
	//Constructor
	public Vehicle(double x, double y, double dt, double v, double acc, double v0, int shadowCounter){
		this(x, y, dt, v, acc, v0, shadowCounter, Color.YELLOW);
	}
	
	
	//Constructor
	public Vehicle(double x, double y) {
		this(x, y, 0.1, 15, 0, 20, 0, Color.BLUE);
	}
	
	
	public boolean isObstructor(){
		return obstructor;
	}
	
	public boolean isShadow(){
		if(shadowCounter==0)
			return false;
		else
			return true;
	}
	
	public void setObstructor(){
		obstructor = true;
		length = 0;
	}
	
	
	//paint
	public void draw(Graphics g){
		Color c = g.getColor();
		g.setColor(color);
		if(length!=0)
			g.fillRect((int) (x-length), (int) y, (int)length, 3);
		else
			g.fillRect((int) x, (int) (y - 4), 1, 10);
		g.setColor(c);
	}
	
	
	//update location
	double averageV;
	public void forward(Vehicle front){
		
		if(this.isObstructor()){
			if(v == 0)
				return;
			if(v + acc*dt < 0){
				x = x + v*dt/2;
				v = 0;
			} else {
				x = x + v*dt + acc*dt*dt/2;
				v = v + acc*dt;
			}
			return;
		}
		
		
		//Update velocity using acc
		//Make sure velocity is positive so that the car not go back
		if(v + acc * dt >= 0){ 
			x = x + v*dt + acc*dt*dt/2;
			v = v + acc * dt;
		} else {
			x = x + v*dt/2;
			v = 0;
		}
		
		//Following car cannot pass previous car
		if(front!=null){
//System.out.println("triggerred");
			if(!front.isObstructor()&& x >= front.getX())
				x = front.getX() - 0.1;
		}
		
//		
//		if(this.isObstructor())
//			
//		else
//			System.out.println("normal car trigger this");
//		return minDec;
			
		
		//update history speed
		averageV = 0;
		for (int i = 9;i > 0;i--){
			pastSpeed[i] = pastSpeed[i-1];
			averageV += pastSpeed[i];
		}
		pastSpeed[0] = v;
		averageV += v;
		averageV /= 10;
		//update history speed
		for (int i = 9;i > 0;i--){
			pastLocation[i] = pastLocation[i-1];
		}
		pastLocation[0] = x;
	}
	

	
	
	public double getSpeed(){
		return v;
	}
	
	
	public double getAcc(){
		return acc;
	}
	
	
	public double getLength(){
		return length;
	}
	
	
	public void setSpeed(double v){
		this.v = v;
	}
	
	
	public double getX(){
		return x;
	}
	
	
	public void setX(double x){
		this.x = x;
	}
	
	
	public void setAcc(double acc){
		this.acc = acc;
		updateHistoryAcc();
	}
	
	public void setDesireSpeed(double v0){
		this.v0 = v0;
	}
	
	public void resetDesireSpeed(){
		this.v0 = this.v0_;
	}
	
	public void increaseEclipasedTimeCounter(){
		eclipasedTimeCounter++;
	}

	
	public void newSpeed(Vehicle front){
		double vsafe = - reactionTime * b + Math.sqrt(reactionTime*b*reactionTime*b + front.getSpeed()*front.getSpeed() + 2*b*(front.getX()-x-front.getLength()));
		if(vsafe > v + dt*a){
			v = v + dt*a;
		}else{
			v = vsafe;
		}
		if(v > maxSpeed){
			v = maxSpeed;
		}
	}
	
	
	public double newAcc(Vehicle front){
		if(front!=null)
			return 6 * (front.pastSpeed[9] - pastSpeed[9]) / (front.pastLocation[9] - pastLocation[9]);
		else
			return a * (1 - Math.pow(v/v0, 4)); 
	}
	
	public double newAccIDM(Vehicle front){
		if(front!=null){
			double s_= s0 + s1*Math.sqrt(v/v0) + t*v + v*(v-front.getSpeed())/(2*Math.sqrt(a*b));
			double a1 = a * (1 - Math.pow(v/v0, 4) - Math.pow(s_/(front.getX()-x-front.getLength()), 2) );
			if (Double.isNaN(a1)) {
				return -15d;
			} else {
				return a1;
			}
		} else {
			return a * (1 - Math.pow(v/v0, 4));
		}
	}
	
	
	public double newAccVDIFF(Vehicle front){
		if(front!=null){
			double s = front.getX() - x - front.getLength();
			double vopt = v0 * (Math.tanh(s/lint - beta) - Math.tanh(-beta)) / 2;
			return (vopt - v) / tao - lambda*(v-front.getSpeed());
		} else {
			return (v0 - v) / tao;
		}
	}
	
	
	public double brake(){
		acc = - 7;
		return acc;
	}
	
		
	double averageAcc = 10;
	public void updateHistoryAcc(){
		averageAcc = 0;
		for (int i = 9;i > 0;i--){
			oldAcc[i] = oldAcc[i-1];
			averageAcc += oldAcc[i];
		}
		oldAcc[0] = acc;
		//averageAcc = averageAcc/12 + acc/4;
		averageAcc = averageAcc / 10 + acc / 10;
	}
	
	
	public void updateCompositeAcc(){
		compositeAcc = 0;
		for (int i = 9;i > 0;i--){
			pastAcc[i] = pastAcc[i-1];
			compositeAcc += pastAcc[i];
		}
		pastAcc[0] = averageAcc;
		compositeAcc = compositeAcc/18 + pastAcc[0]/2;
	}
	
	ArrayList<Double> status = new ArrayList<Double>();
	public void increaseFuelConsumption(){
		double currentFuelConsumption = -1;
		if(averageAcc < -4){
			averageAcc = -4;
		} else if (averageAcc > 2) {
			averageAcc = 2;
		}
//		if(acc < -4){
//			acc = -4;
//		} else if (acc > 2) {
//			acc = 2;
//		}
		if(v < 0){
			v = 0;
		} else if (v > 30) {
			v = 30;
		}
		status.add(Math.floor(v * 2) / 2d);
		status.add(Math.floor(averageAcc * 20) / 20d);
//		status.add(Math.floor(acc * 20) / 20d);
		//System.out.println(status);
		try {
			currentFuelConsumption = Scenario.v_a_fuel.get(status.toString());
		} catch (Exception e){
			System.out.println(status);
			System.out.println(v);
			System.out.println(compositeAcc);
			System.out.println(acc);
			return;
		}
		if(currentFuelConsumption != -1)		
			this.fuelConsumption += currentFuelConsumption;
		else{
			System.err.println("Sth wrong");
		}
		status.clear();
	}
		
	public int stopCount = 0;
	public boolean stopFlag = false;
	public void increaseWaitingTime(){
		if(averageV <= 1.0) {
			waitingTime++;
			if(!stopFlag)
				stopCount++;
			stopFlag = true;
		} else {
			stopFlag = false;
		}
	}
	
	
	public Vehicle changeLaneMOBIL_IDM(Lane left, Lane right, Lane current, Vehicle front, Vehicle back){
		
		double left_desire = 0;
		double right_desire = 0;
		
		Vehicle pred = null;
		Vehicle succ = null;
		
		//int leftIndex = 0;
		//int rightIndex = 0;
		
		double ac_=0, an_=0, ao_=0, ac=0, an=0, ao=0;

		ac = this.newAccIDM(front);
		if(back!=null){
			ao = back.newAccIDM(this);
			ao_ = back.newAccIDM(front);
		}
		
		if(left!=null){
			left_desire = 0;
			succ = left.afterVehicle(this);
			pred = left.frontVehicle(this);
			ac_ = this.newAccIDM(pred);
			if(succ!=null){
				an_ = succ.newAccIDM(this);
				an = succ.newAccIDM(pred);
			}
			left_desire = ac_ + an_ + ao_ - ac - an - ao - 1.0;
		} else
			left_desire = 0;
		
		succ = null; pred = null;
		ac_=0; an_=0;  an=0; 
		if(right!=null){
			right_desire = 0;
			succ = right.afterVehicle(this);
			pred = right.frontVehicle(this);
			ac_ = this.newAccIDM(pred);
			if(succ!=null){
				an_ = succ.newAccIDM(this);
				an = succ.newAccIDM(pred);
			}
			right_desire = ac_ + an_ + ao_ - ac - an - ao - 1.0;
		} else
			right_desire = 0;
		
		if(left_desire > right_desire && left_desire > 0){
			Vehicle shadow = new Vehicle(x, y, dt, v, acc, v0, 20);
			this.color = Color.RED;
			this.y = left.y+4;
			//left.myVehicles.add(leftIndex, this);
			left.addVehicle(this);
			//current.deleteVehicle(this);
//System.out.println("go to left");
			if(x > bufferLength-500)
				laneChangeCounter++;
			return shadow;
		} else if(left_desire < right_desire && right_desire > 0){
			Vehicle shadow = new Vehicle(x, y, dt, v, acc, v0, 20);
			this.color = Color.GREEN;
			this.y = right.y+4;
			//right.myVehicles.add(rightIndex, this);
			//current.deleteVehicle(this);
			right.addVehicle(this);
//System.out.println("go to right");
			if(x > bufferLength-500)
				laneChangeCounter++;
			return shadow;
		} else
			return null;
	}
	
	
	public Vehicle changeLaneMOBIL_VDIFF(Lane left, Lane right, Lane current, Vehicle front, Vehicle back){
		
		double left_desire = 0;
		double right_desire = 0;
		
		Vehicle pred = null;
		Vehicle succ = null;
		
		double ac_=0, an_=0, ao_=0, ac=0, an=0, ao=0;

		ac = this.newAccVDIFF(front);
		if(back!=null){
			ao = back.newAccVDIFF(this);
			ao_ = back.newAccVDIFF(front);
		}
		
		if(left!=null){
			left_desire = 0;
			succ = left.afterVehicle(this);
			pred = left.frontVehicle(this);
			ac_ = this.newAccVDIFF(pred);
			if(succ!=null){
				an_ = succ.newAccVDIFF(this);
				an = succ.newAccVDIFF(pred);
			}
			left_desire = ac_ + an_ + ao_ - ac - an - ao - 0.5;
		} else
			left_desire = 0;
		
		succ = null; pred = null;
		ac_=0; an_=0;  an=0; 
		if(right!=null){
			right_desire = 0;
			succ = right.afterVehicle(this);
			pred = right.frontVehicle(this);
			ac_ = this.newAccVDIFF(pred);
			if(succ!=null){
				an_ = succ.newAccVDIFF(this);
				an = succ.newAccVDIFF(pred);
			}
			right_desire = ac_ + an_ + ao_ - ac - an - ao - 0.5;
		} else
			right_desire = 0;
		
		if(left_desire > right_desire && left_desire > 0){
			Vehicle shadow = new Vehicle(x, y, dt, v, acc, v0, shadowTimer);
			this.color = Color.RED;
			this.y = left.y+4;
			//left.myVehicles.add(leftIndex, this);
			left.addVehicle(this);
			//current.deleteVehicle(this);
//System.out.println("go to left");
			return shadow;
		} else if(left_desire < right_desire && right_desire > 0){
			Vehicle shadow = new Vehicle(x, y, dt, v, acc, v0, shadowTimer);
			this.color = Color.GREEN;
			this.y = right.y+4;
			//right.myVehicles.add(rightIndex, this);
			//current.deleteVehicle(this);
			right.addVehicle(this);
//System.out.println("go to right");
			return shadow;
		} else
			return null;
	}
	
}
 