package vrp.program;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import vrp.model.*;


public class VRPProgram {

	public static int CAR_LIMIT = 40;
	public static double[][] distances; //should be created dynamically
	private static Node[] nodes; //0th node is depot
	private static ArrayList<Route> routes;
	private static int stopNum;
	
	public static ArrayList<Route> createRoutes(Node[] nodes, int carLim) {
		double[][] distances = null; //TODO
		return createRoutes(nodes, distances, carLim);
	}
	
	public static ArrayList<Route> createRoutes(Node[] nodes, double[][] distances, int carLim) {
		loadData(nodes, nodes.length, distances, carLim);
		clarkWright();
		ArrayList<Route> cwRoutes = routes;
		sweep();
		ArrayList<Route> sweepRoutes = routes;
		double cwTotalCost = 0;
		double sweepTotalCost = 0;
		for(Route r:cwRoutes){
			cwTotalCost+= r.totalCost;
		}
		for(Route r:sweepRoutes){
			sweepTotalCost+= r.totalCost;
		}
		if (cwTotalCost < sweepTotalCost) {
			return cwRoutes;
		}
		else {
			return sweepRoutes;
		}
	}
	
	public static void main(String[] args) {
		try {
			VRPProgram.loadData("input/inputTest1.in");
			VRPProgram.clarkWright();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static ArrayList<Route> splitRoute() {
		return null;
	}
	
 	public static ArrayList<Node> cluster(){
		Node depo = nodes[0];
		ArrayList<Node> nodesList = new ArrayList<Node>();
		
		for(int i=1;i<nodes.length;i++){
		   Node n = nodes[i];
		   if(n.lat >= depo.lat){
			   if(n.lng>= depo.lng){
				   n.cluster = 1;
			   }else{
				   n.cluster = 4;
			   }
		   }else{
			   if(n.lng>= depo.lng){
				   n.cluster = 2;
			   }else{
				   n.cluster = 3;
			   }
		   }
		   
		   
		   for(int j=1;j<5;j++){
			   if(n.cluster == j){
				   double difx = Math.abs(n.lat - depo.lat);
				   double dify = Math.abs(n.lng - depo.lng);
				   
				   if(dify!=0){
					   double tangA = (double)dify/difx;
					   
					   if(n.cluster == 2 || n.cluster == 4){
						   tangA= 1/tangA;
					   }
					   n.angle+= Math.atan(tangA);
				   }
				   
				   break;
			   }
			   else{
				   n.angle+= Math.PI/2;
			   }
		   }
		   nodesList.add(n);
	   }
	   return nodesList;
	}
	
 	
 	
	/**
	 * Load the data from external variables
	 * @param lNodes
	 * @param lCount
	 * @param lDistances
	 * @param lAmounts
	 * @param lAdds
	 * @param carLim
	 * @return
	 */
	public static boolean loadData(Node[] lNodes,int lCount, double[][] lDistances,int carLim){
		boolean returnVal = true;
		
		try{
			CAR_LIMIT = carLim;
			stopNum = lCount;
			nodes = lNodes;
			distances = lDistances;
		}catch(Exception ex){
			returnVal = false;
		}
		
		return returnVal;
	}
	
	/**
	 * Load the data from file specified in the parameter
	 * @param file
	 * @throws IOException
	 */
	public static void loadData(String file) throws IOException{
		//BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-16"));
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		stopNum = Integer.parseInt(in.readLine()); //MANU number of cities here
		distances = new double[stopNum][stopNum];

		//nactu tabulku vzdalenosti
		for(int i=0;i<stopNum;i++){
			String line = in.readLine();
			String[] inDist = line.split(" ");
			for(int k=0;k<inDist.length;k++){
				int dis = Integer.parseInt(inDist[k]);
				distances[i][k] = dis; //MANU distance matrix here
			}
		}

		in.readLine();

		//nactu mnozstvi objednaneho zbozi
		//adresy a souradnice
		nodes = new Node[stopNum];		

		for(int i=0;i<stopNum;i++){
			String nodeInfo = in.readLine();
			String[] info = nodeInfo.split(";");
			Node n = new Node(i, info[1], Double.parseDouble(info[2]), Double.parseDouble(info[3]), Integer.parseInt(info[0]));
			nodes[i] = n;
		}
		
		in.close();
	}

	
	/**
	 * Implementation of the Sweep algorithm
	 * @return
	 */
	public static String sweep(){
		ArrayList<Node> nodesList = cluster();
		Collections.sort(nodesList);
		
		//Cluster
		Cluster actualCluster = new Cluster();
		
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		//pridam 0 do clusteru
		actualCluster.add(nodes[0]);
		for(int i=0;i<nodesList.size();i++){
			Node n = nodesList.get(i);
			
			//pokud by byla prekrocena kapacita vytvorim novy cluster
			if(actualCluster.amount + n.weight> CAR_LIMIT){
				clusters.add(actualCluster);
				actualCluster = new Cluster();
				//pridam depot uzel do kazdeho clusteru
				actualCluster.add(nodes[0]);
			}
			
			//pridam uzel do clusteru
			//pridam vsechny hrany ktere inciduji s uzly ktere jiz jsou v clusteru
			actualCluster.add(n);
			for(int j=0;j<actualCluster.nodes.size();j++){
				Node nIn = actualCluster.nodes.get(j);
				Edge e = new Edge(nIn,n,distances[nIn.index][n.index]);
				
				Edge eReverse = new Edge(n,nIn,distances[n.index][nIn.index]);
				
				actualCluster.edges.add(e);
				actualCluster.edges.add(eReverse);
			}
			
			//v pripade posledni polozky musim pridat i cluster.
			if(i==nodesList.size()-1){
				clusters.add(actualCluster);
			}
		}
		
		int totalCost = 0;
		int clusterCount = clusters.size();
		
		StringBuilder sb = new StringBuilder();
		sb.append(clusterCount +"\r\n");
		
		for(int i=0;i<clusterCount;i++){
			//System.out.println("Cluster: " + clusters.get(i).amount);
			clusters.get(i).mst();
			//clusters.get(i).printMST();
			clusters.get(i).dfsONMST();
			clusters.get(i).printTSP(sb);
			sb.append("");
			sb.append("\r\n");
			totalCost += MyUtils.compClusterCost(clusters.get(i), distances);
		}
		
		for(int i=0;i<clusterCount;i++){
			clusters.get(i).printTSPAdds(sb);
			sb.append("\r\n");
		}
		sb.append("TOTAL COST OF THE ROUTES:" + totalCost);
		return sb.toString();
	}
	
	/**
	 * Implementation of the Clarks' & Wright's algorithm
	 * @return
	 */
	public static String clarkWright(){
		routes = new ArrayList<Route>();
		
		//I create N nodes. Each node will be inserted into a route.
		//each route will contain 2 edges - from the depo to the edge and back
		//1 route per stop initially
		for(int i=0;i<stopNum;i++){
			
			Node n = nodes[i];
			
			if(i!=0){
				//creating the two edges
				Edge e  = new Edge(nodes[0],n,distances[0][n.index]);
				Edge e2 = new Edge(n,nodes[0],distances[0][n.index]);
			
				Route r = new Route(stopNum);
				//40 omezeni kamionu
				r.weightLimit = CAR_LIMIT;
				r.add(e);
				r.add(e2);
				r.weightActual += n.weight;
				
				routes.add(r);
			}	
		}
		
		
		MyUtils.printRoutes(routes);
		//Computing the savings - the values which made be saved by optimization
		ArrayList<Saving> savingsList = computeSaving(distances, stopNum, nodes);
		//sorting the savings
		Collections.sort(savingsList);
		
		//and use the savings until the list is not empty
		while(!savingsList.isEmpty()){
			Saving actualS = savingsList.get(0);
			
			Node n1 = actualS.from;
			Node n2 = actualS.to;
			
			Route r1 = n1.route;
			Route r2 = n2.route;
			
			int from = n1.index;
			int to = n2.index;
			
			//MyUtils.printSaving(actualS);
			//seems to be merging two routes together into r1, if r1 will allow it
			if(actualS.val>0 && r1.weightActual+r2.weightActual<r1.weightLimit && !r1.equals(r2)){
				
				//moznozt jedna z uzlu do kteryho se de se de do cile
				
				Edge outgoingR2 = r2.outEdges[to];
				Edge incommingR1 = r1.inEdges[from];
				
				
				if(outgoingR2!=null && incommingR1 != null){
					boolean succ = r1.merge(r2, new Edge(n1,n2,distances[n1.index][n2.index]));
					if(succ){
						routes.remove(r2);
					}
				}else{
					System.out.println("Problem");
				}
				
			}
			
			savingsList.remove(0); //eventually completely removes savingsList, but once cleared, routes are created
			//MyUtils.printRoutes(routes);
			
		}
		StringBuilder sb = new StringBuilder();
		sb.append(routes.size() + "\r\n");
		
		
		MyUtils.printRoutesCities(routes,sb);
		MyUtils.printAdds(routes,nodes,sb);
		return sb.toString();
	}
	
	
	/**
	 * Computation of savings. The value which could be saved if we would not return to the depo, but instead pass directly from one node to other.
	 * @param distances2
	 * @param n
	 * @param sav
	 * @param nodesField
	 * @return
	 */
	public static ArrayList<Saving> computeSaving(double[][] distances2, int n, Node[] nodesField){
		double[][] sav = new double[n][n];
		ArrayList<Saving> sList = new ArrayList<Saving>();
		for(int i=1;i<n;i++){
			for(int j=i+1;j<n;j++){
				sav[i][j] = distances2[0][i] + distances2[j][0] - distances2[i][j];
				Node n1 = nodesField[i];
				Node n2 = nodesField[j];
				Saving s = new Saving(sav[i][j],n1, n2);
				sList.add(s);
			}
		}
		return sList;		
	}
}
