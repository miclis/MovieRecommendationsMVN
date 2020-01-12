package Collaborative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ConvertUtil {

    /**
     * Method to get all possible combinations
     * @param n
     * @param k
     * @return
     */
    static Double[][] getDescription(int n, int k) {
        List<List<Integer>> solutions = findCombinations(n, k);

        // Sort
        solutions = solutions.stream()
                .map(solution -> {
                    List<Integer> collect = solution.stream().sorted().collect(Collectors.toList());
                    Collections.reverse(collect);
                    return collect;
                })
                .sorted(Comparator.comparingDouble(obj -> getWeight(obj, n)))
                .collect(Collectors.toList());

        List<List<Double>> doubleList = solutions.stream()
                .map(s -> s.stream().map(Double::valueOf).collect(Collectors.toList()))
                .collect(Collectors.toList());

        doubleList.forEach(v -> v.add((Math.random() * 2 - 1)));


        return doubleList.stream()
                .map(s -> s.toArray(new Double[0]))
                .toArray(Double[][]::new);
    }

    /**
     * Method to find all possible combinations with given n and k
     * @param max - N
     * @param rowSize - K
     * @return List<List<Integer>> containing combinations
     */
    private static List<List<Integer>> findCombinations(int max, int rowSize) {
        int[] current = new int[rowSize + 1];
        int[] inputArray = IntStream.rangeClosed(0, max).toArray();
        List<List<Integer>> combinations = new ArrayList<>();

        repeater(current, inputArray, 0, rowSize, 0, max, combinations);
        return combinations;
    }

    /**
     * Method to recursively create combination
     * @param current
     * @param input
     * @param index
     * @param rowSize
     * @param start
     * @param end
     * @param combinations
     */
    private static void repeater(int[] current, int[] input, int index, int rowSize, int start, int end, List<List<Integer>> combinations) {
        // Applies if index == current rowSize
        if (index == rowSize) {
            List<Integer> singleSolution = new ArrayList<>();

            for (int i = 0; i < rowSize; i++) {
                singleSolution.add(input[current[i]]);
            }
            combinations.add(singleSolution);
            return;
        }

        // For all
        for (int i = start; i <= end; i++) {
            current[index] = i;
            repeater(current, input, index + 1, rowSize, i, end, combinations);
        }
    }

    /**
     * Method to calculate weight
     * @param integers
     * @param n
     * @return
     */
    private static double getWeight(List<Integer> integers, int n) {
        double current = 0;
        int size = integers.size();
        for (int i = 0; i < size; i++) {
            current += integers.get(i) * Math.pow(n , size - i);
        }
        return current;
    }

    /**
     * Method to transform training sets ok K degree into 1 degree
     * @param N initial N
     * @param K initial K
     * @param polynomial description of the original polynomial
     * @param inputs input values from training sets
     * @return transformed training set for polynomial of 1 degree
     */
    static Double[][] transform(int N, int K, Double[][] polynomial, Double[][] inputs) {
        List<List<Double>> result = new ArrayList<>();

        for (Double[] in : inputs) {
            Double[] inputRow = new Double[N + 1];
            inputRow[0] = 1.0;

            if (N >= 0) System.arraycopy(in, 0, inputRow, 1, N);

            List<Double> lineResults = new ArrayList<>();
            for (Double[] doubles : polynomial) {
                Double multiplicationRes = 1.0;
                int zeroCount = 0;

                for (int g = 0; g < K; g++) {
                    if (doubles[g] == 0) zeroCount++;
                    multiplicationRes *= inputRow[doubles[g].intValue()];
                }
                if (zeroCount != K) {
                    lineResults.add(multiplicationRes);
                }
            }
            result.add(lineResults);
        }
        return result.stream().map(stream -> stream.toArray(new Double[0])).toArray(Double[][]::new);
    }
}
