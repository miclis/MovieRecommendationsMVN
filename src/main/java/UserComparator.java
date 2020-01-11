import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserComparator {

    private static final int MAX_DIFFERENCE = 5;

    public static double compareUsers(Multimap<Integer, UserRating> userMovieRatingMap, int userId1, int userId2) {

        Collection<UserRating> user1MovieRatings = userMovieRatingMap.get(userId1);
        Collection<UserRating> user2MovieRatings = userMovieRatingMap.get(userId2);

        List<Integer> user1MovieIds = user1MovieRatings.stream().mapToInt(UserRating::getMovieId).boxed().collect(Collectors.toList());
        List<Integer> user2MovieIds = user2MovieRatings.stream().mapToInt(UserRating::getMovieId).boxed().collect(Collectors.toList());

        user1MovieIds.retainAll(user2MovieIds); // We work only on same elements

        // They haven't watched same movies - nothing to compare
        if (user1MovieIds.size() < 2) return 0;

        Map<Integer, Integer> ratingDifferenceMap = new HashMap<>();    // Differences between movie ratings will be stored

        // They have watched same movies
        user1MovieRatings.forEach(movieRating -> {  // For each movie rating of user1
            // If the movie was also watched by user2 - it is still present in a List
            if (user1MovieIds.contains(movieRating.getMovieId())) {
                ratingDifferenceMap.put(movieRating.getMovieId(), movieRating.getRating()); // First, put rating of user1
            }
        });
        user2MovieRatings.forEach(movieRating -> {
            // If the movie was watched by user1 - we check only movies watched by both
            if (user1MovieIds.contains(movieRating.getMovieId())) {
                // Set the value to the difference (0 - 5)
                ratingDifferenceMap.put(movieRating.getMovieId(), Math.abs(ratingDifferenceMap.get(movieRating.getMovieId()) - movieRating.getRating()));
            }
        });

        // Average difference per movie rating calculated and normalized so return value is in (0 - 1); 1 means most similar
        return 1 - (ratingDifferenceMap.values().stream().mapToDouble(Double::valueOf).sum() / user1MovieIds.size()) / MAX_DIFFERENCE;
    }
}
