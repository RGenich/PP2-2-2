package web.model;

public class Car {
    private String country;
    private String manufacturer;
    private int year;

    public Car(String country, String manufacturer, int year) {
        this.country = country;
        this.manufacturer = manufacturer;
        this.year = year;
    }

    @Override
    public String toString() {
        return "" + manufacturer +
                " (" + country + ", "+ year+")";
    }
}
