package com.yelp.restaurantFinder;

/**
 * A class used to store all the relevant information regarding reviews
 */
public class Review {

    private String business_id;
    private String review_id;
    private int stars;
    private String text;
    private int useful;

    public Review(String business_id, String review_id, int stars, String text, int useful){
        this.business_id = business_id;
        this.stars = stars;
        this.text = text;
        this.useful = useful;
    }

    public String getBusiness_id() { return business_id; }
    public int getStars() { return stars; }
    public String getText() { return text; }
    public int getUseful() { return useful; }
    public String getReview_id() { return review_id; }
}
