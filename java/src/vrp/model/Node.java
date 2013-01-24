package vrp.model;

import java.util.ArrayList;

public class Node implements Comparable<Node>{
	public int index;
	public Route route;
	
	
	public int cluster;
	public double lat;
	public double lng;
	public double angle;

	public int state;
	public boolean visited;
	
	public ArrayList<Edge> mstEdges;
	
	public String name;
	
	public int weight;
	
	
	public Node(int i, String name, double x, double y, int weight){
		index = i;
		this.name = name;
		this.lat = x;
		this.lng = y;
		this.weight = weight;
	}


	@Override
	public int compareTo(Node o) {
		if(this.angle<o.angle){
			return -1;
		}else if(o.angle == this.angle){
			return 0;
		}else{
			return 1;
		}
	}
	
	@Override
	public String toString() {
		return index+"";
	}
}
