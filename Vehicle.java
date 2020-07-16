package traffic.yl768.cs.njit.edu;
import java.awt.Color;
import java.awt.Graphics;
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
			return a1;
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
	

//	static final private double[][] POS_ARGS 
//	= {{-1.58921, 0.140167,-0.006802, 1.3064e-04},
//	{0.242398, 0.109927,-0.016696, 5.7007e-04},
//	{0.453730,-0.244877, 0.045206,-9.6746e-04},
//	{-0.09031, 0.065993,-0.006307, 2.2232e-05}};
//	static final private double[][] NEG_ARGS 
//	={{-1.58921, 0.109879,-0.003788, 7.8248e-05},
//	{-0.46523, 0.268350,-0.016796, 2.8393e-04},
//	{-0.10342, 0.132270,-0.006050, 9.3123e-05},
//	{-0.02473, 0.024018,-3.10e-04,-3.3636e-06}};
	
//	static final private double[][] POS_ARGS = 
//			{{-1.58921, 0.143167,-0.005702, 1.3064e-04},
//			 {0.242398, 0.109927,-0.016796, 5.8107e-04},
//			 {0.453730,-0.245877, 0.046006,-9.3546e-04},
//			 {-0.10031, 0.063993,-0.007870, 1.5432e-05}};
//	static final private double[][] NEG_ARGS =	
//			{{-1.58921, 0.129879,-0.004088, 8.0248e-05},
//			 {-0.46523, 0.268350,-0.016496, 2.9393e-04},
//			 {-0.10342, 0.134070,-0.006550, 9.3723e-05},
//			 {-0.00125, 0.026018,-3.37e-04,-4.4536e-06}};

//	N =	[-1.58921, 0.109879,-0.003888, 8.0248e-05;
//	-0.46523, 0.268350,-0.016796, 2.9393e-04;
//	-0.10342, 0.132270,-0.006550, 9.3723e-05;
//	-0.02473, 0.024018,-4.17e-04,-4.3536e-06];
//	P = [-1.58921, 0.133167,-0.005702, 1.3064e-04;
//	0.242398, 0.109927,-0.016796, 5.7107e-04;
//	0.453730,-0.245877, 0.044006,-9.6746e-04;
//	-0.10031, 0.063993,-0.009070, 2.1332e-05];
	
//	static final private double[][] POS_ARGS 
//	 = {{-1.58921, 0.123167,-0.005702, 1.3064e-04},
//		{0.242398, 0.109927,-0.016796, 5.7107e-04},
//		{0.453730,-0.245877, 0.044006,-9.6746e-04},
//		{-0.10031, 0.063993,-0.009070, 2.1332e-05}};
//	static final private double[][] NEG_ARGS
//	 = {{-1.58921, 0.109879,-0.003888, 8.0248e-05},
//	    {-0.46523, 0.268350,-0.016796, 2.9393e-04},
//		{-0.10342, 0.132270,-0.006550, 9.3723e-05},
//		{-0.02473, 0.024018,-4.17e-04,-4.3536e-06}};

	
//	static final private double[][] NEG_ARGS
//	 = {{-1.58921, 0.1342, -0.0052, 8.0248e-05},
//	    {-0.4093, 0.2968, 0.0116, 2.9393e-04},
//		{-0.6861, 0.3028, 0.0154, 9.3723e-05},
//		{ 0.2422, 0.0573, 0.0095, 4.3536e-06}};
//	 = {{-1.9428,-0.4093,-0.6861,-0.2422},
//    { 0.1342, 0.2968, 0.3028, 0.0573},
//	{-0.0052, 0.0116, 0.0154, 0.0095},
//	{ 0.0001,-0.0007,-0.0011,-0.0005}};

	
	
//	static final private double[][] POS_ARGS 
//	 = {{-1.58921, 0.036991,-0.00044, 2.8e-06},
//		{0.067333, 0.008482,-0.00036, 3.4e-06},
//		{0.035010,-0.00527, 0.000262,-1.6e-06},
//		{-0.00215, 0.000381,-1.5e-05, 9.8e-09}};
//	static final private double[][] NEG_ARGS
//	 = {{-1.58921, 0.030522,-0.00030,1.72e-06},
//	    {-0.12923, 0.020706,-0.00036,1.75e-06},
//		{-0.00798, 0.002835,-3.9e-05,1.55e-07},
//		{-0.00053, 0.000143,-6.9e-07,-2e-09}};
	
	
	static final private double[][] POS_ARGS 
	 = {{-0.89611, 0.036991,-0.00044, 2.8e-06},
		{0.067323, 0.008482,-0.00037, 3.4e-06},
		{0.034822,-0.00527, 0.000260,-1.6e-06},
		{-0.00225, 0.000381,-1.5e-05, 9.8e-09}};
	static final private double[][] NEG_ARGS
	 = {{-0.89611, 0.030522,-0.00030,1.72e-06},
	    {-0.12923, 0.020706,-0.00036,1.75e-06},
		{-0.00798, 0.002835,-3.9e-05,1.55e-07},
		{-0.00053, 0.000143,-6.9e-07,-2e-09}};
	
	//oiginal
