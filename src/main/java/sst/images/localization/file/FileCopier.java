package sst.images.localization.file;

import sst.images.localization.GpsImageSorterException;
import sst.images.localization.city.CityFinder;
import sst.images.localization.exceptions.GpsException;
import sst.images.localization.gps.Gps;
import sst.images.localization.gps.ImageGPS;
import sst.images.localization.gps.VideoGPSByDrew;
import sst.images.localization.gps.VideoGpsByFFprobe;
import sst.images.localization.model.Localisation;
import sst.images.localization.save.LocationSaver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FileCopier {
    public void fileProcessing(File file, File outputFolder, boolean move) throws IOException, GpsImageSorterException {
        if (isMediaFile(file)) {
            Localisation localisation = LocationSaver.me().get(file.getName());
            if (Objects.isNull(localisation)) {
                localisation = retrieveLocalisation(file);
                if (!Objects.isNull(localisation)) {
                    LocationSaver.me().put(file.getName(), localisation);
                    LocationSaver.me().save();
                }
            }
            try {
                File destination;
                if (!Objects.isNull(localisation) && !Objects.isNull(localisation.getCountryCode())) {
                    destination = prepareDestinationFolders(file, outputFolder, localisation);
                } else {
                    File unknownFolder = new File(outputFolder + File.separator + "unknown");
                    createFolder(unknownFolder);
                    destination = new File(unknownFolder + File.separator + file.getName());
                }

                if (!destination.exists()) {
                    Path sourcePath = file.toPath();
                    Path destinationPath = destination.toPath();
                    System.out.println("Copying " + sourcePath + " into " + destinationPath + "...");
                    // Copier le fichier (remplace s'il existe déjà)

                    if (move) {
                        Files.move(sourcePath, destinationPath, StandardCopyOption.ATOMIC_MOVE);
                    } else {
                        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (Exception e) {
                if (localisation != null) {
                    System.err.println("url         : " + localisation.getUrl());
                    System.err.println("json result : " + localisation.getJsonResult());
                    e.printStackTrace();
                    throw e;
                }
            }
        } else {
            System.out.println("File " + file + " is not an image.");
        }
    }

    private static File prepareDestinationFolders(File file, File outputFolder, Localisation localisation) throws GpsImageSorterException {
        File countryFolder = new File(outputFolder + File.separator + localisation.getCountryCode().toUpperCase());
        createFolder(countryFolder);

        File regionFolder;
        if (!Objects.isNull(localisation.getRegion())) {
            regionFolder = new File(countryFolder + File.separator + toCamelCase(localisation.getRegion()));
            createFolder(regionFolder);
        } else {
            regionFolder = countryFolder;
        }

        File cityFolder;
        if (!Objects.isNull(localisation.getCity())) {
            cityFolder = new File(regionFolder + File.separator + toCamelCase(localisation.getCity()));
            createFolder(cityFolder);
        } else {
            cityFolder = regionFolder;
        }

        return new File(cityFolder + File.separator + file.getName());
    }

    private static final Gps imageGps = new ImageGPS();
    private static final Gps videoGps1 = new VideoGPSByDrew();
    private static final Gps videoGps2 = new VideoGpsByFFprobe();

    private static final Map<String, Gps> gpsByFileType = new HashMap<>();

    static {
        gpsByFileType.put(".jpg", imageGps);
        gpsByFileType.put(".jpeg", imageGps);
        gpsByFileType.put(".png", imageGps);
        gpsByFileType.put(".gif", imageGps);
        gpsByFileType.put(".bmp", imageGps);
        gpsByFileType.put(".webp", imageGps);
        gpsByFileType.put(".tiff", imageGps);
        gpsByFileType.put(".mp4", videoGps2);
        gpsByFileType.put(".mov", videoGps2);
        gpsByFileType.put(".avi", videoGps2);
    }

    private Localisation retrieveLocalisation(File file) {
        Gps gps = getGps(file);
        CityFinder cityFinder = new CityFinder();
        Localisation result;
        try {
            result = gps.retrieveLocalisation(file);
            if (result != null) {
                result = cityFinder.findCity(result);
            }
        } catch (GpsException | IOException e) {
            System.err.println("Invalid JPEG image " + file);
            result = null;
        }
        return result;
    }

    private static Gps getGps(File file) {
        Set<String> extensions = gpsByFileType.keySet();
        String fileName = file.getName().toLowerCase();
        for (String extension : extensions) {
            if (fileName.endsWith(extension)) {
                return gpsByFileType.get(extension);
            }
        }
        return new ImageGPS();
    }

    private static void createFolder(File folder) throws GpsImageSorterException {
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw new GpsImageSorterException("Cannot create " + folder);
            }
        }
    }

    public static boolean isMediaFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        Set<String> extensions = gpsByFileType.keySet();
        String fileName = file.getName().toLowerCase();
        for (String extension : extensions) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Diviser la chaîne en mots en utilisant des délimiteurs
        String[] words = input.split("[\\s_'_/-]+"); // Espace, underscore ou tiret

        // Construire la chaîne camelCase
        StringBuilder camelCaseString = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }

            if (i == 0) {
                // Le premier mot reste en minuscules
                camelCaseString.append(word.toLowerCase());
            } else {
                // Les mots suivants ont leur première lettre en majuscule
                camelCaseString.append(Character.toUpperCase(word.charAt(0)));
                camelCaseString.append(word.substring(1).toLowerCase());
            }
        }

        return camelCaseString.toString();
    }
}
