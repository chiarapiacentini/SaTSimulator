package main;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import main.Position.Terrain;
import map.MapOSM;
import org.apache.commons.collections15.Transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.Serializable;

public class UAVGrid  implements Serializable {

	private List<Position> grid;
	private HashMap<Node,Node> graph;
	private List<Pair<Position,List<Position>>> edgesList;
	private MapOSM mapOSM;
	private ArrayList<Position> forTerrain;
	private List<Position> centroids;
	ArrayList<List<Position>> cities = new ArrayList<>();
	private Set<Set<Node>> clusters ;
	private static final long serialVersionUID = 1L;

	private double maxX;
	private double maxY;
	private double minX;
	private double minY;
	private double stepX;
	private double stepY;
	private int nX;
	private int nY;
	private int edgeCount;
	public DirectedGraph<Node,Edge> rng;

	// this is a square in the map
	public class Node implements Serializable{
		Position position;
		List<Position> neighbours;
		int indexX;
		int indexY;
		Node(Position p){
			indexX = getIndexX(p);
			indexY = getIndexY(p);
			neighbours = new ArrayList<Position>();
			double x = minX + stepX*indexX;
			double y = minY + stepY*indexY;
			position = new Position(x,y);
		}
		
		public Position getPosition(){
			return position;
		}

		private int getIndexX(Position p) {
			return (int) Math.floor(((p.getLatitude() - minX) / stepX) + 0.5);
		}

		private int getIndexY(Position p) {
			return (int) Math.floor(((p.getLongitude() - minY) / stepY) + 0.5);
		}

		boolean isAdjacent(Position p){
			int iX = getIndexX(p);
			int iY = getIndexY(p);
            return ((Math.abs(iX-indexX) + Math.abs(iY-indexY)) == 1  && iX < nX - 1 && iY < nY - 1 && iY > 1 && iX > 1);
		}

		void addNeighobour(Node c){
			neighbours.add(c.getPosition());
		}
		void addNeighobour(Position p){
			neighbours.add(p);
		}
		List<Position> getNeighbours(){
			return neighbours;
		}
		
		@Override 
		public int hashCode() {
		    return indexX*nX + indexY;
		}
		@Override
		public boolean equals(Object obj){
			if (this == obj)
		        return true;
		    if (obj == null)
		        return false;
		    if (getClass() != obj.getClass())
		        return false;
			Node other = (Node) obj;
			return hashCode()==other.hashCode();
		}
		@Override
		public String toString(){
			return "v-" + hashCode();
		}
	}
	
	public class Edge implements Serializable{
		private int id;
		private double travelTime;
		private double weight;
		public Node from;
		public Node to;
		public Edge(Node from, Node to, double t, double w){
			this.from = from;
			this.to = to;
			id = edgeCount++;
			travelTime = t;
			weight = w;
		}
		
		public String toString(){
			return "E-" + id;
		}

		double getTravelTime(){
			return travelTime;
		}

		double getWeight(){
			return weight;
		}
	}

