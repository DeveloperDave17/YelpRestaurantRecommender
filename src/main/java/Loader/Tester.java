package Loader;

import com.yelp.restaurantFinder.Business;
import com.yelp.restaurantFinder.GsonDataRetriever;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class Tester {

    public static void main(String[] args) throws IOException {
        List<Business> theList =  GsonDataRetriever.getBusinessList();
        HashMap<String, Integer> categoryCounts = new HashMap<>();
        for (Business business: theList){
            List<String> categories = business.getCategories();
            for (String category: categories){
                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
            }

        }

        for (String key: categoryCounts.keySet()){
            if(categoryCounts.get(key) > 200) {
                System.out.println(key + " " + categoryCounts.get(key));
            }
        }
    }
}
