package Loader;

import java.io.IOException;

public class ExtendibleHashTableFactory {

    /**
     * A way to load an eht from disk, mostly used within the restaurant finder application.
     * @return a fully loaded eht with all the business names to bins ( containing business file names ).
     * @throws IOException
     */
    public static ExtendibleHashTable createExtendibleHashTable() throws IOException {
        ExtendibleHashTable hashTable = new ExtendibleHashTable();
        hashTable.readTableFromFile("extensibleHashTable");
        System.out.println("finished");
        return hashTable;
    }
}
