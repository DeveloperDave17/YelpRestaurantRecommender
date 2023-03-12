package Loader;

public class Bin {

    private final String binFileName;

    private final int MAXSIZE = 100;
    private int localDepth;

    private int size;


    public Bin(String binFileName, int localDepth, int size){
        this.binFileName = binFileName;
        this.localDepth = localDepth;
        this.size = size;
    }

    public String getBinFileName(){
        return binFileName;
    }

    public int getLocalDepth(){
        return localDepth;
    }

    public int getSize(){
        return size;
    }

    public void setSize(int size){ this.size = size; }

    public void setLocalDepth(int localDepth){ this.localDepth = localDepth;}

    public boolean full(){
        return size == MAXSIZE;
    }

    @Override
    public int hashCode() {
        String hash = binFileName.split("_")[1];
        return Integer.valueOf(hash);
    }
}
