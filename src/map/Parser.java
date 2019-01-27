package map;

import main.Position;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Parser {

    private ArrayList<Position> points;

    public Parser(String nameFile) {
        points = new ArrayList<Position>();
        File file = new File(nameFile);
        Sink sinkImplementation = new Sink() {
            public void process(EntityContainer entityContainer) {
                try {
                    Entity entity = entityContainer.getEntity();
                    if (entity instanceof Node) {
                        //nodes.put(new Long(entity.getId()), (Node) entity);
                        //do something with the node
                        Collection<Tag> c = entity.getTags();
                        for (Tag t : c) {
                            if (t.getKey().equals("place")) {
                                Double lat = new Double(((Node) entity).getLatitude());
                                Double lon = new Double(((Node) entity).getLongitude());
                                Position p = new Position(lat, lon);
                                Position.Terrain tt = Position.Terrain.UNKNOWN;
                                if (t.getValue().equals("suburb")) {
                                    tt = Position.Terrain.SUBURBAN;
                                } else if (t.getValue().equals("city") || t.getValue().equals("neighbourhood")) {
                                    tt = Position.Terrain.CITY;
                                } else if (t.getValue().equals("town") || t.getValue().equals("village") || t.getValue().equals("locality")) {
                                    tt = Position.Terrain.URBAN;
                                } else {
                                    tt = Position.Terrain.ROUGH;
                                }
                                p.setTerrain(tt);
                                points.add(p);
                                //System.out.print(lon.toString()+ " " + lat.toString() + " " + t.getValue()+ "\n");//
                            }
                        }
                    } else if (entity instanceof Way) {
                        //do something with the way
                    } else if (entity instanceof Relation) {
                        //do something with the relation

                    }
                } catch (Exception e) {

                }
            }

            public void release() {
            }

            public void complete() {
            }

            @Override
            public void initialize(Map<String, Object> arg0) {
                // TODO Auto-generated method stub

            }
        };

        boolean pbf = false;
        CompressionMethod compression = CompressionMethod.None;

        if (file.getName().endsWith(".pbf")) {
            pbf = true;
        } else if (file.getName().endsWith(".gz")) {
            compression = CompressionMethod.GZip;
        } else if (file.getName().endsWith(".bz2")) {
            compression = CompressionMethod.BZip2;
        }

        RunnableSource reader;

        try {
            if (pbf) {

                reader = new crosby.binary.osmosis.OsmosisReader(
                        new FileInputStream(file));
                //System.out.println("we are here");

            } else {
                reader = new XmlReader(file, false, compression);
            }

            reader.setSink(sinkImplementation);

            Thread readerThread = new Thread(reader);
            readerThread.start();

            while (readerThread.isAlive()) {
                try {
                    readerThread.join();
                } catch (InterruptedException e) {
                    /* do nothing */
                }
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<Position> getPoints() {
        return points;
    }

    public static void main(String args[]) {
        Parser p = new Parser("resources/scotland-latest.osm.pbf");
        //Parser p = new Parser("/Users/chiara/Work/Code/JavaWorkspace/SaTEvasive/scotland.osm");
        System.out.println("end of file");
        System.out.println("selecting points");
        Position pp = new Position(56.1337538, -3.9617041);

        System.out.println(p.points.size());
        int count = 0;
        long start_time_0 = System.currentTimeMillis();
        for (int i = 0; i < 10000; ++i) {
            count = 0;
            for (Position pos : p.points) {
                if (pp.distance(pos) < 10000) {
                    count++;
                }
            }
        }
        long start_time = System.currentTimeMillis();
        List<Position> close = new ArrayList<Position>();
        for (int i = 0; i < 10000; ++i) {

            close = p.points.stream().filter(x -> x.distance(pp) < 10000).collect(Collectors.toList());
        }
        long end_time = System.currentTimeMillis();
        long execution_time = end_time - start_time;
        long execution_time2 = start_time - start_time_0;

        System.out.println(close.size() + " (" + count + ") found in " + execution_time + " (" + execution_time2 + ") ");

    }
}