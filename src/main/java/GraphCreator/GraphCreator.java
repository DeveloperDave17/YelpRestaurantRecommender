package GraphCreator;

import Loader.BusinessFileData;
import Loader.ExtendibleHashTable;
import Loader.ExtendibleHashTableFactory;
import Loader.FreqHTfactory;
import com.yelp.restaurantFinder.Business;
import com.yelp.restaurantFinder.FreqHT;
import com.yelp.restaurantFinder.GsonDataRetriever;
import com.yelp.restaurantFinder.Review;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static Loader.LoaderApplication.poolUniqueWords;

public class GraphCreator {

    public static void main(String[] args) throws IOException {

        Graph businessGraph = new Graph();

        List<Business> businesses = GsonDataRetriever.getBusinessList();
        HashMap<String, Business> businessMap = GsonDataRetriever.getBusinessHashMap();
        List<Review> reviews = GsonDataRetriever.getReviewList(businessMap);
        Set<String> uniqueWords = poolUniqueWords(reviews);

        ExtendibleHashTable eht = ExtendibleHashTableFactory.createExtendibleHashTable();

        for (Business targetBusiness : businesses) {
            businessGraph.addNode(targetBusiness.getName());
        }

        for (Business targetBusiness : businesses) {

            final int EDGE_COUNT = 4;
            List<String> connections = new ArrayList<>();
            List<Double> connectionDistances = new ArrayList<>();

            for (int i = 0; i < EDGE_COUNT; i++) {
                connections.add("");
                connectionDistances.add(Double.MAX_VALUE);
            }

            for (Business possibleConnection : businesses) {

                // Ensuring a business doesn't have an edge pointing to itself.
                if (targetBusiness.equals(possibleConnection)) {
                    continue;
                }

                double distance = haversineDistance(targetBusiness, possibleConnection);

                // Checking if the connection needs to replace an existing connection
                for (int i = 0; i < EDGE_COUNT; i++) {
                    if (distance < connectionDistances.get(i)) {
                        // Moving all the greater possible connections up in the list excluding the last
                        for (int j = EDGE_COUNT - 1; j > i; j--) {
                            connections.set(j, connections.get(j - 1));
                            connectionDistances.set(j, connectionDistances.get(j - 1));
                        }
                        connections.set(i, possibleConnection.getName());
                        connectionDistances.set(i, distance);
                        break;
                    }
                }
            }


            for (int i = 0; i < EDGE_COUNT; i++) {

                double inverseCosineDistance = 1 / cosineSim(getFreqHT(targetBusiness.getName(),eht),getFreqHT(connections.get(i),eht),uniqueWords);
                businessGraph.addEdge(targetBusiness.getName(), connections.get(i), inverseCosineDistance);
                businessGraph.addEdge(connections.get(i), targetBusiness.getName(), inverseCosineDistance);
            }

        }

        businessGraph.writeGraph();

        System.out.println(businessGraph.numDisjointSets());


    }


    public static double haversineDistance(Business business1, Business business2) {
        double RADIUS_OF_EARTH = 6378100; // in meters
        return 2 * RADIUS_OF_EARTH * Math.asin(
                Math.sqrt(
                        Math.pow(Math.sin((business2.getLatitude() - business1.getLatitude()) / 2), 2)
                                + (Math.cos(business1.getLatitude()) * Math.cos(business2.getLatitude())
                                * Math.pow(Math.sin((business2.getLongitude() - business1.getLongitude()) / 2), 2))
                ));

    }

    public static double cosineSim(FreqHT businessA, FreqHT businessB, Set<String> uniqueWords) {

        double magnitudeOfA = 1;
        double magnitudeOfB = 1;
        double numerator = 1;

        for (String word : uniqueWords) {
            // The numerator consists of a summation of all the word counts for A and B multiplied together for each word
            if (businessA.contains(word) && businessB.contains(word))
                numerator = numerator + (businessA.getCount(word) * businessB.getCount(word));

            // Magnitude of A consists of the summation of each word count for A squared
            if (businessA.contains(word))
                magnitudeOfA = magnitudeOfA + Math.pow(businessA.getCount(word), 2);

            // Magnitude of B consists of the summation of each word count for B squared
            if (businessB.contains(word))
                magnitudeOfB = magnitudeOfB + Math.pow(businessB.getCount(word), 2);
        }

        return numerator / (Math.sqrt(magnitudeOfA) * Math.sqrt(magnitudeOfB));
    }

    public static FreqHT getFreqHT(String businessName, ExtendibleHashTable eht) throws IOException {
        List<BusinessFileData> businesses = eht.getBinsInfo(eht.getBin(businessName));
        for (BusinessFileData businessFileData : businesses) {
            if (businessFileData.getBusinessName().equals(businessName)) {
                return FreqHTfactory.loadFreqHT(businessFileData.getBusinessFileName());
            }
        }

        return new FreqHT();
    }
}
