package Loader;

import com.yelp.restaurantFinder.Business;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ExtendibleHashTable {

    private int globalDepth;

    private Bin[] binArray;
    private int size;

    public ExtendibleHashTable(){
        int binCount = 0;
        globalDepth = 1;
        size = 2;
        binArray = new Bin[size];
        binArray[0] = new Bin("bin_" + binCount, 1, 0);
        binCount++;
        binArray[1] = new Bin("bin_" + binCount, 1, 0);
    }

    public Bin getBin(String businessName){
        int h = businessName.hashCode();
        int i = h & ((1 << globalDepth) - 1);
        return binArray[i];
    }

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

            Bin bin2 = new Bin("bin_" + (bin.hashCode() | (1 << bin.getLocalDepth())), bin.getLocalDepth() + 1, 0);

            try(RandomAccessFile bin1File = new RandomAccessFile(bin.getBinFileName(), "rw");
                FileChannel bin1WritingChannel = bin1File.getChannel();
                RandomAccessFile bin2File = new RandomAccessFile(bin2.getBinFileName(), "rw");
                FileChannel bin2WritingChannel = bin2File.getChannel()){
                for (BusinessFileData businessFileData: oldBinBusinesses){

                    int h = businessFileData.getBusinessName().hashCode() & ((1 << globalDepth) - 1);

                    if ((h | (1 << bin.getLocalDepth())) == h){
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
                    }else{
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
                    }
                }

                bin.setSize(bin1size);
                bin.setLocalDepth(bin.getLocalDepth() + 1);
                bin2.setSize(bin1size);

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
            try(RandomAccessFile binFile = new RandomAccessFile(bin.getBinFileName(), "rw");
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

    public List<BusinessFileData> getBinsInfo(Bin bin) throws IOException {

        List<BusinessFileData> businessFileDataList = new ArrayList<>();

        try(RandomAccessFile binFile = new RandomAccessFile(bin.getBinFileName(), "r");
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

    public void writeTableToFile() throws IOException {
        try(RandomAccessFile hashTableFile = new RandomAccessFile("extensibleHashTable", "rw");
            FileChannel hashTableChannel = hashTableFile.getChannel()){
            ByteBuffer hashTableBuffer = ByteBuffer.allocate(8);
            hashTableBuffer.putInt(globalDepth);
            hashTableBuffer.putInt(size);
            hashTableBuffer.position(0);
            hashTableChannel.write(hashTableBuffer);
            int indice = 0;
            for (Bin bin: binArray){
                hashTableBuffer = ByteBuffer.allocate(42);
                hashTableBuffer.putInt(indice);
                hashTableBuffer.limit(34);
                hashTableBuffer.put(bin.getBinFileName().getBytes(StandardCharsets.UTF_8));
                hashTableBuffer.limit(42);
                hashTableBuffer.position(34);
                hashTableBuffer.putInt(bin.getLocalDepth());
                hashTableBuffer.putInt(bin.getSize());
                hashTableBuffer.position(0);
                hashTableChannel.write(hashTableBuffer);
                ++indice;
            }
        }


    }








}
