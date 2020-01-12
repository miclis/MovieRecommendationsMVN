import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Collaborative.Calculator;
import Collaborative.Trainer;
import javafx.util.Pair;

public class CollaborativeFiltering {

    private static final int N = 8;

    public static void main(String[] args) {
        // Load resources
        List<List<Integer>> trainData = Loader.loadFile("train.csv", 0, 4);
        List<List<Integer>> taskData = Loader.loadFile("task.csv", 0, 3);

        // Lists containing prepared train Data
        List<List<Pair<Integer, Integer>>> trainDataUserFirst = new ArrayList<>();
        List<List<Pair<Integer, Integer>>> trainDataMovieFirst = new ArrayList<>();

        // Helper lists with indexes in order
        List<Integer> usersInOrder = new ArrayList<>();
        List<Integer> moviesInOrder = new ArrayList<>();

        // Read training data
        assert trainData != null;
        for (List<Integer> trainRecord : trainData) { //
            Integer userId = trainRecord.get(1);
            Integer movieId = trainRecord.get(2);

            if (!usersInOrder.contains(userId)) {
                usersInOrder.add(userId);
                trainDataUserFirst.add(new ArrayList<>());
            }

            if (!moviesInOrder.contains(movieId)) {
                moviesInOrder.add(movieId);
                trainDataMovieFirst.add(new ArrayList<>());
            }
        }

        for (List<Integer> trainRecord : trainData) { //
            Integer userId = trainRecord.get(1);
            Integer movieId = trainRecord.get(2);
            Integer rating = trainRecord.get(3);

            int internalUserId = usersInOrder.indexOf(userId);
            int internalMovieId = moviesInOrder.indexOf(movieId);

            trainDataUserFirst.get(internalUserId).add(new Pair<>(internalMovieId, rating));
            trainDataMovieFirst.get(internalMovieId).add(new Pair<>(internalUserId, rating));
        }

        Pair<List<Double[][]>, List<Double[][]>> polynomialDescriptions = Trainer.createAndTrainBoth(N, trainDataUserFirst, trainDataMovieFirst);
        List<Double[][]> userPolynomialList = polynomialDescriptions.getKey();
        List<Double[][]> featurePolynomialList = polynomialDescriptions.getValue();

        Double[][] featureInputs = new Double[polynomialDescriptions.getValue().size()][N];

        for (int i = 0; i < featurePolynomialList.size(); i++) {
            for (int j = 0; j < N; j++) {
                featureInputs[i][j] = featurePolynomialList.get(i)[j][1];
            }
        }

        // Create calculator instance
        Calculator calculator = new Calculator(N, 1);

        assert taskData != null;
        List<Integer> results = taskData.stream()
                .map(taskRecord -> (int) (calculator.calculate(userPolynomialList.get(usersInOrder.indexOf(taskRecord.get(1))),
                        new Double[][]{featureInputs[moviesInOrder.indexOf(taskRecord.get(2))]})[0] + 0.5))
                .map(rating -> {
                    if (rating < 0) rating = 0;
                    if (rating > 5) rating = 5;
                    return rating;
                })
                .collect(Collectors.toList());

        // Add predicted ratings to create submissions
        List<List<Integer>> submissionsData = new ArrayList<>(taskData);
        submissionsData.forEach(record -> record.add(results.remove(0)));
        // Write predicted values to file
        Loader.saveSubmissions(submissionsData);


    }
}
