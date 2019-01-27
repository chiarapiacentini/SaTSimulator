package main;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.OSMTileFactoryInfo;

import patterns.*;
import visualizer.*;

public class Visualizer extends JPanel implements Observer{
	private JFrame frame;
	private State state;
	private JXMapViewer mapViewer;
	private static final long serialVersionUID = 1L;

	public Visualizer(double x, double y)
	{
		//TileFactoryInfo info = new OSMTileFactoryInfo();
        TileFactoryInfo info = new VirtualEarthTileFactoryInfo(Parameters.visualiseSatellite?VirtualEarthTileFactoryInfo.SATELLITE:VirtualEarthTileFactoryInfo.MAP);
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);

		// Setup local file cache
		File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
		LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

		// Setup JXMapViewer
		mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);


		double coordinate[]={x,y};
		GeoPosition initialPosition = new GeoPosition(coordinate);

		// Set the focus
		mapViewer.setZoom(6);
		mapViewer.setAddressLocation(initialPosition);

		// Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);
		mapViewer.addMouseListener(new CenterMapListener(mapViewer));
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
		//mapViewer.addKeyListener(new PanKeyListener(mapViewer));


		Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>(Arrays.asList(
				new MyWaypoint("T", Color.ORANGE, initialPosition)));

		// Create a waypoint painter that takes all the waypoints
		WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
		waypointPainter.setWaypoints(waypoints);
		waypointPainter.setRenderer(new FancyWaypointRenderer());

		mapViewer.setOverlayPainter(waypointPainter);

		// Display the viewer in a JFrame
		frame = new JFrame("Scotland");
        frame.setLocation(0,0);
        frame.getContentPane().add(mapViewer);
		//frame.setSize(1920, 1024);
		frame.setSize(1280, 1024);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addKeyListener(new MapKeyListener(mapViewer, frame));
        frame.setVisible(true);
	}
	public void addState(State s)
	{
		state = s;
		state.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg)
	{
		// TODO Auto-generated method stub
		// Visualise Targets
		if (Parameters.visualiseMoveMap) {
			Position target = state.getTargets().get(0).getPosition();
			mapViewer.setAddressLocation(new GeoPosition(target.getLatitude(), target.getLongitude()));
		}

		Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>();
		for (Target t: state.getTargets()){
            double targetPosition[]={t.getLatitude(),t.getLongitude()};
            double targetStart[]={t.getStartX(),t.getStartY()};
            double targetEnd[]={t.getDestinationX(),t.getDestinationY()};
            Color targetColor = Color.YELLOW;
            if (t.getIsEvading()) targetColor = Color.ORANGE;
            MyWaypoint w = new MyWaypoint("T", targetColor,new GeoPosition(targetPosition));
            MyWaypoint s = new MyWaypoint("S", Color.MAGENTA,new GeoPosition(targetStart));
            MyWaypoint e = new MyWaypoint("E", Color.BLUE,new GeoPosition(targetEnd));
            waypoints.add(w);
            waypoints.add(s);
            waypoints.add(e);
			//mapViewer.setCenterPosition(new GeoPosition(targetPosition));
		}


		// Visualise UAVs
		for (UAV u: state.getUAVs()){
			double uavPosition[]={u.getLatitude(),u.getLongitude()};
			MyWaypoint w = new MyWaypoint("U", u.getColor(),new GeoPosition(uavPosition));
			waypoints.add(w);
			if (Parameters.visualiseUAVTarget && u.getEstimatedTargetPosition() != null) {
				double tPosition[]={u.getEstimatedTargetPosition().getLatitude(),u.getEstimatedTargetPosition().getLongitude()};
				MyWaypoint wt = new MyWaypoint("T", Color.PINK,new GeoPosition(tPosition));
				waypoints.add(wt);
			}
		}



		// Visualise Cities centroids
		int i=0;
		if (state.getUAVs().get(0).getCentroids() != null && Parameters.visualiseCities){
			for (Position c : state.getUAVs().get(0).getCentroids()){
				double cityPosition[]={c.getLatitude(),c.getLongitude()};
				MyWaypoint w = new MyWaypoint("C"+i, Color.RED,new GeoPosition(cityPosition));
				waypoints.add(w);
				i++;
			}
		}

        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new FancyWaypointRenderer());

		// Visualise Grid
		List<Position> grid = state.getUAVs().get(0).getGrid();
		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

		if (Parameters.visualiseGrid) {
			if (grid != null && grid.size() > 0) {
				SquarePainter routePainter = new SquarePainter(grid);
				painters.add(routePainter);
			}
		}

		if (Parameters.visualiseTargetRoute){
			LinePainter linesPainter = new LinePainter(state.getTargets().get(0).getRoute());
			painters.add(linesPainter);
		}


		// Visualise all roads
		if (Parameters.visualiseAllRoads){
			if (grid!= null && grid.size()>0){
				RoadPainter roadPainter = new RoadPainter(State.getGH().getEdgesAsPairs());
				painters.add(roadPainter);
			}
		}



		// visualise camera and plans
		for (UAV u: state.getUAVs()){
			CameraPainter camera = new CameraPainter(u.getCurrent(), u.getCameraFacing(),u.getRadiusOservation(),u.getCameraSweep()?Parameters.cameraAngleWide:Parameters.cameraAngleNarrow, u.getCameraStatus());
			painters.add(camera);
            if (u.getPlan()!=null && Parameters.visualisePlan) {
                PlanPainter plan = new PlanPainter(u.getPlan(), u.getColor());
                painters.add(plan);
            }
			List<SearchPattern> candidate = u.getCandidates();
			// Visualise candidate search patterns;

			if (candidate != null && Parameters.visualiseSearchPatterns){
				for (SearchPattern c : candidate){
					if (c instanceof SpiralSearchPattern){
						SpiralPainter spiralPainter = new SpiralPainter(c.getOrigin(), ((SpiralSearchPattern) c).getRadius(), u.getColor());
						painters.add(spiralPainter);
					}
				}
			}

			// Visualise Roads
			if (grid!= null && grid.size()>0 && Parameters.visualiseSelectedRoads){
				LinePainter linesPainter = new LinePainter(u.getEdges());
				painters.add(linesPainter);
			}

			List<List<Position>> paths =u.getPaths();

			if (Parameters.visualiseColorRoad && paths!=null){
				PathPainter pathPainter = new PathPainter(paths,Color.BLUE);
				painters.add(pathPainter);
			}
		}


		painters.add(waypointPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		mapViewer.setOverlayPainter(painter);


	}
}
