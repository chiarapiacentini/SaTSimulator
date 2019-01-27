package visualizer;

import main.Position;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Created by chiara on 23/06/2017.
 */
public class PlanPainter implements Painter<JXMapViewer> {
    private List<Position> plan;
    private Color color;

    public PlanPainter(List<Position> plan, Color color) {
        this.plan = plan;
        this.color = color;
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
        g.setStroke(new BasicStroke(2));
        g.setColor(color);

        drawGrid(g, map);


        drawGrid(g, map);

        g.dispose();
    }

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        Position pStart = null;
        for (Position pIt : plan) {
            if (pStart != null) {
                // convert geo-coordinate to world bitmap pixel
                GeoPosition gp = new GeoPosition(pStart.getLatitude(), pStart.getLongitude());
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                GeoPosition gpEnd = new GeoPosition(pIt.getLatitude(), pIt.getLongitude());
                Point2D ptEnd = map.getTileFactory().geoToPixel(gpEnd, map.getZoom());
                g.drawLine((int) pt.getX(), (int) pt.getY(), (int) ptEnd.getX(), (int) ptEnd.getY());
            }
            pStart = pIt;
        }
    }
}
