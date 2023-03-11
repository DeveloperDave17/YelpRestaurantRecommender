package com.yelp.restaurantFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * This class contains all the functions involved with the generation of the similarity metric.
 *
 * @author David Hennigan and Anthony Impellizzeri
 */
public class Similarity {

    /**
     * This function prepares all the necessary information (target businesses that share the name the user queried),
     * calls reviewCompare which checks for the most similar businesses, parses the results of reviewCompare to the
     * appropriate business names, and returns a list of the most similar businesses.
     * @param name the queried restaurant name.
     * @return List containing the similar business names.
     * @throws IOException if the data files are not found.
     */
    public static List<String> calculation(String name, List<Review> reviews) throws IOException {
        List<Business> targetBusinesses = new ArrayList<>();
        List<Business> completeListOfBusinesses = GsonDataRetriever.getBusinessList();

        // Search for the businesses that share the desired name
        for (Business business: completeListOfBusinesses){
            if (business.getName().equals(name)){
                targetBusinesses.add(business);
            }
        }

        if (targetBusinesses.size() == 0){
            return new ArrayList<>();
        }

        HashMap<String,Business> businessHashMap = GsonDataRetriever.getBusinessHashMap();

        List<String> chosenBusinessIDs = reviewCompare(targetBusinesses, reviews, businessHashMap);

        List<String> similarRestaurants = new ArrayList<>();

        for (String id: chosenBusinessIDs){
            similarRestaurants.add(businessHashMap.get(id).getName());
        }

        return similarRestaurants;
    }

    /**
     * The two most "similar" businesses are selected via a metric involving tf-idf scores scaled by a factor of 100,
     * shared business categories where the metric is increased by 5 for each shared category, and finally the count
     * of useful upvotes on a review is added into the metric but scaled down by 95%.
     * @param targetBusinesses a list of the businesses whose names matched the users query.
     * @param reviews a list of all the reviews.
     * @param businessHashMap a hashmap that hashes the business_id to all the businesses.
     * @return A list containing business ids of the results.
     */
    private static List<String> reviewCompare(List<Business> targetBusinesses, List<Review> reviews,
                                       HashMap<String, Business> businessHashMap){

        double highestRated = 0;
        String highestRatedID = "";
        double secondHighestRated = 0;
        String secondHighestRatedId = "";

        for (Business business: targetBusinesses) {
            // Getting all the frequencies
            FreqHT textTable = new FreqHT();
            int reviewCount = 0;
            for (Review review : reviews) {
                Set<String> uniqueWords = new HashSet<>();
                if (review.getBusiness_id().equals(business.getBusiness_id())) {

                    String[] reviewWords = splitReviewText(review.getText());

                    for (String word: reviewWords){
                        textTable.add(word);
                        uniqueWords.add(word);
                    }

                    for (String uniqueWord: uniqueWords){
                        textTable.setNumDocsAppearedIn(uniqueWord, textTable.getDocNum(uniqueWord) + 1);
                    }

                }else {
                    String[] reviewWords = splitReviewText(review.getText());

                    uniqueWords.addAll(List.of(reviewWords));

                    for (String uniqueWord: uniqueWords){
                        textTable.setNumDocsAppearedIn(uniqueWord, textTable.getDocNum(uniqueWord) + 1);
                    }
                }

                reviewCount++;
            }

            textTable.setReviewCount(reviewCount);



            // Looking for the two "most" similar reviews
            for (Review review : reviews) {
                if (review.getBusiness_id().equals(business.getBusiness_id())) {
                    continue;
                }

                double metric = 0;

                for (String category : business.getCategories()) {
                    List<String> reviewCategories = businessHashMap.get(review.getBusiness_id()).getCategories();
                    for (String reviewCategory : reviewCategories) {
                        if (category.equals(reviewCategory)) {
                            metric += 5;
                        }
                    }
                }

                String[] textWords = splitReviewText(review.getText());
                for (String word: textWords){
                    metric += (textTable.getTFIDF(word));
                }

                metric += review.getUseful() * 0.05;

                if (metric > highestRated) {
                    secondHighestRatedId = highestRatedID;
                    secondHighestRated = highestRated;
                    highestRated = metric;
                    highestRatedID = review.getBusiness_id();
                } else if (metric > secondHighestRated) {
                    secondHighestRated = metric;
                    secondHighestRatedId = review.getBusiness_id();
                }
            }
        }

        List<String> similarRestaurantIDs = new ArrayList<>();
        similarRestaurantIDs.add(highestRatedID);
        similarRestaurantIDs.add(secondHighestRatedId);

        return similarRestaurantIDs;
    }

    /**
     * Helps parse the reviews into an array of words
     * @param text the text of the review
     * @return An array of strings consisting of the words of a text review.
     */
    private static String[] splitReviewText(String text){
        return text.replace(",", "")
                .replace(".", "")
                .replace("!", "")
                .replace("?","")
                .replace(")", "")
                .replace("(", "")
                .toLowerCase()
                .split(" ");
    }
}


