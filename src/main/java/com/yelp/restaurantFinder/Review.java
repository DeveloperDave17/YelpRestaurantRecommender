package com.yelp.restaurantFinder;

public class Review {

    private String business_id;
    private int stars;
    private String text;
    private int useful;

    public Review(String business_id, int stars, String text, int useful){
        this.business_id = business_id;
        this.stars = stars;
        this.text = text;
        this.useful = useful;
    }

    public String getBusiness_id() { return business_id; }
    public int getStars() { return stars; }
    public String getText() { return text; }
    public int getUseful() { return useful; }
}
