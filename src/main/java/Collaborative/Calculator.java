package Collaborative;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

    private int N;
    private int K;

    public Calculator(int N, int K) {
        this.N = N;
        this.K = K;
    }

    /**
     * Method to calculate the value of single polynomial
     * @param polynomial polynomial description
     * @param inputs input values
     * @return result of the polynomial calculation
     */
    Double calculateSingle(Double[][] polynomial, Double[] inputs) {

        Double[] row = new Double[N + 1];
        row[0] = 1.0; // initialization (can't be zero due to further multiplication)

        if (N >= 0) System.arraycopy(inputs, 0, row, 1, N);
        double sumResult = 0;

        for (Double[] element : polynomial) {   // for each element
            double result = 1;  // initialization (can't be zero due to further multiplication)

            for (int j = 0; j < K; j++) {   // input variable times input variable; max degree equals K
                result *= row[element[j].intValue()];    // selects row variable at position j
            }
            sumResult += result*element[K]; // multiplies by weight and sums all elements
        }
        return sumResult;
    }

    /**
     * Method to calculate value of the polynomial for each line of training set
     * @param polynomial polynomial description
     * @param inputs array of arrays containing inputs
     * @return Double[] containing results of calculations
     */
    public Double[] calculate(Double[][] polynomial, Double[][] inputs) {

        List<Double> resultList = new ArrayList<>();

        for (Double[] inputLine : inputs) {
            resultList.add(calculateSingle(polynomial, inputLine));
        }

        return resultList.toArray(new Double[0]);
    }

    /**
     * Method to evaluate how good the polynomial is
     * @param expectedOutputs values from validation set
     * @param outputs values calculated by polynomial
     * @return evaluation score
     */
    double score(Double[] expectedOutputs, Double[] outputs) {
        double score = 0;

        for (int i = 0; i < expectedOutputs.length; i++) {
            score += Math.pow(outputs[i] - expectedOutputs[i], 2);
        }

        return (score / outputs.length);
    }

    /**
     * Method to calculate best scored index (the rest is abandoned)
     * @param scores array of scores for corresponding K and split index
     * @return int representing best K
     */
    static int getBestIndex(Double[][] scores) {

        int bestK = -1;
        double bestResult = Double.MAX_VALUE;

        for (int i = 1; i < scores.length; i++) {   // skip index 0 as there is no value for K = 0
            double lineResult = 0;

            for (int j = 0; j < scores[i].length; j++) {
                lineResult += Math.pow(scores[i][j], 2);
            }

            if (lineResult < bestResult) {
                bestResult = lineResult;
                bestK = i;
            }
        }
        return bestK;
    }
}
