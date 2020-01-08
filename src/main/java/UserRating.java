public class UserRating {

    private Integer movieId;
    private Integer rating;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public UserRating(Integer movieId, Integer rating) {
        this.movieId = movieId;
        this.rating = rating;
    }
}