	UAVGrid(Position startPosition, MapOSM mapOSM){
		if (main.Parameters.debug) System.out.println("\t...setting the grid");
		long start_time = System.currentTimeMillis();
		grid = new ArrayList<>();
		graph = new HashMap<>();
		edgesList = new ArrayList<>();
        centroids = new ArrayList<>();
		edgeCount = 0;
		rng = new DirectedSparseGraph<>();
		this.mapOSM = mapOSM;
		double rad = Parameters.pointsInGrid; // number of points
		int count = 0;
		int iPoints = 0;

		stepX = Math.abs(startPosition.getOffsetInMeters(0,main.Parameters.gridYSize).getLatitude()-startPosition.getLatitude());
		stepY =  Math.abs(startPosition.getOffsetInMeters(main.Parameters.gridXSize,0).getLongitude()-startPosition.getLongitude());
        minX = startPosition.getLatitude()-stepX*rad;
        maxX = startPosition.getLatitude()+stepX*rad;
        minY = startPosition.getLongitude()-stepY*rad;
        maxY = startPosition.getLongitude()+stepY*rad;
        System.out.println("minX " + minX);
        System.out.println("maxX " + maxX);
        System.out.println("minY " + minY);
        System.out.println("maxY " + maxY);
        nX = (int) (Math.floor(Math.abs(maxX-minX)/stepX)+0.5);
		nY = (int) (Math.floor(Math.abs(maxY-minY)/stepY)+0.5);
        if (Parameters.debug) System.out.println("\tN of points : " + (nX)*(nY) + " " + nX + " x " + nY);
		ArrayList <main.Pair<Position,Position>>  selected = getEdgesInsideArea();
		forTerrain = getPointsInsideArea();

		//System.out.println("selecting " + selected.size() + "/" + State.getGH().getEdgesAsPairs().size() );
		for (int iX = 0; iX < nX;  ++iX){
			for (int iY = 0; iY < nY;  ++iY){
				double x = minX + iX*stepX;
				double y = minY + iY*stepY;
                if (iPoints%Math.ceil(nX*nY/100)==0 && main.Parameters.debug) {
					System.out.print("\t\t" + count +  " % \n");
		            //System.out.flush();
					count++;
				}
                Position pp = new Position(x,y);
				Node c = new Node(pp);
                graph.put(c, c);
				Position p = c.getPosition();
				main.Position.Terrain t = MapOSM.getTerrainType(p, main.Parameters.distanceTerrain,forTerrain);
				p.setTerrain(t);
				grid.add(p);
				iPoints++;
			}
		}

		getConnections(selected);
		
		if (Parameters.debug) 		System.out.println("Vertex " + rng.getVertexCount() + " edges " + rng.getEdgeCount());

		long end_time = System.currentTimeMillis();
		if (main.Parameters.debug) System.out.println("\tfinished in " + (end_time-start_time)/1000. + " secs");
		if (main.Parameters.debug) System.out.println("\tgrid contains  " + iPoints + " points");
		

		for (Position p : grid){
			Node c = new Node(p);
			if (graph.containsKey(c)){
				rng.addVertex(c);
				List<Position> edges = graph.get(c).getNeighbours();
				for (Position e : edges){
					Node to = new Node(e);
					if (rng.findEdge(c, to) == null){
						Edge edge = new Edge(c,to,2/(getTerrainEffectOnSpeed(e.getTerrain())+getTerrainEffectOnSpeed(p.getTerrain())),
								(UAV.getTerrainEffectOnConceleament(e.getTerrain())+UAV.getTerrainEffectOnConceleament(p.getTerrain()))/2);
                        rng.addEdge(edge, c, to, EdgeType.DIRECTED);
					}
				}
				edgesList.add(new Pair(p,graph.get(c).getNeighbours()));
			}
		}
        groupCities();

        System.out.println("edges list : " + edgesList.size());
    }
	
	public List<Position> getGrid(){
		return grid;
	}
	
