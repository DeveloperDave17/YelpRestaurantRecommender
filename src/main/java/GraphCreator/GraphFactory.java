package GraphCreator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class GraphFactory {

    /**
     * Loads the restaurant graph that the GraphCreator writes to disk.
     * @return A graph with 10,000 Restaurants
     */
    public static Graph loadGraph() throws IOException {
        try(RandomAccessFile graphFile = new RandomAccessFile("businessGraph", "r");
            FileChannel graphChannel = graphFile.getChannel()) {
            Graph businessGraph = new Graph();
            businessGraph.readGraph(graphChannel);
            return businessGraph;
        }
    }
}
