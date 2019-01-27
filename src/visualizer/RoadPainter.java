package visualizer;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import main.Position;

public class RoadPainter implements Painter<JXMapViewer> {
    private List<main.Pair<Position, Position>> track;

    public RoadPainter(List<main.Pair<Position, Position>> track) {
        this.track = new ArrayList<main.Pair<Position, Position>>(track);

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

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        for (main.Pair<Position, Position> pIt : track) {
            Position p = pIt.first.getOffsetInMeters(main.Parameters.gridXSize*0.5,-main.Parameters.gridYSize*0.5);
            Position pEnd = pIt.second.getOffsetInMeters(main.Parameters.gridXSize*0.5,-main.Parameters.gridYSize*0.5);
            // convert geo-coordinate to world bitmap pixel
            GeoPosition gp = new GeoPosition(p.getLatitude(), p.getLongitude());
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            GeoPosition gpEnd = new GeoPosition(pEnd.getLatitude(), pEnd.getLongitude());
            Point2D ptEnd = map.getTileFactory().geoToPixel(gpEnd, map.getZoom());
            g.drawLine((int) pt.getX(), (int) pt.getY(), (int) ptEnd.getX(), (int) ptEnd.getY());


        }
    }
}
