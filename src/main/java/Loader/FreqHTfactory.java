package Loader;

import com.yelp.restaurantFinder.FreqHT;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FreqHTfactory {

    public static FreqHT loadFreqHT(String fileName) throws IOException {
        RandomAccessFile freqHTFile = new RandomAccessFile(fileName, "r");
        FileChannel readingChannel = freqHTFile.getChannel();
        FreqHT theHT = new FreqHT();
        theHT.readTable(readingChannel);
        return theHT;
    }
}
