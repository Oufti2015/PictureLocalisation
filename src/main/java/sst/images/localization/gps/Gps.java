package sst.images.localization.gps;

import sst.images.localization.exceptions.GpsException;
import sst.images.localization.model.Localisation;

import java.io.File;

public interface Gps {
    Localisation retrieveLocalisation(File imagefile) throws GpsException;
}
