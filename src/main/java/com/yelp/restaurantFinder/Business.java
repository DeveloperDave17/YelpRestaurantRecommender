package com.yelp.restaurantFinder;
import java.util.List;

public class Business {

    String business_id;
    String name;
    double latitude;
    double longitude;
    double stars;
    List<String> categories;
    int review_count;
    boolean isRestaurant;
    public Business(String business_id, String name, double latitude, double longitude, double stars, int review_count, List<String> categories, boolean isRestaurant){
        this.business_id = business_id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stars = stars;
        this.review_count = review_count;
        this.categories = categories;
        this.isRestaurant = isRestaurant;
    }


}
