package sst.images.localization.model;

public class Localisation {
    private Double latitude;
    private Double longitude;
    private String city;
    private String cityShortCode;
    private String region;
    private String country;
    private String countryCode;

    public Double latitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double longitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String city() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String cityShortCode() {
        return cityShortCode;
    }

    public void setCityShortCode(String cityShortCode) {
        this.cityShortCode = cityShortCode;
    }

    public String region() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String country() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String countryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
