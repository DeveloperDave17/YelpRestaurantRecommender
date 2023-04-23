package GraphCreator;

import Loader.Bin;
import Loader.ClusterReader;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

public class Graph {
    HashMap<String,Node> nodes;

    ArrayList<Node> nodeList;

    /**
     * The graph constructor initializes the node hashmap and node arraylist.
     */
    public Graph() {
        nodes = new HashMap<>();
        nodeList = new ArrayList<>();

    }

    /**
     * Unions two disjoint sets by setting the smaller disjoint sets root to the larger root and adding the size of the
     * smaller set to the size of the larger set.
     * @param x Any node
     * @param y A different node
     */
    private void unionDisjointSets(Node x, Node y){
        Node disjointSet1 = x.findDisjointSet(); Node disjointSet2 = y.findDisjointSet();
        if (disjointSet1 != disjointSet2){
            if(disjointSet1.disjointSetSize < disjointSet2.disjointSetSize){
                disjointSet1.disjointSet = disjointSet2;
                disjointSet2.disjointSetSize += disjointSet1.disjointSetSize;
            } else{
                disjointSet2.disjointSet = disjointSet1;
                disjointSet1.disjointSetSize += disjointSet2.disjointSetSize;
            }
        }
    }

    /**
     * Adds the business to the node hashmap and arraylist
     * @param businessName
     */
    public void addNode(String businessName){
        nodes.put(businessName, new Node(businessName));
        nodeList.add(nodes.get(businessName));
    }

    /**
     * Adds an edge from src to dst, associates a weight, and unions the disjoint sets of src and dst.
     * @param src
     * @param dst
     * @param distance
     */
    public void addEdge(String src, String dst, double distance){
        Node source = nodes.get(src);
        Node destination = nodes.get(dst);
        source.addEdge(destination, distance);
        unionDisjointSets(source, destination);
    }

    /**
     * A method that allows the user to figure out how many disjoint sets exist in the graph.
     * @return
     */
    public int numDisjointSets(){
        Set<Node> uniqueSets = new HashSet<>();
        for(Node current: nodeList){
            uniqueSets.add(current.findDisjointSet());
        }

        return uniqueSets.size();
    }

    /**
     * A method that creates a list of all the disjoint sets of businesses names, accomplishes the creation of sets by
     * checking if a nodes disjointset is already created then adds the business to it, if not then it creates the set
     * adding the disjointset root and the business.
     * @return A list of all the disjointsets of nodes
     */
    public List<Set<String>> disjointSets(){
        List<Set<String>> uniqueSets = new ArrayList<>();

        for(Node current: nodeList){
            boolean exists = false;
            for(Set<String> set: uniqueSets){
                if(set.contains(current.findDisjointSet().businessName)){
                    set.add(current.findDisjointSet().businessName);
                    set.add(current.businessName);
                    exists = true;
                    break;
                }
            }

            if(!exists){
                uniqueSets.add(new HashSet<>());
                uniqueSets.get(uniqueSets.size()-1).add(current.findDisjointSet().businessName);
                uniqueSets.get(uniqueSets.size()-1).add(current.businessName);
            }
        }

        return uniqueSets;
    }

    /**
     *  Writes the created graph to a file named businessGraph, assumes that business names take up no larger than
     *  80 bytes worth of space in UTF_8.
     */
    public void writeGraph() throws IOException {
        try(RandomAccessFile graphFile = new RandomAccessFile("businessGraph", "rw");
            FileChannel graphChannel = graphFile.getChannel()){
            ByteBuffer graphBuffer = ByteBuffer.allocate(4);
            graphBuffer.putInt(nodeList.size());
            graphBuffer.position(0);
            graphChannel.write(graphBuffer);
            for (Node node: nodeList){
                graphBuffer = ByteBuffer.allocate(432);
                graphBuffer.limit(80);
                graphBuffer.put(node.businessName.getBytes(StandardCharsets.UTF_8));

                // Storing all the edges
                for (int i = 1; i <= 4; i++){
                    graphBuffer.limit((i * 88) + 72);
                    graphBuffer.position(i * 88 - 8);
                    graphBuffer.put(node.edges.get(i - 1).dst.businessName.getBytes(StandardCharsets.UTF_8));
                    graphBuffer.limit((i * 88) + 80);
                    graphBuffer.position((i * 88 ) + 72);
                    graphBuffer.putDouble(node.edges.get(i - 1).distance);
                }

                graphBuffer.position(0);
                graphChannel.write(graphBuffer);
            }
        }
    }