	private void getConnections(ArrayList <main.Pair<Position,Position>>  edges){
		int i = 0;
		int iEdge = 0;
		for (Pair<Position,Position> e : edges){
			Position from = e.first;
			Position to = e.second;
			Node cFrom = new Node(from);
			Node cTo = new Node(to);
			if (cFrom.equals(cTo))
				continue;
			if (cFrom.isAdjacent(cTo.getPosition())){
				if (graph.containsKey(cFrom)){
					i++;
					Node c = graph.get(cFrom);
					c.addNeighobour(cTo);
					graph.put(c,c);
				}
				if (graph.containsKey(cTo)){
					i++;
					Node c = graph.get(cTo);
					c.addNeighobour(cFrom);
					graph.put(c,c);
				}
			}else{
				// edge traverse more than one Node
				double m = (from.getLongitude() - to.getLongitude())/(from.getLatitude() - to.getLatitude());
				double q = (from.getLatitude()*to.getLongitude() - to.getLatitude()*from.getLongitude())/(from.getLatitude() - to.getLatitude());
				if (from.getLatitude() < to.getLatitude()){
					Node c1 = new Node(from);
					for ( double x = from.getLatitude(); x < to.getLatitude(); x+=stepX/Parameters.gridDivision){
						double y = m * x + q;
						Node c2 = new Node(new Position(x,y));
						if (!c2.equals(c1) && c2.isAdjacent(c1.getPosition())){
							if (graph.containsKey(c1)){
								Node c = graph.get(c1);
								c.addNeighobour(c2);
								graph.put(c,c);
								iEdge++;
							}
							if (graph.containsKey(c2)){
								Node c = graph.get(c2);
								c.addNeighobour(c1);
								graph.put(c,c);
								iEdge++;
							}
							c1 = c2;
						}
					}
				}else{
					Node c1 = new Node(to);
					for ( double x = to.getLatitude(); x < from.getLatitude(); x+=stepX/Parameters.gridDivision){
						double y = m * x + q;
						Node c2 = new Node(new Position(x,y));
						if (!c2.equals(c1) && c2.isAdjacent(c1.getPosition())){
							if (graph.containsKey(c1)){
								Node c = graph.get(c1);
								c.addNeighobour(c2);
								graph.put(c,c);
								iEdge++;
							}
							if (graph.containsKey(c2)){
								Node c = graph.get(c2);
								c.addNeighobour(c1);
								graph.put(c,c);
								iEdge++;
						}
							c1 = c2;
						}
					}
				}
			}
		}

		if(Parameters.debug) System.out.println("\t\tfound " + i + " connections out of " + iEdge + " edges");
	}

	
	public ArrayList <main.Pair<Position,Position>> getEdgesInsideArea(){
		return (ArrayList<Pair<Position, Position>>) State.getGH().getEdgesAsPairs().stream().filter( z -> 
			z.first.getLatitude() > minX && z.first.getLongitude() > minY
			&& z.second.getLatitude() > minX && z.second.getLongitude() > minY
			&& z.first.getLatitude() < maxX && z.first.getLongitude() < maxX
			&& z.second.getLatitude() < maxX && z.second.getLongitude() < maxY
				).collect(Collectors.toList());
	}


	public ArrayList <Position> getPointsInsideArea(){
		return (ArrayList<Position>) mapOSM.getPoints().stream().filter( z -> 
			z.getLatitude() > minX && z.getLongitude() > minY
			&& z.getLatitude() < maxX && z.getLongitude() < maxX).collect(Collectors.toList());
	}

	public List<Position> getNeighbour(Position p){
		List<Position> toReturn = new ArrayList<Position>();
		for (int x=-1; x<2 ; ++x){
			for (int y = -1; y<2; ++y){
                if (x==0 && y==0)
					continue;
				if ( (x*y) !=0)
					continue;
                Position newp = new Position(p.getLatitude()+x*stepX,p.getLongitude()+y*stepY);
                toReturn.add(newp);
				
			}
		}
		return toReturn;
	}
	
	public List<Pair<Position,List<Position>>> getEdges(){
		return edgesList;
	}


	// group cities and calculate centroid
	void groupCities(){
		List<Position> city = grid.stream().filter( x -> x.getTerrain() == Terrain.CITY).collect(Collectors.toList());
		List<Position> seen = new ArrayList<Position>();
		List<Integer> numbers = new ArrayList<Integer>();
		int nCity = 0;
		for (Position c : city){
			boolean isNewCity = true;
			for (Position s : seen){
				if (c.distance(s) < Parameters.distanceTerrain){
					isNewCity = false;
					Integer index = s.getCity();
					Integer n = numbers.get(index);
					Position centroid = centroids.get(index);
					c.setCity(index);
					centroids.set(index, new Position((centroid.getLatitude()*n+c.getLatitude())/(n+1), (centroid.getLongitude()*n+c.getLongitude())/(n+1)));
					numbers.set(index,n+1);
					cities.get(index).add(c);
					break;
				}
			}
			if (isNewCity){
				c.setCity(nCity);
				numbers.add(1);
				centroids.add(c);
				cities.add(new ArrayList<>());
				cities.get(nCity).add(c);
				nCity++;
			}
			seen.add(c);
		}
		if (Parameters.debug)
			System.out.println(nCity + " cities found");
		
		// add Edge between neighbour in city
		//addEdgeInsideSameCity();
	}

