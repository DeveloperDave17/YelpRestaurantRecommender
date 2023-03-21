package com.yelp.restaurantFinder;

/**
 * A wrapper for the result of our clusterFinder output intended for integration with thymeleaf.
 */
public class ClusterResult {

    private String clusterInfo;

    public ClusterResult(String clusterInfo){
        this.clusterInfo = clusterInfo;
    }

    public String getClusterInfo(){
        return clusterInfo;
    }
}
