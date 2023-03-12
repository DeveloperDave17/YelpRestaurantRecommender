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

        ExtendibleHashTable businessFilesEHT = new ExtendibleHashTable();

        Business business = businesses.get(0);
        String businessFileName = business.getName().replace(" ", "_");
        try (RandomAccessFile writer = new RandomAccessFile(businessFileName, "rw");
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
            textTable.writeTable(writingChannel);
        }

        businessFilesEHT.put(business.getName(), new BusinessFileData(business.getName(), businessFileName, ""));

        //Reading the freq table back in
//        try (RandomAccessFile reader = new RandomAccessFile(business.getName().replace(" ", "_"), "r");
//             FileChannel readingChannel = reader.getChannel()) {
//            FreqHT textTable = FreqHTfactory.loadFreqHT(readingChannel);
//            textTable.printAll();
//        }

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
