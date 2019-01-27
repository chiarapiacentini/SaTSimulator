package visualizer;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple sample application that shows
 * a OSM map of Europe
 *
 * @author Martin Steiger
 * @author Chiara Piacentini
 */
public class Sample4 {
    /**
     * @param args the program args (ignored)
     */
    public static void main(String[] args) {
        // Create a TileFactoryInfo for Virtual Earth
        TileFactoryInfo info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);

        // Setup local file cache
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

        // Setup JXMapViewer
        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        double scotland[] = {56.652, -4.4};
        GeoPosition frankfurt = new GeoPosition(scotland);
        GeoPosition wiesbaden = new GeoPosition(50, 5, 0, 8, 14, 0);
        GeoPosition mainz = new GeoPosition(50, 0, 0, 8, 16, 0);
        GeoPosition darmstadt = new GeoPosition(49, 52, 0, 8, 39, 0);
        GeoPosition offenbach = new GeoPosition(50, 6, 0, 8, 46, 0);

        // Set the focus
        mapViewer.setZoom(10);
        mapViewer.setAddressLocation(frankfurt);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create waypoints from the geo-positions
        Set<MyWaypoint> waypoints = new HashSet<MyWaypoint>(Arrays.asList(
                new MyWaypoint("F", Color.ORANGE, frankfurt),
                new MyWaypoint("W", Color.CYAN, wiesbaden),
                new MyWaypoint("M", Color.GRAY, mainz),
                new MyWaypoint("D", Color.MAGENTA, darmstadt),
                new MyWaypoint("O", Color.GREEN, offenbach)));

        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<MyWaypoint>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(new FancyWaypointRenderer());

        mapViewer.setOverlayPainter(waypointPainter);

        // Display the viewer in a JFrame
        JFrame frame = new JFrame("JXMapviewer2 Example 4");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
