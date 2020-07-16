package traffic.yl768.cs.njit.edu;

import java.security.SecureRandom;

public class RealRandom {
	
	private SecureRandom rand = new SecureRandom();
	private byte[] bytes = new byte[4];
	
	public static void main (String[] args){
		RealRandom rr = new RealRandom();
		for(int i = 0; i<10000; i++){
			System.out.println(""+rr.nextDouble());
		}
	}
	
	private int nextPositiveInt(){
		rand.nextBytes(bytes);
		int x = java.nio.ByteBuffer.wrap(bytes).getInt();
		if(x>=0){
			return x;
		} else {
			//return nextPositiveInt();
			return -x-1;
		}
	}
	
	public int nextInt(){
		return nextPositiveInt();
	}
	
	public double nextDouble () {	
			int x = nextPositiveInt();
			int y = 2147483647;
			return (double) x / (double)y;
	}
}
