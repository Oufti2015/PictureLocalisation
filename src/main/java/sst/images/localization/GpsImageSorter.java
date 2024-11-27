package sst.images.localization;


import org.apache.commons.imaging.ImageReadException;
import sst.images.localization.file.FileCopier;

import java.io.File;
import java.io.IOException;

public class GpsImageSorter {
    private final String inputFolderName;
    private final String outputFolderName;
    private final FileCopier fileCopier = new FileCopier();
    private final boolean moveRequested;

    public GpsImageSorter(String[] args) {
        this.inputFolderName = args[0];
        this.outputFolderName = args[1];
        moveRequested = args.length >= 3 && "--move".equals(args[3]);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("USAge: GpsImageSorter <input folder> <output folder>");
            System.exit(-1);
        }

        try {
            new GpsImageSorter(args).run();
        } catch (GpsImageSorterException | IOException | ImageReadException e) {
            e.printStackTrace();
        }
    }

    private void run() throws GpsImageSorterException, IOException, ImageReadException {
        checkParameters();
        dirProcessing(new File(inputFolderName), new File(outputFolderName));
    }

    private void dirProcessing(File inputFolder, File outputFolder) throws IOException, ImageReadException, GpsImageSorterException {
        for (File file : inputFolder.listFiles()) {
            if (file.isDirectory()) {
                dirProcessing(file, outputFolder);
            } else {
                fileCopier.fileProcessing(file, outputFolder, moveRequested);
            }
        }
    }

    private void checkParameters() throws GpsImageSorterException {
        checkFolderExists(inputFolderName);
        checkFolderExists(outputFolderName);

        System.out.println("Input  : " + inputFolderName);
        System.out.println("Output : " + outputFolderName);
    }

    private void checkFolderExists(String folder) throws GpsImageSorterException {
        if (!new File(folder).exists()) {
            throw new GpsImageSorterException("Folder " + folder + " does not exists.");
        }
        if (!new File(folder).isDirectory()) {
            throw new GpsImageSorterException("Folder " + folder + " is not a directory.");
        }
    }
}
