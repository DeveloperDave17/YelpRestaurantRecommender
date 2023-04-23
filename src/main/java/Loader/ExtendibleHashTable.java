package Loader;

import com.yelp.restaurantFinder.Business;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtendibleHashTable {

    private int globalDepth;

    private Bin[] binArray;
    private int size;

    /**
     * Creates a EHT of size 2, which contains two bins that can store up to 100 business file names each.
     */
    public ExtendibleHashTable(){
        int binCount = 0;
        globalDepth = 1;
        size = 2;
        binArray = new Bin[size];
        binArray[0] = new Bin("bin_" + binCount, 1, 0);
        binCount++;
        binArray[1] = new Bin("bin_" + binCount, 1, 0);
    }

    /**
     * Gets the businesses associated bin
     * @param businessName name of the target business
     * @return the bin where the file name is stored
     */
    public Bin getBin(String businessName){
        int h = businessName.hashCode();
        int i = h & ((1 << globalDepth) - 1);
        return binArray[i];
    }

    /**
     * Puts a business name and its associated file data into a bin where it is stored locally.
     * First the method gets the bin where the business would be stored. Secondly the method checks if the bin is full
     * and if its local depth if equal to the global depth, if the conditions are met the size of the eht is doubled and
     * all the bins are remapped via their hashes. Next if the bin is full and the local depth is less than the global depth
     * a new bin is created, the local depth is increased, and all the businesses stored within the original bin are rehashed
     * to see which bin they belong in. Lastly if the bin wasn't full simply add the business information to the end of
     * the bin file and increase its size accordingly.
     * @param businessName the name of the business to be added
     * @param business all of the file data information for a business
     * @throws IOException
     */
    public void put(String businessName, BusinessFileData business) throws IOException {
        Bin bin = getBin(businessName);

        // Checking condition for doubling hash table size
        if (bin.full() && bin.getLocalDepth() == globalDepth){
            int tempSize = size * 2;
            Bin[] largerBinArray = new Bin[tempSize];
            globalDepth++;
            for (Bin currentBin: binArray ){

                // Remapping indices
                List<Integer> indices = generateIndices(currentBin);
                // Mapping the generated indices to the proper bin
                for ( Integer indice : indices){
                    largerBinArray[indice] = currentBin;
                }
            }
            binArray = largerBinArray;
            size = tempSize;
        }

        // Splitting the bin if full
        if (bin.full() && bin.getLocalDepth() < globalDepth){
            List<BusinessFileData> oldBinBusinesses = getBinsInfo(bin);
            int bin1size = 0;
            int bin2size = 0;

            /**
             * A custom naming convention is used here for new bins where the new bins name is bin_ followed by the previous
             * bins binary number ored with a 1 that is equivalent to the new depth of the bins.
             */
            Bin bin2 = new Bin("bin_" + (bin.hashCode() | (1 << bin.getLocalDepth())), bin.getLocalDepth() + 1, 0);

            try(RandomAccessFile bin1File = new RandomAccessFile("./bins/" + bin.getBinFileName(), "rw");
                FileChannel bin1WritingChannel = bin1File.getChannel();
                RandomAccessFile bin2File = new RandomAccessFile("./bins/" + bin2.getBinFileName(), "rw");
                FileChannel bin2WritingChannel = bin2File.getChannel()){
                for (BusinessFileData businessFileData: oldBinBusinesses){

                    int h = businessFileData.getBusinessName().hashCode() & ((1 << globalDepth) - 1);

                    if ((h | (1 << bin.getLocalDepth())) == h){
                        ByteBuffer bin2Buffer = ByteBuffer.allocate(200);
                        bin2Buffer.limit(80);
                        bin2Buffer.put(businessFileData.getBusinessName().getBytes(StandardCharsets.UTF_8));
                        bin2Buffer.limit(160);
                        bin2Buffer.position(80);
                        bin2Buffer.put(businessFileData.getBusinessFileName().getBytes(StandardCharsets.UTF_8));
                        bin2Buffer.limit(200);
                        bin2Buffer.position(160);
                        bin2Buffer.put(businessFileData.getCluster().getBytes(StandardCharsets.UTF_8));
                        bin2Buffer.position(0);
                        bin2WritingChannel.write(bin2Buffer);
                        bin2size++;
                    }else{
                        ByteBuffer bin1Buffer = ByteBuffer.allocate(200);
                        bin1Buffer.limit(80);
                        bin1Buffer.put(businessFileData.getBusinessName().getBytes(StandardCharsets.UTF_8));
                        bin1Buffer.limit(160);
                        bin1Buffer.position(80);
                        bin1Buffer.put(businessFileData.getBusinessFileName().getBytes(StandardCharsets.UTF_8));
                        bin1Buffer.limit(200);
                        bin1Buffer.position(160);
                        bin1Buffer.put(businessFileData.getCluster().getBytes(StandardCharsets.UTF_8));
                        bin1Buffer.position(0);
                        bin1WritingChannel.write(bin1Buffer);
                        bin1size++;
                    }
                }

                bin.setSize(bin1size);
                bin.setLocalDepth(bin.getLocalDepth() + 1);
                bin2.setSize(bin2size);

                List<Integer> indicesForBin1 = generateIndices(bin);
                List<Integer> indicesForBin2 = generateIndices(bin2);

                for (Integer indice: indicesForBin1){
                    binArray[indice] = bin;
                }

                for (Integer indice: indicesForBin2){
                    binArray[indice] = bin2;
                }

            }



        } else {
            try(RandomAccessFile binFile = new RandomAccessFile("./bins/" + bin.getBinFileName(), "rw");
                FileChannel binWritingChannel = binFile.getChannel()){
                binWritingChannel.position((bin.getSize() * 200L));
                ByteBuffer binBuffer = ByteBuffer.allocate(200);
                binBuffer.limit(80);
                binBuffer.put(business.getBusinessName().getBytes(StandardCharsets.UTF_8));
                binBuffer.limit(160);
                binBuffer.position(80);
                binBuffer.put(business.getBusinessFileName().getBytes(StandardCharsets.UTF_8));
                binBuffer.limit(200);
                binBuffer.position(160);
                binBuffer.put(business.getCluster().getBytes(StandardCharsets.UTF_8));
                binBuffer.position(0);
                binWritingChannel.write(binBuffer);
                bin.setSize(bin.getSize() + 1);
            }
        }


    }

    /**
     * Generates the new indices for a bin based on the difference between local depth and global depth.
     * @param bin the bin whose corresponding array indices are being generated.
     * @return a list of array indices for the bin to be stored in the hashtable.
     */
    private List<Integer> generateIndices(Bin bin){
        List<Integer> indices = new ArrayList<>();

        // ReMapping indices to respective bins
        for (int i = bin.getLocalDepth(); i <= globalDepth; i++){
            List<Integer> extendedIndices = new ArrayList<>();
            if (indices.size() == 0){
                indices.add((bin.hashCode() & ((1 << i) - 1)));
            } else {
                for (Integer code : indices){
                    extendedIndices.add(code);
                    extendedIndices.add(code | ( 1 << (i - 1)) );
                }
                indices = extendedIndices;
            }

        }

        return indices;
    }

    /**
     * Returns all the file information from the bin file on disk.
     * @param bin the desired bin to be extracted.
     * @return A list containing all the stored business files and names.
     * @throws IOException
     */
    public List<BusinessFileData> getBinsInfo(Bin bin) throws IOException {

        List<BusinessFileData> businessFileDataList = new ArrayList<>();

        try(RandomAccessFile binFile = new RandomAccessFile("./bins/" + bin.getBinFileName(), "r");
            FileChannel binReadingChannel = binFile.getChannel()){
            for (int i = 0; i < bin.getSize(); i++){
                ByteBuffer businessBuffer = ByteBuffer.allocate(200);
                binReadingChannel.read(businessBuffer);
                businessBuffer.position(0);
                businessBuffer.limit(80);
                byte[] nameBytes = new byte[80];
                businessBuffer.get(nameBytes,0 ,80);
                String businessName = ( new String(nameBytes, StandardCharsets.UTF_8)).replace("\0","");
                businessBuffer.limit(160);
                businessBuffer.position(80);
                byte[] fileNameBytes = new byte[80];
                businessBuffer.get(fileNameBytes,0 ,80);
                String businessFileName = ( new String(fileNameBytes, StandardCharsets.UTF_8)).replace("\0","");
                businessBuffer.limit(200);
                businessBuffer.position(160);
                byte[] clusterNameBytes = new byte[40];
                businessBuffer.get(clusterNameBytes,0 ,40);
                String cluster = ( new String(clusterNameBytes, StandardCharsets.UTF_8)).replace("\0","");
                businessFileDataList.add(new BusinessFileData(businessName, businessFileName, cluster));
            }
        }

        return businessFileDataList;
    }

    /**
     * Grabs all of the information from a bin file on disk to create a hashmap that maps business names to business
     * file names.
     * @param bin
     * @return
     * @throws IOException
     */
    public HashMap<String, String> getBusinessNameToFileName(Bin bin) throws IOException {

        HashMap<String, String> businessFileData = new HashMap<>();

        try(RandomAccessFile binFile = new RandomAccessFile("./bins/" + bin.getBinFileName(), "r");
            FileChannel binReadingChannel = binFile.getChannel()){
            for (int i = 0; i < bin.getSize(); i++){
                ByteBuffer businessBuffer = ByteBuffer.allocate(200);
                binReadingChannel.read(businessBuffer);
                businessBuffer.position(0);
                businessBuffer.limit(80);
                byte[] nameBytes = new byte[80];
                businessBuffer.get(nameBytes,0 ,80);
                String businessName = ( new String(nameBytes, StandardCharsets.UTF_8)).replace("\0","");
                businessBuffer.limit(160);
                businessBuffer.position(80);
                byte[] fileNameBytes = new byte[80];
                businessBuffer.get(fileNameBytes,0 ,80);
                String businessFileName = ( new String(fileNameBytes, StandardCharsets.UTF_8)).replace("\0","");
                businessBuffer.limit(200);
                businessBuffer.position(160);
                byte[] clusterNameBytes = new byte[40];
                businessBuffer.get(clusterNameBytes,0 ,40);
                String cluster = ( new String(clusterNameBytes, StandardCharsets.UTF_8)).replace("\0","");
                businessFileData.put(businessName,businessFileName);
            }
        }

        return businessFileData;
    }

    /**
     * Writes the eht to disk in order to be read outside the loader.
     * @throws IOException
     */
    public void writeTableToFile() throws IOException {
        try(RandomAccessFile hashTableFile = new RandomAccessFile("extensibleHashTable", "rw");
            FileChannel hashTableChannel = hashTableFile.getChannel()){
            ByteBuffer hashTableBuffer = ByteBuffer.allocate(8);
            hashTableBuffer.putInt(globalDepth);
            hashTableBuffer.putInt(size);
            hashTableBuffer.position(0);
            hashTableChannel.write(hashTableBuffer);
            for (Bin bin: binArray){
                hashTableBuffer = ByteBuffer.allocate(48);
                hashTableBuffer.limit(40);
                hashTableBuffer.put(bin.getBinFileName().getBytes(StandardCharsets.UTF_8));
                hashTableBuffer.limit(48);
                hashTableBuffer.position(40);
                hashTableBuffer.putInt(bin.getLocalDepth());
                hashTableBuffer.putInt(bin.getSize());
                hashTableBuffer.position(0);
                hashTableChannel.write(hashTableBuffer);
            }
        }


    }

    /**
     * Allows for a way to load in a preloaded eht from disk.
     * @param fileName the nma eof the eht's file.
     * @throws IOException
     */
    public void readTableFromFile(String fileName) throws IOException {
        try(RandomAccessFile hashTableFile = new RandomAccessFile(fileName, "r");
            FileChannel hashTableChannel = hashTableFile.getChannel()){
            ByteBuffer hashTableBuffer = ByteBuffer.allocate(8);
            hashTableChannel.read(hashTableBuffer);
            hashTableBuffer.position(0);
            int globalDepth = hashTableBuffer.getInt();
            int size = hashTableBuffer.getInt();

            Bin[] tempBinArray = new Bin[size];

            for ( int i = 0; i < size; i++ ){
                boolean binAlreadyExists = false;

                hashTableBuffer = ByteBuffer.allocate(48);
                hashTableChannel.read(hashTableBuffer);
                hashTableBuffer.position(0);
                hashTableBuffer.limit(40);
                byte[] binFileBytes = new byte[40];
                hashTableBuffer.get(binFileBytes);
                String binFileName = new String(binFileBytes, StandardCharsets.UTF_8).replace("\0", "");
                hashTableBuffer.limit(48);
                hashTableBuffer.position(40);
                int localDepth = hashTableBuffer.getInt();
                int binSize = hashTableBuffer.getInt();

                for (int j = 0; j < i; j++){
                    if (tempBinArray[j].getBinFileName().equals(binFileName)){
                        binAlreadyExists = true;
                        tempBinArray[i] = tempBinArray[j];
                    }
                }

                if(!binAlreadyExists){
                    tempBinArray[i] = new Bin(binFileName, localDepth, binSize);
                }

            }

            this.globalDepth = globalDepth;
            this.size = size;
            this.binArray = tempBinArray;

        }
    }





}
