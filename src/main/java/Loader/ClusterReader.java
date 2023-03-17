package Loader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClusterReader {

    public static List<String> readCluster(String clusterName) throws IOException {
        try(RandomAccessFile clusterFile = new RandomAccessFile(clusterName + "_cluster", "r");
            FileChannel clusterReadingChannel = clusterFile.getChannel()){
            ByteBuffer clusterBuffer = ByteBuffer.allocate(44);
            clusterReadingChannel.read(clusterBuffer);
            clusterBuffer.position(0);
            byte[] name = new byte[40];
            clusterBuffer.limit(40);
            clusterBuffer.get(name);
            clusterBuffer.limit(44);
            clusterBuffer.position(40);
            int size = clusterBuffer.getInt();

            List<String> businessNames = new ArrayList<>();

            for (int i = 0; i < size; i++){
                clusterBuffer = ByteBuffer.allocate(80);
                clusterReadingChannel.read(clusterBuffer);
                byte[] businessName = new byte[80];
                clusterBuffer.position(0);
                clusterBuffer.get(businessName);
                businessNames.add((new String(businessName, StandardCharsets.UTF_8)).replace("\0",""));
            }

            return businessNames;

        }
    }

}