	public List<List<Position>> getCitiesPositions(){
	    return cities;
    }
	private void addEdgeInsideSameCity(){
		// add edge in between squares of city
		List<Position> city = grid.stream().filter( x -> x.getTerrain() == Terrain.CITY).collect(Collectors.toList());
		for (Position c : city){
			List<Position> neighbour = getNeighbour(c);
			Node n = new Node(c);
			if (graph.containsKey(n)){
				for (Position p : neighbour){
					Node nn = graph.get(n);
					nn.addNeighobour(p);
					graph.put(nn,nn);
				}
			}
			//List<Position> neighbourCity = neighbour.stream().filter( x -> x.getTerrain() == Terrain.CITY).collect(Collectors.toList());
			edgesList.add(new Pair<Position,List<Position>>(c,neighbour));
		}
	}

	public List<Position> getCentroids(){
		return centroids;
	}

	public double getMinX(){
		return minX;
	}
	public double getMinY(){
		return minY;
	}
	public double getMaxX(){
		return maxX;
	}
	public double getMaxY(){
		return maxY;
	}
	public double getStepX(){
	    return stepX;
    }
    public  double getStepY(){
	    return stepY;
    }
	
	Set<Set<Node>> findCluster (){
		if(clusters == null){
			Transformer<Graph<Node, Edge>, Set<Set<Node>>> trns = new WeakComponentClusterer<Node,Edge>();
			clusters = trns.transform(rng);
		}
		return clusters;
	}
	
	boolean isConnected(Node f, Node t){
		for (Set<Node> cluster : clusters){
			if (cluster.contains(f) && cluster.contains(t))
				return true;
		}
		return false;
	}
	public List<List<Position>> findPathsToCities(Position lkp, double w){
		List<List<Position>> toReturn = new ArrayList<List<Position>>();
		Transformer<Edge, Double> weightEdge = new Transformer<Edge,Double> (){
			@Override
			public Double transform(Edge edge) {
				return edge.getTravelTime()*(1-w*edge.getWeight());
			}
		};
		findCluster();
	    DijkstraShortestPath<Node, Edge> dd = new DijkstraShortestPath<Node, Edge>(rng, weightEdge);
	    int iCity = -1;
		for (Position destination : getCentroids()){
			iCity++;
			try{
				Node source = new Node(lkp);
				Node sink = new Node(destination);
				if (graph.containsKey(source)){
					source = graph.get(source);
				}else{
					System.err.println("source not found");
				}
				if (graph.containsKey(sink)){
					sink = graph.get(sink);
				}else{
					System.err.println("sink not found");
				}
				if (!isConnected(source,sink)){
					//System.err.println(iCity + " not in the same component");
					continue;
				}
			    List<Edge> path = dd.getPath(source, sink);
				List<Position> pathPosition = new ArrayList<Position>();
				boolean first = true;
				for(Edge e: path){
					edu.uci.ics.jung.graph.util.Pair<Node> p = rng.getEndpoints(e);
					if (first){
						Node i = p.getFirst();
						first = false;
						pathPosition.add(i.position);
					}
					Node j = p.getSecond();
					pathPosition.add(j.position);
				}
				toReturn.add(pathPosition);
				//System.err.println("path found for city " + iCity + " " + destination);
			} catch (Exception ex){
				//System.err.println("path not found for city " + iCity++ + " "  + destination);
			}

		}
		return toReturn;
	}

	public Terrain getTerrain(Position pp){
		return graph.get(new Node(pp)).getPosition().getTerrain();
	}
	@Override
	public String toString(){
		return new StringBuffer("grid").append(grid).toString();
	}

	static public double getTerrainEffectOnSpeed(Position.Terrain t){
		switch(t) {
			case URBAN:
				return 0.65;
			case SUBURBAN:
				return 0.7;
			case CITY:
				return 0.6;
		}
		return 1;
	}
}
