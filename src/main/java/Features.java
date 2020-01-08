import info.movito.themoviedbapi.model.Genre;

import java.util.List;
import java.util.stream.Stream;

public class Features {

    /**
     * Similarity coefficients
     */
    private static final double GENRES_MULTI = 6;   // Most important
    private static final double YEAR_MULTI = 3;
    private static final double BUDGET_MUTLI = 2;
    private static final double BUDGET_NORMALIZER = 100000.0;

    private List<Genre> genres;
    private int releaseYear;
    private long budget;

    public Features(int releaseYear, long budget, List<Genre> genres) {
        this.genres = genres;
        this.releaseYear = releaseYear;
        this.budget = budget;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public long getBudget() {
        return budget;
    }

    public void setBudget(long budget) {
        this.budget = budget;
    }

    /**
     * Method to calculate movies similarity
     * @param toCompare movie features to be compared to
     * @return double representing similarity value
     */
    public double calculateSimilarity(Features toCompare) {
        // Genres (0 - 1)
        double totalGenres = genres.size() + toCompare.getGenres().size();
        double sameGenres = totalGenres - (double) Stream.concat(genres.stream(), toCompare.getGenres().stream()).distinct().count();
        double genresSimilarity = sameGenres / totalGenres;

        // Year (0 - 1)
        double yearSimilarity = Math.pow(0.95, Math.abs(releaseYear - toCompare.getReleaseYear()));

        // Budget (0 - 1)
        double budgetSimilarity = (Math.min(budget, toCompare.getBudget()) + BUDGET_NORMALIZER) / (Math.max(budget, toCompare.getBudget()) + BUDGET_NORMALIZER);

        // Calculate overall
        double overallSimilarity = 0;

        overallSimilarity += genresSimilarity * GENRES_MULTI;
        overallSimilarity += yearSimilarity * YEAR_MULTI;
        overallSimilarity += Math.min(budget, toCompare.getBudget()) > 0 ? budgetSimilarity * BUDGET_MUTLI : 0; // If budget for either movie is not 0

        return overallSimilarity;
    }
}
