package sst.images.localization.save;

import sst.date.utils.DateTimeUtils;
import sst.images.localization.model.Localisation;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimestampSaver {
    private final static TimestampSaver instance;

    static {
        instance = new TimestampSaver();
    }

    public static TimestampSaver me() {
        return instance;
    }

    public static final String IMAGES_MAP_DATA = "timesMap.data";
    private Map<LocalDateTime, Localisation> localisationMap = new HashMap<>();
    private String folder = null;

    private TimestampSaver() {
    }

    public void put(LocalDateTime timestamp, Localisation localisation) {
        localisationMap.put(timestamp, localisation);
    }

    public Localisation get(LocalDateTime timestamp) {
        return localisationMap.get(timestamp);
    }

    public Localisation closest(LocalDateTime timestamp) {
        LocalDateTime closestTimestamp = DateTimeUtils.findClosestTimestamp(localisationMap.keySet().stream().toList(), timestamp);
        if (closestTimestamp != null && Math.abs(Duration.between(closestTimestamp, timestamp).toDays()) <= 1) {
            Localisation localisation = localisationMap.get(closestTimestamp);
            System.out.println("Found closest timestamp: " + closestTimestamp + " for localisation: " + localisation);
            return localisation;
        }
        return null;
    }

    public void save() throws IOException {
        if (folder != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(filename(folder));
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(localisationMap); // Sérialise l'objet et l'écrit dans le fichier
            }
        }
    }

    public void save(String folder) throws IOException {
        this.folder = folder;
        save();
    }

    private static File filename(String folder) {
        return new File(folder + File.separator + IMAGES_MAP_DATA);
    }

    public void load(String folder) throws IOException, ClassNotFoundException {
        this.folder = folder;
        try (FileInputStream fileInputStream = new FileInputStream(filename(folder));
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            Object object = objectInputStream.readObject();
            if (object instanceof Map) {
                localisationMap = (Map<LocalDateTime, Localisation>) object; // Désérialise l'objet
            }
        }
    }

    public List<String> countries() {
        return localisationMap.values().stream()
                .map(Localisation::getCountryCode)
                .distinct()
                .toList();
    }

    public List<LocalDateTime> timestampsForCountry(String countryCode) {
        return localisationMap.entrySet().stream()
                .filter(entry -> countryCode.equals(entry.getValue().getCountryCode()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
