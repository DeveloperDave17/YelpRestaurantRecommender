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

        Set<String> uniqueWords = poolUniqueWords(reviews);
        HashMap<String, List<Review>> businessListHashMap = GsonDataRetriever.getBusinesstoReviewList(businessMap);
        HashMap<String, Integer> uniqueWordWithCounts = uniqueWordToCount(reviews);

        for (Business business: businesses) {
            String businessFileName = business.getName().replace(" ", "_").replace("/", "_");
            try (RandomAccessFile writer = new RandomAccessFile(businessFileName, "rw");
                 FileChannel writingChannel = writer.getChannel()) {

                FreqHT textTable = new FreqHT();
                int reviewCount = reviews.size();
                List<Review> reviewsList = businessListHashMap.get(business.getBusiness_id());
                for (Review review : reviewsList) {
                    String[] reviewWords = splitReviewText(review.getText());
                    for (String word : reviewWords) {
                        textTable.add(String.format("%1.40s", word));
                    }

                }

                for(String uniqueWord: uniqueWords) {
                    if (textTable.contains(String.format("%1.40s", uniqueWord))) {
                        textTable.setNumDocsAppearedIn(String.format("%1.40s", uniqueWord),
                                uniqueWordWithCounts.get(uniqueWord));
                    }
                }

                textTable.setReviewCount(reviewCount);
                textTable.writeTable(writingChannel);
            }

            businessFilesEHT.put(business.getName(), new BusinessFileData(business.getName(), businessFileName, ""));

        }

        businessFilesEHT.writeTableToFile();
//        KMedoids kmedoid = new KMedoids(businessFilesEHT, businesses, uniqueWords);
//        kmedoid.runSimulation();
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

    public static Set<String> poolUniqueWords(List<Review> reviews){
        Set<String> uniqueWords = new HashSet<>();
        for (Review review : reviews) {
            String[] reviewWords = splitReviewText(review.getText());

            uniqueWords.addAll(List.of(reviewWords));
        }

        return uniqueWords;
    }

    private static HashMap<String, Integer> uniqueWordToCount(List<Review> reviews){

        HashMap<String, Integer> uniqueWordsToCount = new HashMap<>();

        for(Review review: reviews) {
            String[] reviewWords = splitReviewText(review.getText());
            Set<String> uniqueWords = new HashSet<>();
            uniqueWords.addAll(List.of(reviewWords));

            for (String uniqueWord : uniqueWords) {
                uniqueWordsToCount.put(uniqueWord, uniqueWordsToCount.getOrDefault(uniqueWord, 0) + 1);
            }
        }

        return uniqueWordsToCount;
    }
}
