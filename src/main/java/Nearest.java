import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

public class Nearest {

    private static final int LIMIT_OF_MOVIES = 25;

    public static void main(String[] args) {
        // Load resources
        List<List<Integer>> movieIds = Loader.loadFile("movies.csv", 0, 2);
        List<List<Integer>> trainData = Loader.loadFile("train.csv", 0, 4);
        List<List<Integer>> taskData = Loader.loadFile("task.csv", 0, 3);

        // Create Map<Id, TMDBId>
        Map<Integer, Integer> movieIdsMap = new HashMap<>();
        assert movieIds != null;
        movieIds.forEach(val -> movieIdsMap.put(val.get(0), val.get(1)));

        // Get Features for movies from TMDB API
        Map<Integer, Features> featuresMap = Loader.downloadFeatures(movieIdsMap);

        Multimap<Integer, UserRating> userMovieRatingMap = ArrayListMultimap.create();

        // Read training data
        for (List<Integer> trainRecord : trainData) {
            Integer userId = trainRecord.get(1);
            Integer movieId = trainRecord.get(2);
            Integer rating = trainRecord.get(3);

            userMovieRatingMap.put(userId, new UserRating(movieId, rating));
        }

        List<Integer> results = new LinkedList<>();

        // Main loop
        for (List<Integer> taskRecord : taskData) { // for every record in task.csv

            Map<Integer, Double> idSimilarityValueMap = new HashMap<>();
            Map<Integer, Integer> idRatingMap = new HashMap<>();

            // Get related movie features
            Features movieFeatures = featuresMap.get(taskRecord.get(2));

            userMovieRatingMap.get(taskRecord.get(1)).forEach(
                    userRating -> {   // for each multimap value
                        idSimilarityValueMap.put(
                                userRating.getMovieId(),
                                movieFeatures.calculateSimilarity(featuresMap.get(userRating.getMovieId())));
                        idRatingMap.put(userRating.getMovieId(), userRating.getRating());
                    });

            // <Id, Similarity Value>
            Map<Integer, Double> bestMoviesMap = idSimilarityValueMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(LIMIT_OF_MOVIES)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry1, LinkedHashMap::new));

            List<Double> ratings = new ArrayList<>();
            List<Double> similarityList = new ArrayList<>();

            bestMoviesMap.forEach((id, similarityValue) -> {
                ratings.add(idRatingMap.get(id) * similarityValue);
                similarityList.add(similarityValue);
            });
            double res = ratings.stream().mapToDouble(Double::valueOf).sum() / similarityList.stream().mapToDouble(Double::valueOf).sum();

            // Add predicted rating for the record
            results.add((int) (res + 0.5)); // 0.5 For better rounding
        }

        // Add predicted ratings to create submissions
        List<List<Integer>> submissionsData = new ArrayList<>(taskData);
        submissionsData.forEach(record -> record.add(results.remove(0)));
        // Write predicted values to file
        Loader.saveSubmissions(submissionsData);
    }
}
