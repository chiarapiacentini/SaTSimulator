package visualizer;

import main.UAV.CameraStatus;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import main.Position;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

/**
 * Created by chiarapiacentini on 2017-05-09.
 */
public class CameraPainter implements Painter<JXMapViewer> {

    private Position position;
    private double direction;
    private double radius;
    private double angle;
    private CameraStatus status;

    public CameraPainter(Position p, double direction, double radius, double angle, CameraStatus status) {
        position = p;
        this.direction = direction;
        this.radius = radius;
        this.angle = angle;
        this.status = status;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int transparency = 100;
        if (status == CameraStatus.BLIND)
            transparency = 220;
        else if (status == CameraStatus.CLEAR)
            transparency = 50;
        Color color = new Color(125, 125, 125,transparency);
        g.setColor(color);
        drawCamera(g, map);
        g.dispose();
    }

    private void drawCamera(Graphics2D g, JXMapViewer map){
        GeoPosition gp = new GeoPosition(position.getLatitude(),position.getLongitude());
        Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

        // distance radius
        Position cameraStart = position.getOffsetInMeters(-radius,radius);
        GeoPosition gpCameraStart = new GeoPosition(cameraStart.getLatitude(), cameraStart.getLongitude());
        Point2D ptCameraStart = map.getTileFactory().geoToPixel(gpCameraStart,map.getZoom());
        int w = (int) (2*Math.abs(ptCameraStart.getX()-pt.getX()));
        int angleStart = (int) (direction + 90 - angle*0.5);//(angle== Parameters.cameraAngleWide?0:angle * 0.5));
        Arc2D camera = new Arc2D.Double(ptCameraStart.getX(), ptCameraStart.getY(),w, w, angleStart, angle, Arc2D.PIE);
        g.fill(camera);
    }

}