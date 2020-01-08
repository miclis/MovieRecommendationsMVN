import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.tools.MovieDbException;

public class Loader {
    private static final String RES_PATH = "src/main/resources/";
    private static final String OUT_PATH = "src/main/output/";
    private static final String API_KEY = "f7b51d5758abad3b84e3c0dbba3591e2";

    public static Set<Integer> alreadyDownloadedFeatures;

    /**
     * Method to load csv file
     * @param fileName file name to load
     * @param colsToSkip number of columns from the beginning to be skipped
     * @param limitOfCols max number of columns that will be read
     * @return List<List<Integer>> containing file data
     */
    public static List<List<Integer>> loadFile(String fileName, int colsToSkip, int limitOfCols) {
        try {
            List<String> fileData = Files.readAllLines(Paths.get(RES_PATH + fileName));

            return fileData.stream()
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> line.split(";"))
                    .map(val -> Arrays.stream(val).skip(colsToSkip).limit(limitOfCols).map(Integer::valueOf)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList())
                    ;
        } catch (IOException e) {
            System.out.println("There was an error loading a file " + fileName + "...");
            return null;
        }
    }

    /**
     * Method to download movie features from TMDB
     * @param idMap Map<Id, TMDBId> containing movie Ids
     * @return Map<Integer, Features> containing movies and their features. Key is our movie Id
     */
    public static Map<Integer, Features> downloadFeatures(Map<Integer, Integer> idMap) {
        TmdbMovies api = new TmdbApi(API_KEY).getMovies();

        Map<Integer, Features> featuresMap = new HashMap<>();
        alreadyDownloadedFeatures = new HashSet<>();    // Clean set

        idMap.forEach((id, tmdbId) -> {
            if (!alreadyDownloadedFeatures.contains(tmdbId)) {

                try {
                    MovieDb movieInfo = api.getMovie(tmdbId, "en");
                    Features movieFeatures = new Features(
                            Integer.parseInt(movieInfo.getReleaseDate().split("-")[0]),
                            movieInfo.getBudget(),
                            movieInfo.getGenres()
                    );
                    featuresMap.put(id, movieFeatures);
                } catch (MovieDbException e) {
                    System.out.println("There was an error downloading features for movie with TMDBId: " + tmdbId + ".");
                    if (e.getMessage().contains("404")) System.out.println(e.getMessage());
                    else throw e;   // Something else wrong
                }

                // Add id as data already downloaded
                alreadyDownloadedFeatures.add(tmdbId);
            }
        });
        return featuresMap;
    }

    /**
     * Method to write submissions to a file
     * @param submissions List<List<Integer>> containing submissions data
     */
    public static void saveSubmissions(List<List<Integer>> submissions) {
        try {
            PrintWriter writer = new PrintWriter(new File(OUT_PATH + "submission.csv"));

            writer.println(
                    submissions.stream()
                    .map(line -> line.stream().map(Object::toString).collect(Collectors.joining(";")))
                    .collect(Collectors.joining("\n"))
            );
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Something went wrong when saving submissions...");
            System.out.println(e.getMessage());
        }
    }
}