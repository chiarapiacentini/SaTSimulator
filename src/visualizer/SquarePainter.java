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

public class SquarePainter implements Painter<JXMapViewer> {

    private List<Position> track;
    private int trasparency = 50;

    public SquarePainter(List<Position> track) {
        this.track = new ArrayList<Position>(track);

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

        g.dispose();
    }

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        for (Position p : track) {
            // convert geo-coordinate to world bitmap pixel
            GeoPosition gp = new GeoPosition(p.getLatitude(), p.getLongitude());
            Position newP = p.getOffsetInMeters(main.Parameters.gridXSize, Parameters.gridYSize);
            GeoPosition gpH = new GeoPosition(newP.getLatitude(),newP.getLongitude());
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            Point2D ptH = map.getTileFactory().geoToPixel(gpH, map.getZoom());
            Color color = new Color(0, 0, 255, trasparency);
            if (p.getTerrain() == Position.Terrain.CITY) {
                color = new Color(255, 0, 0, trasparency);
            } else if (p.getTerrain() == Position.Terrain.SUBURBAN) {
                color = new Color(0, 255, 0, trasparency);
            } else if (p.getTerrain() == Position.Terrain.URBAN) {
                color = new Color(127, 127, 0, trasparency);
            } else if (p.getTerrain() == Position.Terrain.ROUGH) {
                color = new Color(0, 127, 127, trasparency);
            }
            if (p.getCity() != -1) {
                int i = p.getCity();
                color = new Color(255, 0, 0,75);
            }
            g.setColor(color);
            //System.out.println(p.getX() + " " + p.getY());

            g.fillRect((int) pt.getX(), (int) pt.getY(), (int) Math.abs(ptH.getX() - pt.getX() - 1), (int) (Math.abs(ptH.getY() - pt.getY()) - 1));


        }
    }
}
