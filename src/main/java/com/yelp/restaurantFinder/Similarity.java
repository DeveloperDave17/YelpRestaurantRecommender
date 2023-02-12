package com.yelp.restaurantFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Similarity {

    public List<String> calculation(String name) throws IOException {
        List<Business> targetBusinesses = new ArrayList<>();
        List<Business> completeListOfBusinesses = GsonDataRetriever.getBusinessList();

        // Search for the businesses that share the desired name
        for (Business business: completeListOfBusinesses){
            if (business.getName().equals(name)){
                targetBusinesses.add(business);
            }
        }

        HashMap<String,Business> businessHashMap = GsonDataRetriever.getBusinessHashMap();
        List<Review> completeListOfReviews = GsonDataRetriever.getReviewList(businessHashMap);
        List<String> chosenBusinessIDs = new ArrayList<>();

        for (Business business: targetBusinesses){
            chosenBusinessIDs = reviewCompare(completeListOfReviews, business.getBusiness_id(),
                    business.getCategories(), businessHashMap);
        }

        List<String> similarRestaurants = new ArrayList<>();

        for (String id: chosenBusinessIDs){
            similarRestaurants.add(businessHashMap.get(id).getName());
        }

        return similarRestaurants;
    }

    private List<String> reviewCompare(List<Review> reviews, String businessID, List<String> categories,
                                       HashMap<String, Business> businessHashMap){

        // Creating the text similarity Metric
        for (Review review: reviews){
            if (review.getBusiness_id().equals(businessID)){

            }
        }

        int highestRated = 0;
        String highestRatedID = "";
        int secondHighestRated = 0;
        String secondHighestRatedId = "";

        // Looking for the two "most" similar reviews
        for (Review review: reviews){
            if (review.getBusiness_id().equals(businessID)){
                continue;
            }

            int metric = 0;

            for (String category : categories){
                List<String> reviewCategories = businessHashMap.get(review.getBusiness_id()).getCategories();
                for (String reviewCategory: reviewCategories){
                    if (category.equals(reviewCategory)){
                        metric += 10;
                    }
                }
            }


            if (metric > highestRated){
                secondHighestRatedId = highestRatedID;
                secondHighestRated = highestRated;
                highestRated = metric;
                highestRatedID = review.getBusiness_id();
            } else if (metric > secondHighestRated) {
                secondHighestRated = metric;
                secondHighestRatedId = review.getBusiness_id();
            }
        }

        List<String> similarRestaurantIDs = new ArrayList<>();
        similarRestaurantIDs.add(highestRatedID);
        similarRestaurantIDs.add(secondHighestRatedId);

        return similarRestaurantIDs;
    }
}


