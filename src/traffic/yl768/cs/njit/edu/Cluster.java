package traffic.yl768.cs.njit.edu;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;

public class Cluster {
	
	public boolean canClusterPass = true;

	public Light nextLight = null;
	public List<Vehicle> cluster = null;
	public Double clusterDesiredSpeed = null;
	
	public Cluster (Light nextLight, List<Vehicle> cluster){
		this.nextLight = nextLight;
		this.cluster = cluster;
	}
	
	public void setClusterID() {
		Vehicle myVehicle;
		Iterator<Vehicle> itr = cluster.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			myVehicle.clusterID = this;
		}
	}
	
	public void setClusterDesiredSpeed(Double clusterDesiredSpeed) {
		this.clusterDesiredSpeed = clusterDesiredSpeed;
		Vehicle myVehicle;
		Iterator<Vehicle> itr = cluster.iterator();
		while(itr.hasNext()) {
			myVehicle = itr.next();
			myVehicle.clusterDesiredSpeed = this.clusterDesiredSpeed;
		}
	}

	public void resetVehicles (){
		Vehicle myVehicle;
		Iterator<Vehicle> itr = cluster.iterator();
		while(itr.hasNext()){
			myVehicle = itr.next();
			myVehicle.clusterID = null;
			myVehicle.clusterDesiredSpeed = null;
		}
	}
	
}
