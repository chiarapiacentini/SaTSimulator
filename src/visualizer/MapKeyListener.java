package visualizer;

import org.jxmapviewer.input.PanKeyListener;
import main.Parameters;
import org.jxmapviewer.JXMapViewer;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Parameter;

/**
 * Created by chiarapiacentini on 2017-05-22.
 */
public class MapKeyListener extends PanKeyListener {
    JFrame frame;
    public MapKeyListener(JXMapViewer viewer, JFrame frame) {
        super(viewer);
        this.frame = frame;
    }
    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
    }

    @Override
    public void keyTyped(KeyEvent e){
        switch (e.getKeyChar())
        {
            case 'g':
                Parameters.visualiseGrid = !Parameters.visualiseGrid;
                break;
            case 'r':
                Parameters.visualiseSelectedRoads = !Parameters.visualiseSelectedRoads;
                break;
            case 't':
                Parameters.visualiseMoveMap = !Parameters.visualiseMoveMap;
                break;
            case 'T':
                Parameters.visualiseTargetRoute = !Parameters.visualiseTargetRoute;
                break;
            case 'c':
                Parameters.visualiseCities = !Parameters.visualiseCities;
                break;
            case 's':
                Parameters.visualiseSearchPatterns = !Parameters.visualiseSearchPatterns;
                break;
            case 'p':
                Parameters.visualisePlan = !Parameters.visualisePlan;
                break;
            case '>':
                if (Parameters.scaleTime > 1 )
                    Parameters.scaleTime=Parameters.scaleTime/2;
                break;
            case '<':
                Parameters.scaleTime=Parameters.scaleTime*2;
                break;
            case '?':
                Parameters.scaleTime=50;
                break;
            case '.':
                Parameters.scaleTime=10000;
                break;

        }

    }
}
