package sst.images.localization;

import sst.images.localization.save.TimestampSaver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class ShowCountryTiming {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("USAge: GpsImageSorter <input folder>");
            System.exit(-1);
        }

        try {
            TimestampSaver.me().load(args[0]);

            TimestampSaver.me().countries().stream().sorted().forEach(country -> {
                Optional<LocalDateTime> min = TimestampSaver.me().timestampsForCountry(country).stream().min(LocalDateTime::compareTo);
                Optional<LocalDateTime> max = TimestampSaver.me().timestampsForCountry(country).stream().max(LocalDateTime::compareTo);

                System.out.println("Country: " + country + " from " + (min.isPresent() ? min.get() : "N/A") + " to " + (max.isPresent() ? max.get() : "N/A"));
            });
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
