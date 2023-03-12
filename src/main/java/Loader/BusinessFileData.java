package Loader;

/**
 * A wrapper class for persistently storing file names and associated clusters.
 */
public class BusinessFileData {

    private final String businessName;
    private final String businessFileName;
    private String cluster;

    public BusinessFileData (String businessName, String businessFileName, String cluster){
        this.businessName = businessName;
        this.businessFileName = businessFileName;
        this.cluster = cluster;
    }

    public String getCluster () {
        return cluster;
    }

    public String getBusinessFileName() {
        return businessFileName;
    }

    public String getBusinessName() { return businessName; }

    public void setCluster(String cluster){
        this.cluster = cluster;
    }

}
