package com.yelp.restaurantFinder;

/**
 * A class that exists just as a wrapper around a string for integration with thymeleaf in order to retrieve user
 * queries
 */
public class Query {

    private String name;

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
