package traffic.yl768.cs.njit.edu;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Light {

	private int x = 0;
	private int y = 0;
	private int stopX = 0;
	
	private int stopV = 25;
	
	private final int R = 10;
	
	private Color status = Color.GREEN;
	private int counter = 1;
	private ArrayList<Lane> lanes = new ArrayList<Lane>();
	private ArrayList<Vehicle> obstructors = new ArrayList<Vehicle>();
	
	public Light prevLight = null;
	
	public static final int GREEN_DUTY = 890;
	public static final int YELLOW_DUTY = 40;
	public static final int RED_DUTY = 460;
	
//	public static final int GREEN_DUTY = 900;
//	public static final int YELLOW_DUTY = 40;
//	public static final int RED_DUTY = 400;

//	public static final int GREEN_DUTY = 800;
//	public static final int YELLOW_DUTY = 1;
//	public static final int RED_DUTY = 1;
	
	Light(int x, int y, ArrayList<Lane> lanes){
		this.x = x;
		this.y = y;
		this.stopX = x - 35;
		this.lanes = lanes;
		createObstructors();
	}
	
	//paint
	public void draw(Graphics g){
		Color c = g.getColor();
		g.setColor(status);
		g.fillOval(x, y, R, R);
		g.setColor(c);
	}

	public void nextTimeStep(){
		counter--;
		if(counter==0){
			if(status==Color.RED){
				counter = GREEN_DUTY;
				status = Color.GREEN;
				resumeTraffic();
			} else if (status==Color.GREEN){
				counter = YELLOW_DUTY;
				status = Color.YELLOW;
				stopTraffic();
			} else if (status==Color.YELLOW){
				counter = RED_DUTY;
				status = Color.RED;
			}
		}
	}
	
	public void createObstructors(){
		Vehicle myVehicle;
		Iterator<Lane> itr = lanes.iterator();
		while(itr.hasNext()){
			Lane myLane = itr.next();
			myVehicle = new Vehicle(stopX, myLane.y+4, 0.1, stopV, 0);
			myVehicle.color = Color.BLACK;
			myVehicle.setObstructor();
			obstructors.add(myVehicle);
		}
	}
	
	public void stopTraffic(){
		for (int i = 0; i < lanes.size(); i++){
			Vehicle obstructor = obstructors.get(i);
			obstructor.setX(stopX);	
			obstructor.setSpeed(stopV);		
			lanes.get(i).addVehicle(obstructor);
		}
	}
	
	public void resumeTraffic(){
		for (int i = 0; i < lanes.size(); i++){
			lanes.get(i).deleteVehicle(obstructors.get(i));
		}
	}
	
	public int getStopX(){
		return stopX;
	}
	
	public int getX(){
		return x;
	}
	
	public Color getStatus(){
		return status;
	}
	
	public int getCounter(){
		return counter;
	}
}
