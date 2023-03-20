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

        ExtendibleHashTable eht = ExtendibleHashTableFactory.createExtendibleHashTable();
        System.out.println(eht.getBin(theList.get(0).getName()).getBinFileName());

    }
}
