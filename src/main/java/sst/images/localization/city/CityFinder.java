package sst.images.localization.city;

import sst.images.localization.model.Localisation;

import java.io.IOException;

public interface CityFinder {
    Localisation findCity(Localisation localisation) throws IOException;
}
