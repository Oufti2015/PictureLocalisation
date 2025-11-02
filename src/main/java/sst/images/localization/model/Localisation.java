package sst.images.localization.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class Localisation implements Serializable {
    @Getter
    @Setter
    private Double latitude;
    @Getter
    @Setter
    private Double longitude;
    @Getter
    @Setter
    private String city;
    @Getter
    @Setter
    private String cityShortCode;
    @Getter
    @Setter
    private String region;
    @Getter
    @Setter
    private String country;
    @Getter
    @Setter
    private String countryCode;

    @Getter
    @Setter
    private String imageFileName;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String jsonResult;


    @Override
    public String toString() {
        return countryCode + "-" + region + "-" + city + " (" + latitude + "," + longitude + ")";
    }
}
