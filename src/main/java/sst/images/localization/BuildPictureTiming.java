package sst.images.localization;

import sst.images.localization.file.FileCopier;
import sst.images.localization.model.Localisation;
import sst.images.localization.save.LocationSaver;
import sst.images.localization.save.TimestampSaver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class BuildPictureTiming {
    private final FileCopier tools = new FileCopier();
    private int index = 0;

    public static void main(String[] args) {
        new BuildPictureTiming().buildTimestampDatabase(args);
    }

    public void buildTimestampDatabase(String[] args) {
        if (args.length < 1) {
            System.err.println("USAge: GpsImageSorter <input folder>");
            System.exit(-1);
        }

        try {
            LocationSaver.me().load(args[0]);

            dirProcessing(new File(args[0]));

            TimestampSaver.me().save(args[0]);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void dirProcessing(File inputFolder) throws IOException {
        for (File file : Objects.requireNonNull(inputFolder.listFiles())) {
            if (file.isDirectory()) {
                dirProcessing(file);
            } else {
                fileProcessing(file);
            }
        }
    }

    private void fileProcessing(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        LocalDateTime creationTimestamp = LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault());
        Localisation localisation = LocationSaver.me().get(file.getName());
        if (Objects.isNull(localisation) || Objects.isNull(localisation.getCountryCode())) {
            localisation = tools.retrieveLocalisation(file);
        }
        if (!Objects.isNull(localisation) && !Objects.isNull(localisation.getCountryCode())) {
            index++;
            TimestampSaver.me().put(creationTimestamp, localisation);
            System.out.println("Processed file: " + file.getName() + " with timestamp: " + creationTimestamp + 3 + " and localisation: " + localisation);

            LocationSaver.me().put(file.getName(), localisation);
        }

        if (index % 1000 == 0) {
            System.out.println("Processed " + index + " files...");
            TimestampSaver.me().save();
            LocationSaver.me().save();
        }
    }
}
