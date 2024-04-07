import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ArraysTypes {
    public static double[] generateArray(int length, String arrayType) {
        return switch (arrayType) {
            case "Random Wide" -> generateWideRangeRandomArray(length);
            case "Random Narrow" -> generateNarrowRangeRandomArray(length);
            case "Nearly Sorted" -> generateNearlySortedArray(length);
            case "Reverse Sorted" -> generateReverseSortedArray(length);
            case "High Variance" -> generateHighVarianceArray(length);
            case "Small Numbers" -> generateSmallNumbersArray(length);
            case "Large Numbers" -> generateLargeNumbersArray(length);
            case "Equal Distrib" -> generateEqualDistributionArray(length);
            case "Unequal Distrib" -> generateUnequalDistributionArray(length);
            default -> throw new IllegalArgumentException("Invalid array type: " + arrayType);
        };
    }

    private static double[] generateWideRangeRandomArray(int length) {
        double[] array = new double[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = ThreadLocalRandom.current().nextDouble(Integer.MIN_VALUE, (double) Integer.MAX_VALUE + 1);
        }
        return array;
    }

    private static double[] generateNarrowRangeRandomArray(int length) {
        double[] array = new double[length];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextDouble() * 200 - 100; // Range [-100, 100)
        }
        return array;
    }

    private static double[] generateNearlySortedArray(int length) {
        double[] array = new double[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }
        Random random = new Random();
        int swaps = (int)(length * 0.05); // 5% of elements are out of order
        for (int i = 0; i < swaps; i++) {
            int pos1 = random.nextInt(length);
            int pos2 = random.nextInt(length);
            double temp = array[pos1];
            array[pos1] = array[pos2];
            array[pos2] = temp;
        }
        return array;
    }

    private static double[] generateReverseSortedArray(int length) {
        double[] array = new double[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = length - i;
        }
        return array;
    }

    private static double[] generateHighVarianceArray(int length) {
        double[] array = new double[length];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            if (random.nextBoolean()) {
                array[i] = random.nextDouble() * 1E10; // Large values
            } else {
                array[i] = random.nextDouble(); // Small values
            }
        }
        return array;
    }

    private static double[] generateSmallNumbersArray(int length) {
        double[] array = new double[length];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextDouble() * 10;
        }
        return array;
    }

    private static double[] generateLargeNumbersArray(int length) {
        double[] array = new double[length];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextDouble() * 1_000_000_000;
        }
        return array;
    }

    private static double[] generateEqualDistributionArray(int length) {
        double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = (double) i / (length - 1) * 100; // Evenly distribute between 0 and 100
        }
        // Shuffle to remove any ordered sequence while maintaining distribution
        shuffleArray(array);
        return array;
    }

    private static double[] generateUnequalDistributionArray(int length) {
        double[] array = new double[length];
        Random random = new Random();

        // Split the array into segments with different distributions
        int segment = length / 3;
        for (int i = 0; i < segment; i++) {
            array[i] = random.nextDouble() * 30; // Lower third: Concentrate in the range [0, 30)
        }
        for (int i = segment; i < 2 * segment; i++) {
            array[i] = 30 + random.nextDouble() * 40; // Middle third: Concentrate in the range [30, 70)
        }
        for (int i = 2 * segment; i < length; i++) {
            array[i] = 70 + random.nextDouble() * 30; // Upper third: Concentrate in the range [70, 100)
        }

        // Optionally shuffle the array to remove any ordered sequence while maintaining the overall unequal distribution
        shuffleArray(array);

        return array;
    }


    private static void shuffleArray(double[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            double temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}



