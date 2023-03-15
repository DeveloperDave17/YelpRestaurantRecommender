package Loader;

import java.io.IOException;

public class ExtendibleHashTableFactory {

    public static ExtendibleHashTable createExtendibleHashTable() throws IOException {
        ExtendibleHashTable hashTable = new ExtendibleHashTable();
        hashTable.readTableFromFile("extensibleHashTable");
        return hashTable;
    }
}
