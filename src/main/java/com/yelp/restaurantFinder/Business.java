package org.example;
import java.util.List;

public class Business {

    String business_id;
    double latitude;
    double longitude;
    List<String> categories;
    boolean isRestaurant;
    public Business(String business_id, double latitude, double longitude, List<String> categories, boolean isRestaurant){
        this.business_id = business_id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categories = categories;
        this.isRestaurant = isRestaurant;
    }


}
