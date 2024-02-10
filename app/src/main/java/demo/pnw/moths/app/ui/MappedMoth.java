package demo.pnw.moths.app.ui;
/**
 * Class representing the information used to display moths on the map fragment
 */
public class MappedMoth {
    public String name;
    public double latitude;
    public double longitude;


    public MappedMoth(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
