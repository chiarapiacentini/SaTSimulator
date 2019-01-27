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

public class PathPainter implements Painter<JXMapViewer> {
    private List<List<Position>> track;
    Color color;

    public PathPainter(List<List<Position>> track, Color color) {
        this.track = new ArrayList<List<Position>>(track);
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
        g.setColor(color);
        g.setStroke(new BasicStroke(1));

        drawGrid(g, map);

        // do the drawing again

        //g.setStroke(new BasicStroke(5));

        drawGrid(g, map);

        g.dispose();
    }

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        //int nTracks = track.size();
        //int nTracksPerSize = (int)(Parameters.uavEvading*nTracks);
        //System.out.println(nTracks + " " + nTracksPerSize);
        //int iTrack = 0;
        int iStrock = 1;
        for (List<Position> pIt : track) {
            Position ppEnd = null;
            for (Position pp : pIt) {
                if (ppEnd != null) {
                    Position p = pp.getOffsetInMeters(main.Parameters.gridXSize*0.5,-main.Parameters.gridYSize*0.5);;
                    Position pEnd = ppEnd.getOffsetInMeters(main.Parameters.gridXSize*0.5,-main.Parameters.gridYSize*0.5);;
                    // convert geo-coordinate to world bitmap pixel
                    GeoPosition gp = new GeoPosition(p.getLatitude(), p.getLongitude());
                    Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                    GeoPosition gpEnd = new GeoPosition(pEnd.getLatitude(), pEnd.getLongitude());
                    Point2D ptEnd = map.getTileFactory().geoToPixel(gpEnd, map.getZoom());
                    g.drawLine((int) pt.getX(), (int) pt.getY(), (int) ptEnd.getX(), (int) ptEnd.getY());
                }
                ppEnd = pp;
            }
            //if (iTrack % nTracksPerSize == 0 ){
            //    g.setStroke(new BasicStroke(iStrock));
            //    iStrock++;
            //}
            //iTrack++;


        }
    }
}
