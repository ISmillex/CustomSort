import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import java.io.FileWriter;
import java.io.IOException;


public class ArraySorterBenchmark {
    private static  int NUM_WARMUP_RUNS = 3;
    private static  int NUM_TIMED_RUNS = 5;
    private static  int NUM_ITERATIONS = 1;
    private static  int[] ARRAY_LENGTHS = {5, 10, 100, 1000};

    private static boolean isSameSort = false;
    private static String SAME_SORT_REAL_NAME = "";
    private static String[] ARRAY_TYPES = {"Random Wide","Random Narrow", "Nearly Sorted",
            "Reverse Sorted", "High Variance", "Small Numbers",
            "Large Numbers", "Equal Distrib", "Unequal Distrib"};
    private static String[] SORTING_ALGORITHMS = {"java.util.Arrays::sort", "java.util.Arrays::parallelSort", "org.example.CustomSort::sort"};

    private static final Map<String, Double> totalPercentageImprovements = new HashMap<>();
    private static final Map<String, Integer> winCounts = new HashMap<>();


    public static double[] copyArray(double[] array) {
        return Arrays.copyOf(array, array.length);
    }

    public static double testSortingAlgorithm(double[] array, Consumer<double[]> sortFunction) {
        double totalTime = 0;

        for (int i = 0; i < NUM_WARMUP_RUNS; i++) {
            double[] copy = copyArray(array);
            sortFunction.accept(copy);
        }

        for (int i = 0; i < NUM_TIMED_RUNS; i++) {
            double[] copy = copyArray(array);
            long start = System.nanoTime();
            sortFunction.accept(copy);
            long end = System.nanoTime();
            totalTime += (end - start);
        }

        return totalTime / (NUM_TIMED_RUNS * 1_000_000.0);
    }


    private static void runIterationsPrint(int currentIteration, FileWriter writer) throws IOException {
        String iterationString = String.format("\n\niteration %d/%d%n\n", currentIteration + 1, NUM_ITERATIONS);
        System.out.println(iterationString);
        writer.write(iterationString);

        List<String> headers = new ArrayList<>(Arrays.asList("ArrayType", "Length"));
        int maxAlgorithmLength = calculateMaxAlgorithmNameLength(0);

        headers.addAll(Arrays.asList(SORTING_ALGORITHMS));

        String formatString = "%-20s%-10s";
        for (int i = 0; i < SORTING_ALGORITHMS.length; i++) {
            formatString += "%-" + (maxAlgorithmLength + 5) + "s";
        }
        formatString += "%n";


        System.out.printf(formatString, headers.toArray());
        writer.write(String.format(formatString, headers.toArray()));
        System.out.println(String.join("", Collections.nCopies(30 * (3 + SORTING_ALGORITHMS.length), "-")));
        writer.write(String.join("", Collections.nCopies(30 * (3 + SORTING_ALGORITHMS.length), "-")) + "\n");
    }

    private static void runIterations(int currentIteration, FileWriter writer) throws IOException{
        runIterationsPrint(currentIteration, writer);
        for (int length : ARRAY_LENGTHS) {
            for (String arrayType : ARRAY_TYPES) {
                double[] array = ArraysTypes.generateArray(length, arrayType);
                testSortingAlgorithms(length, array, arrayType, writer);
            }
        }
        printBenchmarkSummary(writer);
    }

