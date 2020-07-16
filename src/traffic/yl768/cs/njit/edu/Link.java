package traffic.yl768.cs.njit.edu;
import java.util.ArrayList;
import java.util.List;

public class Link {
	int id;
	Node start;
	Node end;
	float limit;
	List<Lane> lanes = new ArrayList<Lane>();
}