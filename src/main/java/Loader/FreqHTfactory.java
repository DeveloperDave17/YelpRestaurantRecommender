package Loader;

import com.yelp.restaurantFinder.FreqHT;

import java.io.IOException;
import java.nio.channels.FileChannel;

public class FreqHTfactory {

    public static FreqHT loadFreqHT(FileChannel readingChannel) throws IOException {
        FreqHT theHT = new FreqHT();
        theHT.readTable(readingChannel);
        return theHT;
    }
}
