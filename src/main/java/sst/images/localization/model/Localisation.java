package sst.images.localization.model;

import lombok.Data;

@Data
public class Localisation {
    private Double latitude;
    private Double longitude;
    private String city;
    private String cityShortCode;
    private String region;
    private String country;
    private String countryCode;
    private String url;
    private String jsonResult;
}
