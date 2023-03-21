package Loader;

import com.yelp.restaurantFinder.FreqHT;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FreqHTfactory {

    /**
     * Loads a frequency hashtable from file for a specific business file name.
     * @param fileName the business's file name.
     * @return The business file's stored FreqHT.
     * @throws IOException
     */
    public static FreqHT loadFreqHT(String fileName) throws IOException {
        RandomAccessFile freqHTFile = new RandomAccessFile(fileName, "r");
        FileChannel readingChannel = freqHTFile.getChannel();
        FreqHT theHT = new FreqHT();
        theHT.readTable(readingChannel);
        return theHT;
    }
}
