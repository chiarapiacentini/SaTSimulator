package visualizer;

import main.Parameters;
import main.Position;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class LinePainter implements Painter<JXMapViewer> {

	private List<main.Pair<Position,List<Position>>> track;
	private double displace;

	public LinePainter(List<main.Pair<Position,List<Position>>> track){
		this.track = new ArrayList<main.Pair<Position,List<Position>>>(track);
		displace = main.Parameters.gridXSize*0.5;
	}

	public LinePainter(Vector<Position> track){
		this.track = new ArrayList<main.Pair<Position,List<Position>>>();
		Position previous = null;
		for (Position p : track){
			if (previous != null){
				ArrayList<Position> l = new ArrayList<Position>();
				l.add(p);
				this.track.add(new main.Pair(previous,l));
			}
			previous = p;
		}
		displace = 0;

	}
	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		// TODO Auto-generated method stub
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// do the drawing
		//g.setColor(Color.BLACK);
		//g.setStroke(new BasicStroke(2));

		drawGrid(g, map);

		// do the drawing again
	
		//g.setStroke(new BasicStroke(5));

		drawGrid(g, map);

		g.dispose();
	}
	private void drawGrid(Graphics2D g, JXMapViewer map)
	{

		for (main.Pair<Position,List<Position>> pIt : track)
		{
			Position p = pIt.first;
			List<Position> adj = pIt.second;
			// convert geo-coordinate to world bitmap pixel
			Position newP = p.getOffsetInMeters(displace,-displace);
			GeoPosition gp = new GeoPosition(newP.getLatitude(),newP.getLongitude());
			Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
			for (Position pEnd : adj){
				Position newEnd = pEnd.getOffsetInMeters(displace,-displace);
				GeoPosition gpEnd = new GeoPosition(newEnd.getLatitude(),newEnd.getLongitude());
				Point2D ptEnd = map.getTileFactory().geoToPixel(gpEnd, map.getZoom());
				//System.out.println(pt + " " + ptEnd);
				g.drawLine((int) pt.getX(), (int) pt.getY(),(int) ptEnd.getX(), (int) ptEnd.getY());
			}

		}
	}
}
