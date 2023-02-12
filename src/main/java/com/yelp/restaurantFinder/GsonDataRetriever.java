package com.yelp.restaurantFinder;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GsonDataRetriever {

    public static HashMap<String,Business> getBusinessHashMap() throws IOException {
        File theBusinessFile = new File("../yelp_academic_dataset_business.json");
        FileInputStream businessStream = new FileInputStream(theBusinessFile);
        return readBusinessJsonStreamHM(businessStream);
    }
    private static HashMap<String,Business> readBusinessJsonStreamHM(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.setLenient(true);
        try{
            return readBusinessesHM(reader);
        } finally {
            reader.close();
        }
    }

    private static HashMap<String,Business> readBusinessesHM(JsonReader reader) throws IOException {
        List<Business> businesses = new ArrayList<>();
        HashMap<String, Business> businessesMap = new HashMap<>();

        while (reader.hasNext()) {
            Business business = readBusiness(reader);
            if (business.getIsRestaurant()){
                businesses.add(business);
            }
        }

        for (Business business: businesses){
            if (business.getReview_count() >= 200 && business.getStars() >= 3){
                businessesMap.put(business.getBusiness_id(), business);
            }
        }
        return businessesMap;
    }

    public static List<Business> getBusinessList() throws IOException {
        File theBusinessFile = new File("../yelp_academic_dataset_business.json");
        FileInputStream businessStream = new FileInputStream(theBusinessFile);
        return readBusinessJsonStreamL(businessStream);
    }

    private static List<Business> readBusinessJsonStreamL(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.setLenient(true);
        try{
            return readBusinessesL(reader);
        } finally {
            reader.close();
        }
    }

    private static List<Business> readBusinessesL(JsonReader reader) throws IOException {
        List<Business> businesses = new ArrayList<>();

        while (reader.hasNext()) {
            Business business = readBusiness(reader);
            if (business.getIsRestaurant() & business.getReview_count() >= 200 && business.getStars() >= 3){
                businesses.add(business);
            }
        }

        return businesses;
    }

    private static Business readBusiness(JsonReader reader) throws IOException {
        String business_id = null;
        String businessName = null;
        double latitude = 0;
        double longitude = 0;
        double stars = 0;
        int review_count = 0;
        List<String> categories = null;
        boolean isRestaurant = false;

        reader.beginObject();
        categories = new ArrayList<>();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("business_id")) {
                business_id = reader.nextString();
            } else if (name.equals("latitude")) {
                latitude = reader.nextDouble();
            } else if (name.equals("longitude")) {
                longitude = reader.nextDouble();
            } else if (name.equals("stars")){
                stars = reader.nextDouble();
            } else if (name.equals("review_count")){
                review_count = reader.nextInt();
            } else if (name.equals("name")){
                businessName = reader.nextString();
            } else if (name.equals("categories")) {
                // Ensures the array is not empty before being read
                if (!reader.peek().equals(JsonToken.NULL)) {
                    String category = reader.nextString();
                    // Checking for restaurants
                    if (category.contains("Restaurants") | category.contains("Food")){
                        // Checking to see if the categories were all mashed into one string
                        if (category.contains(",")){
                            categories = Arrays.stream(category.split(",")).toList();
                        }else{
                            categories.add(category);
                        }
                        isRestaurant = true;
                    } else {
                        categories.add(category);
                    }
                } else{
                    reader.nextNull();
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Business(business_id, businessName, latitude, longitude, stars, review_count, categories, isRestaurant);
    }

    public static List<Review> getReviewList(HashMap<String,Business> businessHashMap) throws IOException {
        File theReviewFile = new File("../yelp_academic_dataset_review.json");
        FileInputStream reviewStream = new FileInputStream(theReviewFile);
        return readReviewJsonStream(reviewStream, businessHashMap);
    }

    private static List<Review> readReviewJsonStream(InputStream in, HashMap<String,Business> businesses) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.setLenient(true);
        try{
            return readReviews(reader,businesses);
        } finally {
            reader.close();
        }
    }

    private static List<Review> readReviews(JsonReader reader, HashMap<String,Business> businesses) throws IOException {
        List<Review> reviews = new ArrayList<>();

        int i = 0;

        while (reader.hasNext() & i < 10000) {
            Review review = readReview(reader);
            if (businesses.containsKey(review.getBusiness_id())){
                Business business = businesses.get(review.getBusiness_id());
                double starsHi = (int)(business.getStars() + 1);
                double starsLo = (int)(business.getStars());
                if (review.getStars() >= starsLo | review.getStars() <= starsHi) {
                    reviews.add(review);
                    i++;
                }
            }
        }
        return reviews;
    }

    private static Review readReview(JsonReader reader) throws IOException {
        String business_id = null;
        int stars = 0;
        String text = null;
        int useful = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("business_id")) {
                business_id = reader.nextString();
            } else if (name.equals("stars")) {
                stars = reader.nextInt();
            } else if (name.equals("text")) {
                text = reader.nextString();
            } else if (name.equals("useful")) {
                useful = reader.nextInt();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Review(business_id, stars, text, useful);
    }
}
