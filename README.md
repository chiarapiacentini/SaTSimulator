# SaTSimulator

Simulator for Search and Track with UAVs. For more information visit our (WikiPage)[https://graphhopper.com]

## Dependencies

The simulator is written in JAVA1.8. Before compiling make sure to have the Java machine installed on your computer.

The simulators depends on some open-source external libraries. For your convenience the necessary jar files are all in the ``libs`` folder:

- [Graphhopper](https://graphhopper.com): routing libraries
- [JMapViewer](https://wiki.openstreetmap.org/wiki/JMapViewer): Java component which allows to easily integrate an Open Street Map (OSM) view into a Java application

In addition, the simulator requires an OSM map. You can download the map of Scotland [here](https://www.dropbox.com/s/bb9yosnqyjl9efw/scotland-latest.osm.pbf.zip?dl=0). Unzip the file and copy in the ``resources``

## Run the Simulator

### Method 1: Open the project using [IntelliJ](https://intellij-support.jetbrains.com/)

1. Open IntelliJ
2. Click ``Open``
3. Navigate to the folder of the source code and click ``open``
4. If necessary, import the libraries in the ``libs`` folder using ``Files>Project Structure..>Libraries>Add``
5. Build
6. Run

### Method 2: Compile and run Manually

1. Open a terminal
2. Navigate to the folder containing the project
3. Compile

```
javac -classpath "$(pwd)"/src/:"$(pwd)/libs/*":"$(pwd)/libs/osmosis-latest/lib/default/*"  src/main/MainAnimation.java

```

4. Run

```
java -classpath "$(pwd)"/src/:"$(pwd)/libs/*":"$(pwd)/libs/osmosis-latest/lib/default/*"  main.MainAnimation

```


