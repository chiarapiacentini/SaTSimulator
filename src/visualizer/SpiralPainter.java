package visualizer;

import main.Parameters;
import main.Position;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

/**
 * Created by chiarapiacentini on 2017-04-15.
 */
public class SpiralPainter implements Painter<JXMapViewer> {

    Position origin;
    Double radius;
    private int trasparency = 50;
    Color color;
    public SpiralPainter(Position o, Double r, Color c) {
        origin = o;
        radius = r;
        color = new Color(c.getRed(),c.getGreen(),c.getBlue(),trasparency);
        //color = color.brighter();
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        // TODO Auto-generated method stub
        g = (Graphics2D) g.create();
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawGrid(g, map);
        drawGrid(g, map);
        g.dispose();
    }

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        // convert geo-coordinate to world bitmap pixel
        Position origin = this.origin.getOffsetInMeters( 0,0);//Parameters.gridYSize*0.5,-Parameters.gridYSize*0.5);
        Position tmp = origin.getOffsetInMeters( -radius,radius);
        Position tmp2 = origin.getOffsetInMeters( radius,-radius);
        GeoPosition gpH = new GeoPosition(tmp.getLatitude(), tmp.getLongitude());
        GeoPosition gpH2 = new GeoPosition(tmp2.getLatitude(), tmp2.getLongitude());
        Point2D pt = map.getTileFactory().geoToPixel(gpH, map.getZoom());
        Point2D ptH = map.getTileFactory().geoToPixel(gpH2, map.getZoom());
        //Color color = new Color(255, 0, 0, trasparency);

        g.setColor(color);

        g.fillOval((int) pt.getX(), (int) pt.getY(), (int) Math.abs(ptH.getX() - pt.getX()), (int) Math.abs(ptH.getY() - pt.getY()));


    }

}