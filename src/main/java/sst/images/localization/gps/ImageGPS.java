package sst.images.localization.gps;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import sst.images.localization.model.Localisation;

import java.io.File;
import java.io.IOException;

public class ImageGPS {
    public Localisation retrieveLocalisation(File imagefile) throws IOException, ImageReadException {
        // Read metadata from the image
        ImageMetadata metadata = Imaging.getMetadata(imagefile);
        TiffImageMetadata tiffMetadata = null;
        if (metadata instanceof TiffImageMetadata tiff) {
            tiffMetadata = tiff;
        }
        if (metadata instanceof JpegImageMetadata jpegImageMetadata) {
            // Extract GPS metadata
            tiffMetadata = jpegImageMetadata.getExif();
        }
        if (tiffMetadata == null) {
            System.err.println("No meta data found on file " + imagefile);
            return null;
        }
        Localisation gpsInfo = getGpsInfo(tiffMetadata);
        if (gpsInfo == null) {
            System.err.println("No location found on file " + imagefile);
            return null;
        }
        return gpsInfo;
    }


    private static Localisation getGpsInfo(TiffImageMetadata tiffMetadata) throws ImageReadException {
        TiffImageMetadata.GPSInfo gpsInfo = tiffMetadata.getGPS();
        Localisation localisation = null;

        if (gpsInfo != null) {
            localisation = new Localisation();
            localisation.setLatitude(gpsInfo.getLatitudeAsDegreesNorth());
            localisation.setLongitude(gpsInfo.getLongitudeAsDegreesEast());
        }
        return localisation;
    }

}
