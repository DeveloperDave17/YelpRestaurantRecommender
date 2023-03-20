package com.yelp.restaurantFinder;

public class ClusterResult {

    private String clusterInfo;

    public ClusterResult(String clusterInfo){
        this.clusterInfo = clusterInfo;
    }

    public String getClusterInfo(){
        return clusterInfo;
    }
}
