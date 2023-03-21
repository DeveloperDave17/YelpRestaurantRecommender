package com.yelp.restaurantFinder;

import Loader.ClusterReader;
import Loader.ExtendibleHashTable;
import Loader.FreqHTfactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ClusterFinder {

    /**
     * Finds the associated cluster of the business and the most similar ( based on cosine similarity ) business within
     * the cluster, returns them both within an output string.
     * @param businessName name of the target business
     * @param uniqueWords All the unique words tied to review data
     * @param eht The extensible hashtable mapping business to their file names.
     * @return A string that contains the cluster of a business and its most similar business within the cluster
     * @throws IOException
     */
    public static String find(String businessName, Set<String> uniqueWords, ExtendibleHashTable eht) throws IOException {


        List<String> bar = ClusterReader.readCluster("Bar");
        List<String> american = ClusterReader.readCluster("American");
        List<String> breakfast = ClusterReader.readCluster("Breakfast");
        List<String> italian = ClusterReader.readCluster("Italian");
        List<String> asian = ClusterReader.readCluster("Asian");

        FreqHT businessFreqTable = FreqHTfactory.loadFreqHT(eht.getBusinessNameToFileName(eht.getBin(businessName)).get(businessName));
        double highestCosSim = 0;
        String mostSimilar = "";

        if (bar.contains(businessName)) {

            bar.remove(businessName);

            for(String business: bar){
                double cosSim = cosineSim(businessFreqTable, FreqHTfactory.loadFreqHT(eht.getBusinessNameToFileName(eht.getBin(business)).get(business)), uniqueWords);
                if (cosSim > highestCosSim){
                    highestCosSim = cosSim;
                    mostSimilar = business;
                }
            }

            return businessName + " belongs to the Bar Cuisine Cluster and the most similar restaurant within this cluster is " + mostSimilar;

        } else if(american.contains(businessName)) {
            american.remove(businessName);

            for(String business: american){
                double cosSim = cosineSim(businessFreqTable, FreqHTfactory.loadFreqHT(eht.getBusinessNameToFileName(eht.getBin(business)).get(business)), uniqueWords);
                if (cosSim > highestCosSim){
                    highestCosSim = cosSim;
                    mostSimilar = business;
                }
            }

            return businessName + " belongs to the American Cuisine Cluster and the most similar restaurant within this cluster is " + mostSimilar;
        } else if(breakfast.contains(businessName)) {
            breakfast.remove(businessName);

            for(String business: breakfast){
                double cosSim = cosineSim(businessFreqTable, FreqHTfactory.loadFreqHT(eht.getBusinessNameToFileName(eht.getBin(business)).get(business)), uniqueWords);
                if (cosSim > highestCosSim){
                    highestCosSim = cosSim;
                    mostSimilar = business;
                }
            }

            return businessName + " belongs to the Breakfast Cuisine Cluster and the most similar restaurant within this cluster is " + mostSimilar;
        } else if(italian.contains(businessName)) {
            italian.remove(businessName);

            for(String business: italian){
                double cosSim = cosineSim(businessFreqTable, FreqHTfactory.loadFreqHT(eht.getBusinessNameToFileName(eht.getBin(business)).get(business)), uniqueWords);
                if (cosSim > highestCosSim){
                    highestCosSim = cosSim;
                    mostSimilar = business;
                }
            }

            return businessName + " belongs to the Italian Cuisine Cluster and the most similar restaurant within this cluster is " + mostSimilar;
        } else {
            asian.remove(businessName);

            for(String business: asian){
                double cosSim = cosineSim(businessFreqTable, FreqHTfactory.loadFreqHT(eht.getBusinessNameToFileName(eht.getBin(business)).get(business)), uniqueWords);
                if (cosSim > highestCosSim){
                    highestCosSim = cosSim;
                    mostSimilar = business;
                }
            }

            return businessName + " belongs to the Asian Cuisine Cluster and the most similar restaurant within this cluster is " + mostSimilar;
        }

    }

    /**
     * Calculates the cosine similarity between two businesses utilizing already constructed frequency tables.
     * @param businessA the first business's frequency table
     * @param businessB the second business's frequency table
     * @param uniqueWords all the unique words associated with review data
     * @return a cosine similarity metric
     */
    public static double cosineSim(FreqHT businessA, FreqHT businessB, Set<String> uniqueWords){
        double magnitudeOfA = 1;
        double magnitudeOfB = 1;
        double numerator = 0;

        for (String word: uniqueWords){
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
}
