package Loader;

/**
 * A wrapper class for persistently storing file names and associated clusters.
 */
public class BusinessFileData {

    private final String businessFileName;
    private String cluster;

    public BusinessFileData (String businessFileName, String cluster){
        this.businessFileName = businessFileName;
        this.cluster = cluster;
    }

    public String getCluster () {
        return cluster;
    }

    public String getBusinessFileName() {
        return businessFileName;
    }

    public void setCluster(String cluster){
        this.cluster = cluster;
    }

}
