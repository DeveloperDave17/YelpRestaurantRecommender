package com.yelp.restaurantFinder;
import java.util.List;

/**
 * A class used to store all the relevant information pertaining to businesses
 */

public class Business {

    private String business_id;
    private String name;
    private double latitude;
    private double longitude;
    private double stars;
    private List<String> categories;
    private int review_count;
    private boolean isRestaurant;
    private int isOpen;
    public Business(String business_id, String name, double latitude, double longitude, double stars,
                    int review_count, List<String> categories, boolean isRestaurant, int isOpen){
        this.business_id = business_id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stars = stars;
        this.review_count = review_count;
        this.categories = categories;
        this.isRestaurant = isRestaurant;
        this.isOpen = isOpen;
    }

    public String getBusiness_id(){ return business_id; }
    public String getName(){ return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public List<String> getCategories() { return categories; }
    public double getStars(){ return stars; }
    public int getReview_count(){ return review_count; }
    public boolean getIsRestaurant() { return isRestaurant; }
    public int getIsOpen() { return isOpen; }


}
