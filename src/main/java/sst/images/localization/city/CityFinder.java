package sst.images.localization.city;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sst.images.localization.model.Localisation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class CityFinder {
    public static final String ADDRESS_JSON = "address";
    private static final String LAYER_PARAMETER = ADDRESS_JSON;
    private static final String ADDRESS_DETAILS_PARAMETER = "1";
    private static final String ZOOM_PARAMETER = "18";
    private static final String FORNAT_PARAMETER = "json";
    private static final String EMAIL_PARAMETER = "stephane.stiennon@gmail.com";

    public Localisation findCity(Localisation localisation) throws IOException {
        String lon = String.format("%2.6f", localisation.getLongitude()).replace(",", ".");
        String lat = String.format("%2.6f", localisation.getLatitude()).replace(",", ".");

        // Construire l'URL pour Nominatim API
        String urlString = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=%s&lat=%s&lon=%s&zoom=%s&addressdetails=%s&layer=%s&email=%s",
                FORNAT_PARAMETER,
                lat,
                lon,
                ZOOM_PARAMETER,
                ADDRESS_DETAILS_PARAMETER,
                LAYER_PARAMETER,
                EMAIL_PARAMETER
        );

        localisation.setUrl(urlString);

        //String proxyHost = "proxy.shrd.dbgcloud.io"; // Replace with your proxy host
        //int proxyPort = 3128; // Replace with your proxy port

        // Set up the proxy
        //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

        // Faire la requête HTTP
        URL url = new URL(urlString);
        //HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Java/ReverseGeocodingApp");
        connection.connect();

        // Lire la réponse
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        //System.out.println("urlString    : " + urlString);
        // Afficher la réponse JSON
        //System.out.println("Réponse brute: " + response);

        // Extraire les données intéressantes
        localisation.setJsonResult(response.toString());
        JsonObject jsonObject = JsonParser.parseString(localisation.getJsonResult()).getAsJsonObject();
        JsonObject address = jsonObject.getAsJsonObject(ADDRESS_JSON);

        localisation.setCity(parseJson(address, Arrays.asList("city", "village", "city_district", "town")));
        localisation.setCityShortCode(parseJson(address, Arrays.asList("ISO3166-2-lvl4", "ISO3166-2-lvl6")));
        localisation.setRegion(parseJson(address, List.of("state")));
        localisation.setCountry(parseJson(address, List.of("country")));
        localisation.setCountryCode(parseJson(address, List.of("country_code")));

        return localisation;
    }

    private String parseJson(JsonObject element, List<String> fields) {
        String result = null;
        for (String field : fields) {
            JsonElement jsonElement = element.get(field);
            if (jsonElement != null) {
                result = jsonElement.getAsString();
                break;
            }
        }
        return result;
    }
}
