package sst.images.localization.gps;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.GpsDirectory;
import sst.images.localization.exceptions.GpsException;
import sst.images.localization.model.Localisation;

import java.io.File;
import java.io.IOException;

public class VideoGPSByDrew implements Gps {
    @Override
    public Localisation retrieveLocalisation(File videoFile) throws GpsException {
        Localisation gpsInfo = null;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(videoFile);
            for (Directory directory : metadata.getDirectories()) {
                if (directory instanceof GpsDirectory gpsDir) {
                    if (gpsDir.getGeoLocation() != null) {
                        System.out.println("Latitude: " + gpsDir.getGeoLocation().getLatitude());
                        System.out.println("Longitude: " + gpsDir.getGeoLocation().getLongitude());
                        gpsInfo = getGpsInfo(gpsDir.getGeoLocation());
                    } else {
                        System.err.println("No location found on file " + videoFile);
                        return null;
                    }
                }
            }
        } catch (ImageProcessingException | IOException e) {
            throw new GpsException(e);
        }
        return gpsInfo;
    }

    private static Localisation getGpsInfo(GeoLocation geoLocation) {
        Localisation localisation = null;
        if (geoLocation != null) {
            localisation = new Localisation();
            localisation.setLatitude(geoLocation.getLatitude());
            localisation.setLongitude(geoLocation.getLongitude());
        }
        return localisation;
    }
}
