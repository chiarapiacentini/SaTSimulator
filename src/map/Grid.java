package map;

import main.Position;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    double minX;
    double minY;
    double maxX;
    double maxY;
    public List<CellGrid> grid;

    public class CellGrid {
        Position position;
        List<CellGrid> neighbours;

        CellGrid(Position p) {
            neighbours = new ArrayList<CellGrid>();
            position = p;
        }

        void setNeighbour(Position p) {
            if (p.getLatitude() < minX)
                return;
            if (p.getLatitude() > maxX)
                return;
            if (p.getLongitude() < minY)
                return;
            if (p.getLongitude() > maxY)
                return;
            CellGrid n = new CellGrid(p);
            n.setNeighbour(position);
            //neighbour.
        }

        List<CellGrid> getNeighbours() {
            return neighbours;
        }

        Position getPosition() {
            return position;
        }

        public String toString() {
            String toReturn = "node (" + position.getLatitude() + ", " + position.getLongitude() + ") : ";
            for (CellGrid c : neighbours) {
                toReturn += " (" + c.position.getLatitude() + "," + c.position.getLongitude() + ") - ";
            }
            //toReturn += " -----" +  neighbours.get(0).getNeighbours().get(0).getPosition().getX();
            toReturn += " ----- " + neighbours.get(0).getNeighbours().size();
            toReturn += " ----- " + hashCode();
            return toReturn;
        }

        @Override
        public int hashCode() {
            return (int) Math.floor(position.getLongitude() * (maxY - minX) + position.getLongitude());
        }
    }

    public Grid(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        grid = new ArrayList<CellGrid>();
        for (double x = minX; x <= maxX; x += 2) {
            for (double y = minY; y <= maxY; y += 2) {
                Position p = new Position(x, y);
                CellGrid cg = new CellGrid(new Position(x, y));
                //CellGrid cg = new CellGrid(new Position(x,y));
                cg.setNeighbour(new Position(p.getLatitude() + 2, p.getLongitude()));
                cg.setNeighbour(new Position(p.getLatitude() - 2, p.getLongitude()));
                cg.setNeighbour(new Position(p.getLatitude(), p.getLongitude() - 2));
                cg.setNeighbour(new Position(p.getLatitude(), p.getLongitude() + 2));
                grid.add(cg);

            }
        }
    }

    public static void main(String args[]) {
        Grid grid = new Grid(0, 10, 0, 10);
        for (CellGrid c : grid.grid) {
            System.out.println(c);
        }
    }
}
