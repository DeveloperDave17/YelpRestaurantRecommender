package org.example;

public class Review {

    String business_id;
    int stars;
    String text;
    int useful;

    public Review(String business_id, int stars, String text, int useful){
        this.business_id = business_id;
        this.stars = stars;
        this.text = text;
        this.useful = useful;
    }
}
