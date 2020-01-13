package Collaborative;


import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("DuplicatedCode")
public class Trainer {

    private static final int K = 1;
    private static final Double alpha = 0.1;
    private static final Double MIN_CHANGE = 1e-16;
    private static final int MAX_ITERATIONS = 5000;

    /**
     * Method to train polynomial
     * @param N number of elements
     * @param polynomial polynomial description
     * @param trainSet training set
     */
    private static int train(int N, Double[][] polynomial, Double[][] trainSet) {

        // Copy inputs from sets
        Double[][] inputs = new Double[trainSet.length][];
        for (int i = 0; i < trainSet.length; i ++) {
            inputs[i] = Arrays.copyOf(trainSet[i], trainSet[i].length - 1); // Only inputs without values
        }

        // Create calculator instance
        Calculator calculator = new Calculator(N, K);

        // Create var for previous params
        Double[] previousParams = new Double[N + 1];
        Arrays.fill(previousParams, (double) 0);

        boolean ifContinue = true;

        // Main iteration
        int iterations;
        for (iterations = 0; iterations < 1 && ifContinue; iterations++) {  // until iterations goal reached or change to small

            // Create var for errors
            double error;
            Double[] averageDist = new Double[polynomial.length];
            Arrays.fill(averageDist, 0.0);

            // Calculate error
            for (int i = 0; i < trainSet.length; i++) { // for each line
                error = calculator.calculateSingle(polynomial, inputs[i]) - trainSet[i][N]; // calculate error in respect to value from training set

                // Calculate average distances
                for (int j = 0; j < polynomial.length; j++) {   // for different input values
                    averageDist[j] += polynomial[j][0] == 0 ? error : error * inputs[i][polynomial[j][0].intValue() - 1];
                }
            }

            averageDist = Arrays.stream(averageDist).map(val -> val / trainSet.length).toArray(Double[]::new);

            // Check if change too small
            for (int i = 0; i < N + 1; i++) {
                if (Math.abs(previousParams[i] - polynomial[i][K]) > MIN_CHANGE) {
                    break;  // optimization
                }
                ifContinue = false;   // weight change was smaller than MIN_CHANGE
            }

            // Save last params & adjust for next iteration
            for (int i = 0; i < N + 1; i++) {
                previousParams[i] = polynomial[i][K];
                polynomial[i][K] = polynomial[i][K] - (alpha * averageDist[i]);
            }
        }
        return iterations;
    }


    /**
     * Method to create polynomial definitions for users and features, and train them both using provided inputs.
     * @param N number of elements
     * @param trainDataUserFirst List<List<Pair<Integer, Integer>>> (in our custom order) containing training records; userId is the first value, rating is second
     * @param trainDataMovieFirst List<List<Pair<Integer, Integer>>> (in our custom order) containing training records; movieId is the first value, rating is second
     * @return trained user polynomial and feature polynomial
     */
    public static Pair<List<Double[][]>, List<Double[][]>> createAndTrainBoth(int N, List<List<Pair<Integer, Integer>>> trainDataUserFirst, List<List<Pair<Integer, Integer>>> trainDataMovieFirst) {

        // Create polynomials
        List<Double[][]> featuresPolynomials = new ArrayList<>();
        List<Double[][]> userPolynomials = new ArrayList<>();
        trainDataMovieFirst.forEach(trainMovieFirst -> {
            Double[][] polynomial = ConvertUtil.getDescription(N, K);
            polynomial[N][K] = 0.0;
            featuresPolynomials.add(polynomial);
        });
        trainDataUserFirst.forEach(trainUserFirst -> userPolynomials.add(ConvertUtil.getDescription(N, K)));

        // Train polynomials
        IntStream.range(0, MAX_ITERATIONS).forEach(i -> {
            // Train userPolynomial using features for inputs
            for (int j = 0; j < trainDataUserFirst.size(); j++) {

                Double[][] featuresValues = new Double[trainDataUserFirst.get(j).size()][N + 2];

                for (int k = 0; k < trainDataUserFirst.get(j).size(); k++) {
                    for (int l = 0; l < N; l++) {
                        featuresValues[k][l] = featuresPolynomials.get(trainDataUserFirst.get(j).get(k).getKey())[l][1];
                    }

                    featuresValues[k][N] = Double.valueOf(trainDataUserFirst.get(j).get(k).getValue());
                }

                Trainer.train(N, userPolynomials.get(j), featuresValues);
            }

            // Train features polynomial using users for inputs
            for (int j = 0; j < trainDataMovieFirst.size(); j++) {

                Double[][] userValues = new Double[trainDataMovieFirst.get(j).size()][N + 2];

                for (int k = 0; k < trainDataMovieFirst.get(j).size(); k++) {
                    for (int l = 0; l < N; l++) {
                        userValues[k][l] = userPolynomials.get(trainDataMovieFirst.get(j).get(k).getKey())[l][1];
                    }

                    userValues[k][N] = Double.valueOf(trainDataMovieFirst.get(j).get(k).getValue());
                }

                Trainer.train(N, featuresPolynomials.get(j), userValues);
            }
        });

        return new Pair<>(userPolynomials, featuresPolynomials);
    }
}
