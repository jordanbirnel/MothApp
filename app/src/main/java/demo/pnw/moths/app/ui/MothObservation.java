package demo.pnw.moths.app.ui;
/**
 * Represents a single observation of a moth, including its location, images taken, and other relevant details.
 */
public class MothObservation {

    private int index;
    private String mothName;
    private String mothLocation;
    private String cameraImage1;
    private String cameraImage2;
    private String cameraImage3;
    private String cameraImage4;
    private double latitude;
    private double longitude;
    private boolean uploaded;


    /**
     * Constructs a new MothObservation instance with detailed information.
     *
     * @param i Index of the observation.
     * @param name Name of the moth.
     * @param loc Location where the moth was observed.
     * @param pic1 URI of the first image taken.
     * @param pic2 URI of the second image taken.
     * @param pic3 URI of the third image taken.
     * @param pic4 URI of the fourth image taken.
     * @param lat Latitude of the observation location.
     * @param longitude Longitude of the observation location.
     * @param uploaded Flag indicating whether the observation has been uploaded.
     */
    public MothObservation(int i, String name, String loc, String pic1, String pic2, String pic3, String pic4, double lat, double longitude, boolean uploaded) {
        index = i;
        mothName = name;
        mothLocation = loc;
        cameraImage1 = pic1;
        cameraImage2 = pic2;
        cameraImage3 = pic3;
        cameraImage4 = pic4;
        this.latitude=lat;
        this.longitude=longitude;
        this.uploaded=uploaded;
    }

    // Setter methods allow changing the properties of an observation after it has been created.
    public void setMothName(String name) {
        mothName = name;
    }
    public void setMothLocation(String location) {
        mothLocation = location;
    }
    public void setCameraImage1(String pic1) {
        cameraImage1 = pic1;
    }
    public void setCameraImage2(String pic2) {
        cameraImage1 = pic2;
    }
    public void setCameraImage3(String pic3) {
        cameraImage3 = pic3;
    }
    public void setCameraImage4(String pic4) {
        cameraImage4 = pic4;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Getter methods provide read access to the properties of an observation.
    public int getIndex() {
        return index;
    }
    public String getMothName() {
        return mothName;
    }
    public String getMothLocation() {
        return mothLocation;
    }
    public String getCameraImage1() {
        return cameraImage1;
    }
    public String getCameraImage2() {
        return cameraImage2;
    }
    public String getCameraImage3() {
        return cameraImage3;
    }
    public String getCameraImage4() {
        return cameraImage4;
    }
    public String getLatitudeString() {
        return Double.toString(latitude);
    }
    public String getLongitudeString() {
        return Double.toString(longitude);
    }
    public double getLat(){ return latitude;}
    public double getLong(){ return longitude;}
    public boolean isUploaded() {
        return this.uploaded;
    }

    /**
     * Retrieves the camera image URI based on a given index.
     *
     * @param k Index of the camera image (0-based).
     * @return URI of the requested camera image or an empty string if the index is invalid.
     */
    public String getCameraImageK(int k) {
        switch (k) {
            case 0:
                return cameraImage1;
            case 1:
                return cameraImage2;
            case 2:
                return cameraImage3;
            case 3:
                return cameraImage4;
            default:
                return "";
        }
    }
}