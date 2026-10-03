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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://my.locationiq.com/dashboard#accesstoken
 * log in with Google account
 */
public class LocationIQ implements CityFinder {
    public static final String ADDRESS_JSON = "address";
    private static final String LAYER_PARAMETER = ADDRESS_JSON;
    private static final String ADDRESS_DETAILS_PARAMETER = "1";
    private static final String ZOOM_PARAMETER = "18";
    private static final String FORMAT_PARAMETER = "json";
    private static final String EMAIL_PARAMETER = "stephane.stiennon@gmail.com";
    private static final String USER_AGENT = "PictureLocalisation/1.0 (stephane.stiennon@gmail.com)";
    private static final String NOMINATIM_URL = "https://us1.locationiq.com/v1/search";
    private static final int MAX_RETRIES = 5;
    private static final long MIN_REQUEST_INTERVAL_MS = 1000L;
    private static final Map<String, String> RESPONSE_CACHE = new ConcurrentHashMap<>();
    private static final String ACCESS_TOKEN = "pk.c84ff85e0fad0237e56397dfbea103eb";
    private static volatile long lastRequestTimestampMs = 0L;

    @Override
    public Localisation findCity(Localisation localisation) throws IOException {
        if (localisation == null) {
            throw new IllegalArgumentException("localisation must not be null");
        }

        String cachedResponse = RESPONSE_CACHE.get(buildCacheKey(localisation));
        String url = buildUrl(localisation);
        System.out.println("LocationIQ URL: " + url);
        if (cachedResponse != null) {
            localisation.setUrl(url);
            localisation.setJsonResult(cachedResponse);
            applyJsonResult(localisation, cachedResponse);
            return localisation;
        }

        String urlString = url;
        localisation.setUrl(urlString);

        String responseBody = fetchResponse(urlString);
        localisation.setJsonResult(responseBody);
        if (responseBody != null && !responseBody.isBlank()) {
            RESPONSE_CACHE.put(buildCacheKey(localisation), responseBody);
        }
        applyJsonResult(localisation, responseBody);
        return localisation;
    }

    private String buildUrl(Localisation localisation) {
        String lon = String.format(Locale.US, "%.6f", localisation.getLongitude()).replace(",", ".");
        String lat = String.format(Locale.US, "%.6f", localisation.getLatitude()).replace(",", ".");

        return String.format(
                Locale.US,
                "%s?format=%s&q=%s,%s&addressdetails=1&statecode=1&normalizeaddress=1&key=%s",
                NOMINATIM_URL,
                FORMAT_PARAMETER,
                lat,
                lon,
                ACCESS_TOKEN
        );
    }

    private String buildCacheKey(Localisation localisation) {
        String lat = String.format(Locale.US, "%.6f", localisation.getLatitude());
        String lon = String.format(Locale.US, "%.6f", localisation.getLongitude());
        return lat + "," + lon;
    }

    private String fetchResponse(String urlString) throws IOException {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                throttleRequest();
                HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(20000);

                int statusCode = connection.getResponseCode();
                String responseBody = readResponseBody(connection, statusCode);

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    return responseBody;
                }

                if (statusCode == 429 || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    if (attempt == MAX_RETRIES) {
                        throw new IOException("Nominatim rejected the request (HTTP " + statusCode + "): " + responseBody);
                    }
                    sleepBeforeRetry(attempt);
                    continue;
                }

                throw new IOException("Nominatim request failed (HTTP " + statusCode + "): " + responseBody);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting before retry", e);
            }
        }

        throw new IOException("Nominatim request failed after " + MAX_RETRIES + " attempts");
    }

    private String readResponseBody(HttpURLConnection connection, int statusCode) throws IOException {
        java.io.InputStream inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (inputStream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private void applyJsonResult(Localisation localisation, String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return;
        }

        responseBody = responseBody.substring(1, responseBody.length() - 1);

        try {
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject address = jsonObject.has(ADDRESS_JSON) && jsonObject.get(ADDRESS_JSON).isJsonObject()
                    ? jsonObject.getAsJsonObject(ADDRESS_JSON)
                    : null;

            if (address == null) {
                return;
            }

            localisation.setCity(parseJson(address, Arrays.asList("city", "village", "city_district", "town")));
            localisation.setCityShortCode(parseJson(address, Arrays.asList("ISO3166-2-lvl4", "ISO3166-2-lvl6")));
            localisation.setRegion(parseJson(address, List.of("state", "state_district")));
            localisation.setCountry(parseJson(address, List.of("country")));
            localisation.setCountryCode(parseJson(address, List.of("country_code")));
        } catch (RuntimeException e) {
            localisation.setCity(null);
            localisation.setCityShortCode(null);
            localisation.setRegion(null);
            localisation.setCountry(null);
            localisation.setCountryCode(null);
        }
    }

    private void throttleRequest() throws InterruptedException {
        synchronized (LocationIQ.class) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRequestTimestampMs;
            if (elapsed < MIN_REQUEST_INTERVAL_MS) {
                Thread.sleep(MIN_REQUEST_INTERVAL_MS - elapsed);
            }
            lastRequestTimestampMs = System.currentTimeMillis();
        }
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        long backoffMs = (long) Math.pow(2, attempt - 1) * 1000L;
        Thread.sleep(backoffMs);
    }

    private String parseJson(JsonObject element, List<String> fields) {
        String result = null;
        for (String field : fields) {
            JsonElement jsonElement = element.get(field);
            if (jsonElement != null && !jsonElement.isJsonNull()) {
                result = jsonElement.getAsString();
                break;
            }
        }
        return result;
    }
}
