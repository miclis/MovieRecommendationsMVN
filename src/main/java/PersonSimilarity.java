import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

public class PersonSimilarity {

    private static final int LIMIT_OF_PERSONS = 15;

    @SuppressWarnings("DuplicatedCode")
    public static void main(String[] args) {
        // Load resources
        List<List<Integer>> movieIds = Loader.loadFile("movies.csv", 0, 2);
        List<List<Integer>> trainData = Loader.loadFile("train.csv", 0, 4);
        List<List<Integer>> taskData = Loader.loadFile("task.csv", 0, 3);

        // Create Map<Id, TMDBId>
        Map<Integer, Integer> movieIdsMap = new HashMap<>();
        assert movieIds != null;
        movieIds.forEach(val -> movieIdsMap.put(val.get(0), val.get(1)));

        Multimap<Integer, UserRating> userMovieRatingMap = ArrayListMultimap.create();
        Multimap<Integer, Integer> movieUsersWatchedMap = ArrayListMultimap.create();

        // Read training data
        assert trainData != null;
        for (List<Integer> trainRecord : trainData) {
            Integer userId = trainRecord.get(1);
            Integer movieId = trainRecord.get(2);
            Integer rating = trainRecord.get(3);

            userMovieRatingMap.put(userId, new UserRating(movieId, rating));
            movieUsersWatchedMap.put(movieId, userId);
        }

        List<Integer> results = new LinkedList<>();

        // Main Loop
        assert taskData != null;
        for (List<Integer> taskRecord : taskData) {

            Integer userId = taskRecord.get(1);
            Integer movieId = taskRecord.get(2);

            Map<Integer, Double> idSimilarityValueMap = new HashMap<>();
            Map<Integer, Integer> idRatingMap = new HashMap<>();

            // For movie, for each user that watched it
            movieUsersWatchedMap.get(movieId).forEach(anotherUserId -> {
                // Calculate users similarity to the one being processed
                idSimilarityValueMap.put(anotherUserId, UserComparator.compareUsers(userMovieRatingMap, userId, anotherUserId));
                // Get anotherUserId's rating for a particular movie from the Multimap
                idRatingMap.put(anotherUserId, userMovieRatingMap.get(anotherUserId)
                        .stream()
                        .filter(userIdUserRatingRecord -> userIdUserRatingRecord.getMovieId().equals(movieId))
                        .findAny()
                        .get()
                        .getRating()
                );
            });

            // <Id, Similarity Value> Map only with best Persons
            Map<Integer, Double> bestPersonsMap = idSimilarityValueMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(LIMIT_OF_PERSONS)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entry1, entry2) -> entry1, LinkedHashMap::new));

            List<Double> ratings = new ArrayList<>();
            List<Double> similarityList = new ArrayList<>();

            /*
             * Create List with ratings * similarity.
             * Create List with similarities.
             * */
            bestPersonsMap.forEach((id, similarityValue) -> {
                ratings.add(idRatingMap.get(id) * similarityValue);
                similarityList.add(similarityValue);
            });

            // SUM(rating * similarity) / SUM(similarity)
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
