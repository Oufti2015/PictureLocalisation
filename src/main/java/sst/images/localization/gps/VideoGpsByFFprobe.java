package sst.images.localization.gps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sst.images.localization.exceptions.GpsException;
import sst.images.localization.model.Localisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class VideoGpsByFFprobe implements Gps {
    @Override
    public Localisation retrieveLocalisation(File videoFile) throws GpsException {
        Localisation gpsInfo;

        try {
            JsonNode rootNode = getJsonNode(videoFile);

            // Extraire la valeur "location"
            JsonNode locationNode = rootNode.path("format").path("tags").path("location");
            if (!locationNode.isMissingNode()) {
                String location = locationNode.asText();
                double[] coordinates = parseLocation(location);
                gpsInfo = new Localisation();
                gpsInfo.setLatitude(coordinates[0]);
                gpsInfo.setLongitude(coordinates[1]);

                //System.out.println("Latitude: " + coordinates[0]);
                //System.out.println("Longitude: " + coordinates[1]);
            } else {
                System.err.println("No location found on file " + videoFile);
                return null;
            }
        } catch (Exception e) {
            throw new GpsException(e);
        }
        return gpsInfo;
    }

    private static JsonNode getJsonNode(File videoFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "ffprobe", "-v", "quiet", "-print_format", "json",
                "-show_entries", "format_tags=location", videoFile.getAbsolutePath());

        builder.redirectErrorStream(true);
        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder jsonOutput = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonOutput.append(line);
        }
        process.waitFor();

        // Convertir la sortie en JSON
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonOutput.toString());
    }

    // Convertit la chaîne location en latitude et longitude
    public static double[] parseLocation(String location) {
        // Format : "+48.8588+002.2945/" ou "-12.3456-098.7654/"
        String cleanLocation = location.replace("/", ""); // Supprime le slash à la fin
        String[] parts = cleanLocation.split("(?=[+-])"); // Sépare en fonction des signes + ou -

        double latitude = Double.parseDouble(parts[0]); // Latitude
        double longitude = Double.parseDouble(parts[1]); // Longitude

        return new double[]{latitude, longitude};
    }
}
