package Loader;

import com.yelp.restaurantFinder.Business;
import com.yelp.restaurantFinder.FreqHT;
import com.yelp.restaurantFinder.GsonDataRetriever;
import com.yelp.restaurantFinder.Review;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLockInterruptionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoaderApplication {

    public static void main(String[] args) throws Exception {

        List<Business> businesses = GsonDataRetriever.getBusinessList();
        HashMap<String, Business> businessMap = GsonDataRetriever.getBusinessHashMap();
        List<Review> reviews = GsonDataRetriever.getReviewList(businessMap);

        System.out.println(businesses.size());
        System.out.println(businessMap.size());
        System.out.println(reviews.size());

        Business business = businesses.get(0);
        try (RandomAccessFile writer = new RandomAccessFile(business.getName().replace(" ", "_"), "rw");
             FileChannel writingChannel = writer.getChannel()) {

            FreqHT textTable = new FreqHT();
            int reviewCount = 0;
            for (Review review : reviews) {
                Set<String> uniqueWords = new HashSet<>();
                if (review.getBusiness_id().equals(business.getBusiness_id())) {

                    String[] reviewWords = splitReviewText(review.getText());

                    for (String word: reviewWords){
                        textTable.add(word);
                        uniqueWords.add(word);
                    }

                    for (String uniqueWord: uniqueWords){
                        textTable.setNumDocsAppearedIn(uniqueWord, textTable.getDocNum(uniqueWord) + 1);
                    }

                }else {
                    String[] reviewWords = splitReviewText(review.getText());

                    uniqueWords.addAll(List.of(reviewWords));

                    for (String uniqueWord: uniqueWords){
                        textTable.setNumDocsAppearedIn(uniqueWord, textTable.getDocNum(uniqueWord) + 1);
                    }
                }

                reviewCount++;
            }

            textTable.setReviewCount(reviewCount);

            textTable.printAll();
            System.out.println("\n Writing Table \n");

            textTable.writeTable(writingChannel);
        }


        try (RandomAccessFile reader = new RandomAccessFile(business.getName().replace(" ", "_"), "r");
             FileChannel readingChannel = reader.getChannel()) {
            FreqHT textTable = FreqHTfactory.loadFreqHT(readingChannel);
            textTable.printAll();
        }

    }

    private static String[] splitReviewText(String text){
        return text.replace(",", "")
                .replace(".", "")
                .replace("!", "")
                .replace("?","")
                .replace(")", "")
                .replace("(", "")
                .toLowerCase()
                .split(" ");
    }
}