//	static final private double[][] POS_ARGS 
//	 = {{-0.89611, 0.036991,-0.00044, 2.8e-06},
//		{0.067323, 0.008482,-0.00037, 3.4e-06},
//		{0.034822,-0.00527, 0.000260,-1.6e-06},
//		{-0.00225, 0.000381,-1.5e-05, 9.8e-09}};
//	static final private double[][] NEG_ARGS
//	 = {{-0.89611, 0.030522,-0.00030,1.72e-06},
//	    {-0.12923, 0.020706,-0.00036,1.75e-06},
//		{-0.00798, 0.002835,-3.9e-05,1.55e-07},
//		{-0.00053, 0.000143,-6.9e-07,-2e-09}};
	
//	static final private double[][] POS_ARGS 
//				 = {{-1.9116, 2.7630,-2.5418, 0.6289},
//					{ 0.1108,-0.0850, 0.2791,-0.1128},
//					{-0.0008,-0.0198, 0.0275,-0.0111},
//					{-0.0005, 0.0011,-0.0019, 0.0008}};
//	static final private double[][] NEG_ARGS
//				 = {{-1.9428,-0.4093,-0.6861,-0.2422},
//				    { 0.1342, 0.2968, 0.3028, 0.0573},
//					{-0.0052, 0.0116, 0.0154, 0.0095},
//					{ 0.0001,-0.0007,-0.0011,-0.0005}};
	
//	static final private double[][] POS_ARGS 
//		 = {{-2.02233, 3.0116,-3.6463, 2.0060},
//			{ 0.1334,-0.2541, 0.7478,-0.7446},
//			{-0.0060, 0.0359,-0.0932, 0.0935},
//			{ 0.0002,-0.0013, 0.0032,-0.0034}};
//	static final private double[][] NEG_ARGS
//		 = {{-1.9866,-0.4183,-1.0615,-0.6250},
//		    { 0.1305, 0.1031, 0.3768, 0.2607},
//			{-0.0061, 0.0299,-0.0099,-0.0226},
//			{ 0.0002,-0.0013, 0.0001, 0.0008}};
	
	double averageAcc;
	public void updateHistoryAcc(){
		averageAcc = 0;
		for (int i = 9;i > 0;i--){
			oldAcc[i] = oldAcc[i-1];
			averageAcc += oldAcc[i];
		}
		oldAcc[0] = acc;
		//averageAcc = averageAcc/12 + acc/4;
		averageAcc = averageAcc/10 + acc/10;
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
	
	
	public void increaseFuelConsumption(){
		double currentFuelConsumption = 0;
		if(compositeAcc>=0){
			for(int i=0; i<4; i++){
				for(int j=0; j<4; j++){
					currentFuelConsumption += POS_ARGS[i][j] * Math.pow(3.6*v,j) * Math.pow(3.6*compositeAcc,i);
				}
			}
		} 
//		else if (compositeAcc<=-2){
//			for(int i=0; i<4; i++){
//				for(int j=0; j<4; j++){
//					currentFuelConsumption += NEG_ARGS[i][j] * Math.pow(v,j) * Math.pow(-2,i);
//				}
//			}
//		} 
		else	{
			for(int i=0; i<4; i++){
				for(int j=0; j<4; j++){
					currentFuelConsumption += NEG_ARGS[i][j] * Math.pow(3.6*v,j) * Math.pow(3.6*compositeAcc,i);
				}
			}
		}
		currentFuelConsumption = Math.expm1(currentFuelConsumption)+1;
//		if(currentFuelConsumption>200)
//			//TODO
//			//System.out.println();
//			;
//		else
//			this.fuelConsumption += currentFuelConsumption;
		if(Double.isInfinite(currentFuelConsumption) || Double.isNaN(currentFuelConsumption))
			//System.out.println();
			;
		else if (currentFuelConsumption<10)
			this.fuelConsumption += currentFuelConsumption;
		else if (compositeAcc>=0)
			this.fuelConsumption += 0;
		else if  (compositeAcc<0)
			this.fuelConsumption += 0;
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
 