    /**
     * Reads a graph from disk that is found at the graph channel provided.
     * @param graphChannel A channel to graphs file.
     */
    public void readGraph(FileChannel graphChannel) throws IOException {
        ByteBuffer graphBuffer = ByteBuffer.allocate(4);
        graphChannel.read(graphBuffer);
        graphBuffer.position(0);
        int size = graphBuffer.getInt();

        List<String> edgeDestinations = new ArrayList<>();
        List<Double> edgeDistances = new ArrayList<>();

        for (int i = 0; i < size; i++){
            graphBuffer = ByteBuffer.allocate(432);
            graphBuffer.limit(80);
            graphChannel.read(graphBuffer);
            graphBuffer.position(0);

            byte[] node = new byte[80];
            graphBuffer.get(node);
            addNode(new String(node, StandardCharsets.UTF_8).replace("\0", ""));

            for (int edgeNum = 1; edgeNum<= 4; edgeNum++) {
                graphBuffer.limit((edgeNum * 88) + 72);
                graphBuffer.position(edgeNum * 88 - 8);
                graphChannel.read(graphBuffer);
                graphBuffer.position(edgeNum * 88 - 8);
                byte[] edgeDest = new byte[80];
                graphBuffer.get(edgeDest);

                edgeDestinations.add(new String(edgeDest, StandardCharsets.UTF_8).replace("\0", ""));


                graphBuffer.limit((edgeNum * 88) + 80);
                graphBuffer.position((edgeNum * 88 ) + 72);
                graphChannel.read(graphBuffer);
                graphBuffer.position((edgeNum * 88 ) + 72);
                edgeDistances.add(graphBuffer.getDouble());
            }

        }

        for (int i = 0; i < nodeList.size(); i++){
            for (int edgeNum = 0; edgeNum < 4; edgeNum++){
                addEdge(nodeList.get(i).businessName,edgeDestinations.get(4 * i + edgeNum), edgeDistances.get(4 * i + edgeNum));
                addEdge(edgeDestinations.get(4 * i + edgeNum), nodeList.get(i).businessName, edgeDistances.get(4 * i + edgeNum));
            }
        }

    }

    /**
     * Takes all the cluster centers and routinely checks using dijkstra's algorithm ( breadth-first search with a
     * priority que) whether one of the centers has been reached.
     * @param root The source node of the path.
     * @param destination1 Cluster center 1
     * @param destination2 Cluster center 2
     * @param destination3 Cluster center 3
     * @param destination4 Cluster center 4
     * @param destination5 Cluster center 5
     * @return A string containing the entire path from source to destination.
     */
    String buildShortestPathTree(Node root, Node destination1, Node destination2, Node destination3, Node destination4, Node destination5){
        PQ pq = new PQ(nodeList, root);
        Node p;

        while ((p = pq.poll()) != null){
            if (p == destination1 || p == destination2 || p == destination3 || p == destination4 || p == destination5) break;
            for (Edge e : p.edges){
                Node s = p, d = e.dst;
                double w = s.best + e.distance;
                if (w < d.best) {
                    d.parent = s;
                    d.best = w;
                    pq.resift(d);
                }
            }
        }

        List<String> paths = new ArrayList<>();

        while (p != null){
            Node dest = p;
            p = p.parent;
            if (p != null) {
                paths.add( " -> " + dest.businessName);
            }else {
                paths.add(dest.businessName);
            }
        }

        StringBuilder solution = new StringBuilder();
        for ( int i = paths.size(); i > 0; i--){
            solution.append(paths.get(i - 1));
        }

        return solution.toString();

    }

    /**
     * Handles all the necessary preprocessing before running the shortest path algorithm, also checking before running
     * the algorithm is the node can reach a cluster center.
     * @param businessName Source of the path
     * @return A string containing either the shortest path to a cluster center or no result.
     */
    public String shortestPath(String businessName) throws IOException {
        Node src = nodes.get(businessName);

        List<String> bar = ClusterReader.readCluster("Bar");
        List<String> american = ClusterReader.readCluster("American");
        List<String> breakfast = ClusterReader.readCluster("Breakfast");
        List<String> italian = ClusterReader.readCluster("Italian");
        List<String> asian = ClusterReader.readCluster("Asian");

        Node barCenter = nodes.get(bar.get(0));
        Node americanCenter = nodes.get(american.get(0));
        Node breakfastCenter = nodes.get(breakfast.get(0));
        Node italianCenter = nodes.get(italian.get(0));
        Node asianCenter = nodes.get(asian.get(0));

        List<Set<String>> disjointSets = disjointSets();
        Set<String> disjointSet = new HashSet<>();

        for (Set<String> set: disjointSets){
            if(set.contains(businessName)){
                disjointSet = set;
                break;
            }
        }

        if (disjointSet.contains(barCenter.businessName) || disjointSet.contains(americanCenter.businessName)
         || disjointSet.contains(breakfastCenter.businessName) || disjointSet.contains(italianCenter.businessName)
         || disjointSet.contains(asianCenter.businessName)){
            return buildShortestPathTree(src, barCenter, americanCenter, breakfastCenter, italianCenter, asianCenter);
        } else{
            return "A cluster center is not reachable from " + businessName;
        }


    }

}



