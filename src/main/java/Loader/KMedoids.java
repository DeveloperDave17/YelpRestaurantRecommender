package Loader;

import com.yelp.restaurantFinder.Business;
import com.yelp.restaurantFinder.FreqHT;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KMedoids {

    private Set<String> uniqueWords;
    private ExtendibleHashTable eht;
    private List<Business> businesses;
    private List<Business> bars;
    private List<Business> american;
    private List<Business> breakfast;
    private List<Business> italian;
    private List<Business> asianCuisine;
    private List<Business> barCluster;
    private List<Business> americanCluster;
    private List<Business> breakfastCluster;
    private List<Business> italianCluster;
    private List<Business> asianCuisineCluster;



    /**
     * Creates a KMedoids object with everything necessary to run the simulations
     * Possible medoids are put into 5 key categories: bars, american, breakfast, italian, and asianCuisine.
     */
    public KMedoids(ExtendibleHashTable eht, List<Business> businesses, Set<String> uniqueWords){
       this.eht = eht;
       this.uniqueWords = uniqueWords;
       this.businesses = businesses;

       bars = new ArrayList<>();
       american = new ArrayList<>();
       breakfast = new ArrayList<>();
       italian = new ArrayList<>();
       asianCuisine = new ArrayList<>();


       for (Business business: businesses){
           for (String category: business.getCategories()){
               if(category.contains("Bars")){
                   if(!bars.contains(business)){
                       bars.add(business);
                   }
               }else if(category.contains("American")){
                   if(!american.contains(business)){
                       american.add(business);
                   }
               }else if(category.contains("Breakfast")){
                   if(!breakfast.contains(business)){
                       breakfast.add(business);
                   }
               }else if(category.contains("Italian")){
                   if(!italian.contains(business)){
                       italian.add(business);
                   }
               }else if(category.contains("Asian") || category.contains("Chinese") || category.contains("Korean") || category.contains("Japanese")){
                   if(!asianCuisine.contains(business)){
                       asianCuisine.add(business);
                   }
               }
           }
       }


    }

    /**
     * Finds the cosine similarity metric between two businesses based upon review word counts.
     * @param businessA the first business to compare, typically the medoid
     * @param businessB the second business to compare
     * @return the cosine similarity metric
     */
    public double cosineSim(FreqHT businessA, FreqHT businessB){
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

    public void runSimulation() throws IOException {
        double greatestCosineSum = 0;
        List<Business> barCluster = new ArrayList<>();
        List<Business> americanCluster = new ArrayList<>();
        List<Business> breakfastCluster = new ArrayList<>();
        List<Business> italianCluster = new ArrayList<>();
        List<Business> asianCluster = new ArrayList<>();

        HashMap<Business, FreqHT> businessFreqHTHashMap = new HashMap<>();

        for (int i = 0; i < 100; i++){
            Business barMedoid = pickOne(bars);

            Business americanMedoid = pickOne(american);
            while(barMedoid == americanMedoid){
                americanMedoid = pickOne(american);
            }

            Business breakfastMedoid = pickOne(breakfast);
            while(breakfastMedoid == barMedoid || breakfastMedoid == americanMedoid){
                breakfastMedoid = pickOne(breakfast);
            }

            Business italianMedoid = pickOne(italian);
            while(italianMedoid == barMedoid || italianMedoid == breakfastMedoid || italianMedoid == americanMedoid){
                italianMedoid = pickOne(italian);
            }

            Business asianMedoid = pickOne(asianCuisine);
            while(asianMedoid == barMedoid || asianMedoid == breakfastMedoid || asianMedoid == americanMedoid || asianMedoid == italianMedoid){
                asianMedoid = pickOne(asianCuisine);
            }

            double cosineSum = 0;

            List<Business> currentBarCluster = new ArrayList<>();
            currentBarCluster.add(barMedoid);

            List<Business> currentAmericanCluster = new ArrayList<>();
            currentAmericanCluster.add(americanMedoid);

            List<Business> currentBreakfastCluster = new ArrayList<>();
            currentBreakfastCluster.add(breakfastMedoid);

            List<Business> currentItalianCluster = new ArrayList<>();
            currentItalianCluster.add(italianMedoid);

            List<Business> currentAsianCluster = new ArrayList<>();
            currentAsianCluster.add(asianMedoid);

            List<Business> medoids = new ArrayList<>();
            medoids.add(barMedoid);
            medoids.add(americanMedoid);
            medoids.add(breakfastMedoid);
            medoids.add(italianMedoid);
            medoids.add(asianMedoid);
            for (Business business: medoids){
                if (!businessFreqHTHashMap.containsKey(business)){
                    businessFreqHTHashMap.put(business, getFreqHT(business));
                }
            }

            for (Business business: businesses){
                if (!businessFreqHTHashMap.containsKey(business)){
                    businessFreqHTHashMap.put(business, getFreqHT(business));
                }

                if (medoids.contains(business)){
                    continue;
                }

                double largestCosine = 0;
                String mostSimilarMedoid = "";

                double cosineSimilarity = cosineSim(businessFreqHTHashMap.get(barMedoid), businessFreqHTHashMap.get(business));
                largestCosine = cosineSimilarity;
                mostSimilarMedoid = "bar";

                cosineSimilarity = cosineSim(businessFreqHTHashMap.get(americanMedoid), businessFreqHTHashMap.get(business));
                if (cosineSimilarity > largestCosine){
                    largestCosine = cosineSimilarity;
                    mostSimilarMedoid = "american";
                }

                cosineSimilarity = cosineSim(businessFreqHTHashMap.get(breakfastMedoid), businessFreqHTHashMap.get(business));
                if (cosineSimilarity > largestCosine){
                    largestCosine = cosineSimilarity;
                    mostSimilarMedoid = "breakfast";
                }

                cosineSimilarity = cosineSim(businessFreqHTHashMap.get(italianMedoid), businessFreqHTHashMap.get(business));
                if (cosineSimilarity > largestCosine){
                    largestCosine = cosineSimilarity;
                    mostSimilarMedoid = "italian";
                }

                cosineSimilarity = cosineSim(businessFreqHTHashMap.get(asianMedoid), businessFreqHTHashMap.get(business));
                if (cosineSimilarity > largestCosine){
                    largestCosine = cosineSimilarity;
                    mostSimilarMedoid = "asian";
                }

                cosineSum += largestCosine;

                if (mostSimilarMedoid.equals("bar")){
                    currentBarCluster.add(business);
                } else if (mostSimilarMedoid.equals("american")){
                    currentAmericanCluster.add(business);
                } else if (mostSimilarMedoid.equals("breakfast")){
                    currentBreakfastCluster.add(business);
                } else if (mostSimilarMedoid.equals("italian")){
                    currentItalianCluster.add(business);
                } else {
                    currentAsianCluster.add(business);
                }

            }

            System.out.println(cosineSum);

            if ( cosineSum > greatestCosineSum){
                greatestCosineSum = cosineSum;
                barCluster = currentBarCluster;
                americanCluster = currentAmericanCluster;
                breakfastCluster = currentBreakfastCluster;
                italianCluster = currentItalianCluster;
                asianCluster = currentAsianCluster;

                System.out.println(barCluster.get(0).getName() + " " + barCluster.size());
                System.out.println(americanCluster.get(0).getName() + " " + americanCluster.size());
                System.out.println(breakfastCluster.get(0).getName() + " " + breakfastCluster.size());
                System.out.println(italianCluster.get(0).getName() + " " + italianCluster.size());
                System.out.println(asianCluster.get(0).getName() + " " + asianCluster.size());
            }

            System.out.println(i);

        }

        this.barCluster = barCluster;
        this.americanCluster = americanCluster;
        this.breakfastCluster = breakfastCluster;
        this.italianCluster = italianCluster;
        this.asianCuisineCluster = asianCluster;

        writeCluster("Bar", barCluster);
        writeCluster("American", americanCluster);
        writeCluster("Breakfast", breakfastCluster);
        writeCluster("Italian", italianCluster);
        writeCluster("Asian", asianCluster);

    }

    private Business pickOne(List<Business> businesses){
        Random random = new Random();
        return businesses.get(random.nextInt(businesses.size()));
    }

    /**
     * Gets the freqHT for a business from disk using the extensible hashtable created by the loader.
     * @param business in which a freqHT is desired for.
     * @return the freqHT for the associated business.
     * @throws IOException
     */
    private FreqHT getFreqHT(Business business) throws IOException {
        List<BusinessFileData> businesses =  eht.getBinsInfo(eht.getBin(business.getName()));
        for (BusinessFileData businessFileData: businesses){
            if(businessFileData.getBusinessName().equals(business.getName())){
                return FreqHTfactory.loadFreqHT(businessFileData.getBusinessFileName());
            }
        }

        return new FreqHT();
    }

    /**
     * Writes cluster lists to file.
     * @param clusterName the name of the cluster to be stored on disk
     * @param cluster the list of businesses within the cluster.
     * @throws IOException
     */
    private void writeCluster(String clusterName, List<Business> cluster) throws IOException {
        try(RandomAccessFile clusterFile = new RandomAccessFile(clusterName + "_cluster", "rw");
            FileChannel clusterWritingChannel = clusterFile.getChannel()){
            ByteBuffer clusterBuffer = ByteBuffer.allocate(44);
            clusterBuffer.limit(40);
            clusterBuffer.put(clusterName.getBytes(StandardCharsets.UTF_8));
            clusterBuffer.limit(44);
            clusterBuffer.position(40);
            clusterBuffer.putInt(cluster.size());
            clusterBuffer.position(0);
            clusterWritingChannel.write(clusterBuffer);

            for (Business business: cluster){
                clusterBuffer = ByteBuffer.allocate(80);
                clusterBuffer.put(business.getName().getBytes(StandardCharsets.UTF_8));
                clusterBuffer.position(0);
                clusterWritingChannel.write(clusterBuffer);
            }

        }
    }
}