    private static Consumer<double[]> stringToConsumer(String algorithm) {
        return arr -> {
            try {
                String[] parts = algorithm.split("::");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid method reference: " + algorithm);
                }
                String className = parts[0];
                String methodName = parts[1];

                Class<?> clazz = Class.forName(className);
                Method method = clazz.getDeclaredMethod(methodName, double[].class);
                method.invoke(null, (Object) arr);
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute sorting algorithm: " + algorithm, e);
            }
        };
    }


    private static void testSortingAlgorithms(int length, double[] array, String arrayType, FileWriter writer) throws IOException {
        int maxAlgorithmNameLength = calculateMaxAlgorithmNameLength(0);
        List<Double> times = gatherSortingTimes(array);
        String[] winnerInfo = determineWinnerAndUpdateStats(times);
        printFormattedResults(length, arrayType, times, winnerInfo, maxAlgorithmNameLength, writer);
    }

    private static int calculateMaxAlgorithmNameLength(int margin) {
        int maxAlgorithmNameLength = Arrays.stream(SORTING_ALGORITHMS).mapToInt(String::length).max().orElse(25) + margin;
        maxAlgorithmNameLength = Math.max(maxAlgorithmNameLength, "Sorting Algorithm".length());
        return maxAlgorithmNameLength;
    }

    private static List<Double> gatherSortingTimes(double[] array) {
        List<Double> times = new ArrayList<>();

        if (isSameSort) {
            for (String algorithm : SORTING_ALGORITHMS) {
                double time = testSortingAlgorithm(array, stringToConsumer(SAME_SORT_REAL_NAME));
                times.add(time);
            }
        } else {
            for (String algorithm : SORTING_ALGORITHMS) {
                double time = testSortingAlgorithm(array, stringToConsumer(algorithm));
                times.add(time);
            }
        }

        return times;
    }

    private static String[] determineWinnerAndUpdateStats(List<Double> times) {
        double minTime = Collections.min(times);
        double maxTime = Collections.max(times);
        String winner = "";
        double percentageImprovement = 0.0;

        for (int i = 0; i < SORTING_ALGORITHMS.length; i++) {
            if (times.get(i) == minTime) {
                winner = SORTING_ALGORITHMS[i];
                percentageImprovement = 100.0 * (maxTime - minTime) / maxTime;
                totalPercentageImprovements.put(winner, totalPercentageImprovements.getOrDefault(winner, 0.0) + percentageImprovement);
                winCounts.put(winner, winCounts.getOrDefault(winner, 0) + 1);
                break; // Assuming only one winner
            }
        }
        return new String[]{winner, String.valueOf(percentageImprovement)};
    }

    private static void printFormattedResults(int length, String arrayType, List<Double> times, String[] winnerInfo, int maxAlgorithmLength, FileWriter writer) throws IOException {
        String formatString = "%-20s%-10d";
        for (int i = 0; i < SORTING_ALGORITHMS.length; i++) {
            formatString += " %-" + (maxAlgorithmLength + 5) + ".4f";
        }
        formatString += " Winner: %s, faster by %.2f%%\n";
        Object[] formatArgs = new Object[2 + SORTING_ALGORITHMS.length + 2];
        formatArgs[0] = arrayType;
        formatArgs[1] = length;
        for (int i = 0; i < times.size(); i++) {
            formatArgs[i + 2] = times.get(i);
        }
        formatArgs[formatArgs.length - 2] = winnerInfo[0];
        formatArgs[formatArgs.length - 1] = Double.parseDouble(winnerInfo[1]);
        System.out.printf(formatString, formatArgs);
        writer.write(String.format(formatString, formatArgs));
    }


    private static void printSeparator(int length, FileWriter writer) throws IOException {
        for (int i = 0; i < length; i++) {
            String separator = "-";
            System.out.print(separator);
            writer.write(separator);
        }
        System.out.println();
        writer.write("\n");
    }

    private static void printOverallWinner(int maxAlgorithmNameLength, FileWriter writer) throws IOException {
        String overallWinner = "";
        double maxAvgImprovement = Double.NEGATIVE_INFINITY;
        int maxWins = 0;
        String nextBestAlgorithm = "";
        double nextBestAvgImprovement = Double.NEGATIVE_INFINITY;

        for (String algorithm : SORTING_ALGORITHMS) {
            double avgImprovement = totalPercentageImprovements.getOrDefault(algorithm, 0.0) / winCounts.getOrDefault(algorithm, 0);
            int wins = winCounts.getOrDefault(algorithm, 0);

            if (avgImprovement > maxAvgImprovement || (avgImprovement == maxAvgImprovement && wins > maxWins)) {
                nextBestAlgorithm = overallWinner;
                nextBestAvgImprovement = maxAvgImprovement;
                overallWinner = algorithm;
                maxAvgImprovement = avgImprovement;
                maxWins = wins;
            } else if (avgImprovement > nextBestAvgImprovement) {
                nextBestAlgorithm = algorithm;
                nextBestAvgImprovement = avgImprovement;
            }
        }

        double percentageBetterThanNextBest = maxAvgImprovement - nextBestAvgImprovement;
        String formattedOutput = String.format("Overall Winner: %-" + maxAlgorithmNameLength + "s %.2f%% better than %-" + maxAlgorithmNameLength + "s%n%n", overallWinner, percentageBetterThanNextBest, nextBestAlgorithm);
        System.out.println(formattedOutput);
        writer.write(formattedOutput);
    }


    private static void printResultsTable(int maxAlgorithmNameLength, FileWriter writer) throws IOException {
        int totalPossibleWins = NUM_ITERATIONS * ARRAY_LENGTHS.length * ARRAY_TYPES.length;
        String string = "\n\n\nDisplaying overall results...\n";
        System.out.println(string);
        writer.write(string);

        String headingFormat = String.format("%%-%ds%%-25s%%-15s%%n", maxAlgorithmNameLength);
        System.out.printf(headingFormat, "Sorting Algorithm", "Average % Improvement", "Wins");
        writer.write(String.format(headingFormat, "Sorting Algorithm", "Average % Improvement", "Wins"));
        printSeparator(maxAlgorithmNameLength + 25 + 15, writer);
        String rowFormat = String.format("%%-%ds%%-25.2f%%-15s", maxAlgorithmNameLength);
        for (String algorithm : SORTING_ALGORITHMS) {
            double avgImprovement = totalPercentageImprovements.get(algorithm) / winCounts.get(algorithm);
            System.out.printf(rowFormat, algorithm, avgImprovement, winCounts.get(algorithm) + "/" + totalPossibleWins + " wins");
            writer.write(String.format(rowFormat, algorithm, avgImprovement, winCounts.get(algorithm) + "/" + totalPossibleWins + " wins"));
            System.out.println();
            writer.write("\n");
        }
        System.out.println("\n");
        writer.write("\n");
    }


    private static void printBenchmarkSummary(FileWriter writer) throws IOException {
        int maxAlgorithmNameLength = calculateMaxAlgorithmNameLength(5);
        printResultsTable(maxAlgorithmNameLength, writer);
        printOverallWinner(maxAlgorithmNameLength, writer);
    }

    private static void setUpBenchmark(String[] args) {
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argMap.put(args[i], args[i + 1]);
        }

        if (argMap.containsKey("-warmup")) {
            NUM_WARMUP_RUNS = Integer.parseInt(argMap.get("-warmup"));
        }
        if (argMap.containsKey("-timed")) {
            NUM_TIMED_RUNS = Integer.parseInt(argMap.get("-timed"));
        }
        if (argMap.containsKey("-iterations")) {
            NUM_ITERATIONS = Integer.parseInt(argMap.get("-iterations"));
        }
        if (argMap.containsKey("-lengths")) {
            String[] lengths = argMap.get("-lengths").split(",");
            ARRAY_LENGTHS = new int[lengths.length];
            for (int i = 0; i < lengths.length; i++) {
                ARRAY_LENGTHS[i] = Integer.parseInt(lengths[i].trim());
            }
        }
        if (argMap.containsKey("-types")) {
            ARRAY_TYPES = argMap.get("-types").split(",");
        }
        if (argMap.containsKey("-algorithms")) {
            SORTING_ALGORITHMS = argMap.get("-algorithms").split(",");
        }

        // Ensure each string is trimmed
        for (int i = 0; i < ARRAY_TYPES.length; i++) {
            ARRAY_TYPES[i] = ARRAY_TYPES[i].trim();
        }
        for (int i = 0; i < SORTING_ALGORITHMS.length; i++) {
            SORTING_ALGORITHMS[i] = SORTING_ALGORITHMS[i].trim();
        }


        Set<String> set = new HashSet<>();
        for (int i = 0; i < SORTING_ALGORITHMS.length; i++) {
            if (!set.add(SORTING_ALGORITHMS[i])) {
                isSameSort = true;
                SAME_SORT_REAL_NAME = SORTING_ALGORITHMS[i];
                SORTING_ALGORITHMS[i] = SORTING_ALGORITHMS[i] + i;
            }
        }


    }

    public static void main(String[] args) {
        try {
            FileWriter writer = new FileWriter("benchmark_results.txt");
            setUpBenchmark(args);
            System.out.println("Starting ArraySorter Benchmark...\n");
            writer.write("Starting ArraySorter Benchmark...\n\n");

            System.out.println("Benchmark Parameters:");
            writer.write("Benchmark Parameters:\n");
            System.out.println("java ArraySorterBenchmark " + String.join(" ", args));
            writer.write("java ArraySorterBenchmark " + String.join(" ", args) + "\n");

            for (int iteration = 0; iteration < NUM_ITERATIONS; iteration++) {
                runIterations(iteration, writer);
            }
            writer.close();
        }
        catch (IOException e) {
            System.out.println("An error occurred while writing to the file. Exiting...");
            e.printStackTrace();
        }

    }
}