class Node implements Comparable<Node> {
    String businessName;
    List<Edge> edges;
    double best;
    int pqIndex;
    Node disjointSet;
    int disjointSetSize;
    Node parent;

    /**
     * Constructor for the node class, stores the business name initializes the set of edges and calls the
     * initializeDisjointSet method.
     * @param businessName The business name that is desired to be stored.
     */
    Node (String businessName){
        this.businessName = businessName;
        edges = new ArrayList<>();
        initializeDisjointSet();
    }

    /**
     * Sets the disjoint set of the node to the current node and its disjointSetSize to 1.
     */
    void initializeDisjointSet(){
        disjointSet = this;
        disjointSetSize = 1;
    }

    /**
     * Finds the disjoint set by setting the node equal to the disjointset of the current node until the current node is
     * equal to its disjointset. Then compresses all the nodes along the path to the root node.
     * @return the root of a disjointset.
     */
    Node findDisjointSet(){
        Node d = disjointSet, t = d;
        while(t != (t = t.disjointSet));

            // Compression
        while(d != t) {
            Node p = d.disjointSet;
            d.disjointSet = t;
            d = p;
        }

        return t;
    }

    /**
     * Checks to see if the edge already exists, if not then adds the edge to the node.
     * @param dst The edges destination node.
     * @param distance the weighted distance associated with the edge.
     */
    void addEdge(Node dst, double distance){
        for (Edge edge: edges){
            if (edge.dst == dst){
                return;
            }
        }
        edges.add(new Edge(dst,distance));
    }

    public int compareTo(Node x) { return Double.compare(best, x.best);}
}

class Edge implements Comparable<Edge>{
    Node dst;
    double distance;

    Edge(Node dst, double distance){
        this.dst = dst;
        this.distance = distance;
    }

    public int compareTo(Edge e) {return Double.compare(distance, e.distance);}
}

class PQ {
    final Node[] array;
    int size;
    static int leftOf(int k) { return (k << 1) + 1;}
    static int rightOf(int k) { return leftOf(k) + 1;}
    static int parentOf(int k) { return (k - 1) >>> 1;}
    PQ(Collection<Node> nodes, Node root) {
        array = new Node[nodes.size()];
        root.best = 0;
        root.pqIndex = 0;
        array[0] = root;
        int k = 1;
        for (Node p: nodes) {
            p.parent = null;
            if (p != root) {
                p.best = Double.MAX_VALUE;
                array[k] = p; p.pqIndex = k++;
            }
        }
        size = k;
    }

    void resift(Node x) {
        int k = x.pqIndex;
        assert(array[k] == x);
        while (k > 0) {
            int parent = parentOf(k);
            Node p = array[parent];
            if (x.compareTo(p) >= 0)
                break;
            array[k] = p; p.pqIndex = k;
            k = parent;
        }
        array[k] = x; x.pqIndex = k;
    }

    void add(Node x){
        x.pqIndex = size++;
        resift(x);
    }

    Node poll() {
        int n = size;
        if (n == 0) return null;
        Node least = array[0];
        if(least.best == Double.MAX_VALUE) return null;
        size = --n;
        if (n > 0) {
            Node x = array[n]; array[n] = null;
            int k = 0, child;
            while ((child = leftOf(k)) < n) {
                Node c = array[child];
                int right = child + 1;
                if (right < n){
                    Node r = array[right];
                    if (c.compareTo(r) > 0){
                        c = r;
                        child = right;
                    }
                }
                if ( x.compareTo(c) <= 0)
                    break;
                array[k] = c; c.pqIndex = k;
                k = child;
            }
            array[k] = x; x.pqIndex = k;
        }
        return least;
    }

}

