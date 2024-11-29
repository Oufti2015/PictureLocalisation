package sst.images.localization.save;

import sst.images.localization.model.Localisation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LocationSaver {
    private static LocationSaver instance;

    static {
        instance = new LocationSaver();
    }

    public static LocationSaver me() {
        return instance;
    }

    public static final String IMAGES_MAP_DATA = "imagesMap.data";
    private Map<String, Localisation> localisationMap = new HashMap<>();
    private String folder = null;

    private LocationSaver() {
    }

    public void put(String filename, Localisation localisation) {
        localisationMap.put(filename, localisation);
    }

    public Localisation get(String filename) {
        return localisationMap.get(filename);
    }

    public void save() throws IOException {
        if (folder != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(filename(folder));
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                objectOutputStream.writeObject(localisationMap); // Sérialise l'objet et l'écrit dans le fichier
            }
        }
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
                localisationMap = (Map<String, Localisation>) object; // Désérialise l'objet
            }
        }
    }
}
