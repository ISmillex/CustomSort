package custom.sorts;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;


public class CustomSort {

    // CORES determines the number of available CPU processors at runtime. This value is crucial for configuring
    // the ForkJoinPool to match the system's parallel processing capabilities, enabling efficient use of hardware resources.
    // By aligning the ForkJoinPool's thread count with the number of processors, we ensure that the workload
    // is distributed in a manner that maximizes CPU utilization without overcommitting resources.
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    // POOL is a ForkJoinPool instance initialized to use a number of threads equal to the available CPU processors.
    // This shared pool facilitates parallel execution of sorting tasks, allowing for concurrent processing
    // of different segments of the array. The aim is to leverage multi-core architectures effectively by dividing
    // the sorting work among multiple threads, thereby reducing the total computation time.
    private static final ForkJoinPool POOL = new ForkJoinPool(CORES);

    // INSERTION_SORT_THRESHOLD specifies the maximum array size for which insertion sort is preferred over quicksort.
    // Insertion sort is known to perform well for small datasets due to its low overhead and simplicity.
    // This threshold helps in switching to insertion sort for small subarrays, optimizing the overall performance
    // of the sorting algorithm by reducing the time complexity in cases where quicksort's partitioning would be less efficient.
    private static final int INSERTION_SORT_THRESHOLD = 100;

    // MIN_PARALLEL_THRESHOLD defines the minimum size of the array for which the sorting task will be executed in parallel.
    // If the array size exceeds this threshold, the sorting task is split into smaller sub-tasks, which are then
    // executed concurrently using the ForkJoin framework. This threshold ensures that overhead from task management
    // and thread coordination is justified by the performance gains from parallel execution, particularly for large datasets.
    private static final int MIN_PARALLEL_THRESHOLD = 4096;

    // MIN_SEQUENTIAL_SORTED_THRESHOLD sets the maximum size for which the algorithm checks if a segment of the array
    // is sorted sequentially rather than parallel. For segments smaller than or equal to this size, a direct sequential
    // check is performed to determine if the segment is already sorted. This avoids the overhead of splitting and
    // managing parallel tasks for small datasets, where a sequential approach is more efficient.
    private static final int MIN_SEQUENTIAL_SORTED_THRESHOLD = 1024;



    // This class, extending RecursiveTask<Boolean[]>, is designed for checking whether a segment of an array is sorted.
    // It utilizes the Fork/Join framework to potentially split the task into smaller, parallel subtasks, improving efficiency on multi-core processors.
    static class CheckSortedTask extends RecursiveTask<Boolean[]> {
        private final double[] array; // The array segment to be checked for sorted order.
        private final int left; // The starting index of the segment.
        private final int right; // The ending index of the segment.

        // Constructor initializes the task with the array segment defined by left and right indices.
        CheckSortedTask(double[] array, int left, int right) {
            this.array = array;
            this.left = left;
            this.right = right;
        }

        // Executes the main computation logic for this task.
        @Override
        protected Boolean[] compute() {
            // Determine the length of the array segment to decide on the computation strategy.
            int length = right - left + 1;

            // If the segment is small enough (under a predefined threshold), perform a sequential check.
            // This decision avoids the overhead of further task splitting for small segments.
            if (length <= MIN_SEQUENTIAL_SORTED_THRESHOLD) {
                return checkSortedSequentially(array, left, right);
            } else {
                // For larger segments, divide the task into two subtasks for parallel execution.
                // This is achieved by calculating a midpoint and creating two new CheckSortedTask instances accordingly.
                int mid = left + length / 2;
                CheckSortedTask leftTask = new CheckSortedTask(array, left, mid);
                CheckSortedTask rightTask = new CheckSortedTask(array, mid + 1, right);

                leftTask.fork(); // Asynchronously executes the left subtask in a separate thread.
                Boolean[] rightResult = rightTask.compute(); // Executes the right subtask in the current thread.
                Boolean[] leftResult = leftTask.join(); // Waits for and retrieves the result of the left subtask.

                // After both subtasks complete, their results are merged.
                // This step checks whether each segment is sorted or reverse-sorted, combining the findings.
                return mergeSortedResults(leftResult, rightResult);
            }
        }

        // Checks sequentially whether the specified segment of the array is sorted or reverse-sorted.
        // Returns a Boolean array where the first element indicates sorted order and the second indicates reverse-sorted order.
        private Boolean[] checkSortedSequentially(double[] array, int left, int right) {
            boolean isSorted = true;
            boolean isReverseSorted = true;
            // Iterate through the segment to check order. The loop breaks early if both sorted and reverse-sorted conditions are false.
            for (int i = left; i < right; i++) {
                if (array[i] > array[i + 1]) {
                    isSorted = false;
                }
                if (array[i] < array[i + 1]) {
                    isReverseSorted = false;
                }
                if (!isSorted && !isReverseSorted) {
                    break;
                }
            }
            return new Boolean[]{isSorted, isReverseSorted};
        }

        // Merges the results from two subtasks to determine if the entire segment is either sorted or reverse-sorted.
        // It considers the segment sorted only if both sub-segments are sorted, and similarly for reverse-sorted.
        private Boolean[] mergeSortedResults(Boolean[] left, Boolean[] right) {
            boolean isSorted = left[0] && right[0];
            boolean isReverseSorted = left[1] && right[1];
            return new Boolean[]{isSorted, isReverseSorted};
        }
    }



    /**
     * This class represents a sorting task designed to be executed within the ForkJoin framework.
     * It extends RecursiveAction, meaning it does not return any value upon completion.
     * The SortTask is used to sort a specified segment of an array in parallel, leveraging divide-and-conquer principles.
     * It can utilize different sorting algorithms based on the characteristics of the data segment it processes,
     * such as its size or the depth of recursion, to optimize performance.
     */
    static class SortTask extends RecursiveAction {
        // The array to be sorted. This reference allows the task to access and modify
        // the segment of the array it is responsible for.
        private final double[] array;

        // The starting index of the segment within the array to be sorted by this task.
        // This allows the task to work on a specific portion of the array without affecting the rest.
        private final int left;

        // The ending index of the segment within the array to be sorted by this task.
        // It defines the boundary of this task's scope within the array.
        private final int right;

        // A limit on the recursion depth to prevent excessive splitting into subtasks.
        // This is used to switch to a different sorting algorithm when the depth limit is reached,
        // preventing stack overflow and controlling the overhead of recursion.
        private final int depthLimit;

        SortTask(double[] array, int left, int right, int depthLimit) {
            this.array = array;
            this.left = left;
            this.right = right;
            this.depthLimit = depthLimit;
        }

        @Override
        protected void compute() {
            // Determine the current segment's size to decide on the sorting strategy.
            int size = right - left + 1;

            // If the segment size is small (below the predefined threshold), insertion sort is used for its efficiency on small arrays.
            if (size <= INSERTION_SORT_THRESHOLD) {
                insertionSort(array, left, right);
            }
            // If the recursion depth limit is reached, switch to heap sort to avoid stack overflow and to guarantee O(n log n) performance.
            else if (depthLimit <= 0) {
                heapSort(array, left, right);
            }
            // For larger segments and when depth limit hasn't been reached, proceed with the quicksort algorithm.
            else {
                // Select a pivot using a method that aims to find a value close to the median, which helps in achieving balanced partitions.
                int pivot = choosePivotByGoldenRation(array, left, right);
                // Partition the array around the chosen pivot, so that elements less than the pivot are on its left, and elements greater are on its right.
                pivot = partition(array, left, right, pivot);
                // Recursively apply the same sorting logic to the two partitions created by the pivot. This step is performed in parallel to leverage multi-core processors.
                // The depth limit is decremented with each recursive call to ensure that the algorithm does not recurse indefinitely.
                invokeAll(new SortTask(array, left, pivot - 1, depthLimit - 1),
                        new SortTask(array, pivot + 1, right, depthLimit - 1));
            }
        }



        private static void heapSort(double[] a, int low, int high) {
            // First, build a max heap from the input data.
            for (int k = (low + high) >>> 1; k > low; ) {
                pushDown(a, --k, a[k], low, high);
            }
            // Then, one by one, extract elements from the heap.
            while (--high > low) {
                double max = a[low];
                pushDown(a, low, a[high], low, high);
                a[high] = max;
            }
        }

        // This method pushes down the element at index p to its correct position in the heap.
        // It is used to restore the heap property after an element is removed or replaced.
        private static void pushDown(double[] a, int p, double value, int low, int high) {
            for (int k ;; a[p] = a[p = k]) {
                k = (p << 1) - low + 2; // Compute the index of the left child of p.

                // If the left child is not within the heap, break.
                if (k > high) {
                    break;
                }
                // If the right child is within the heap and is greater than the left child, increment k to point to the right child.
                if (k == high || a[k] < a[k - 1]) {
                    --k;
                }
                // If the value at the child is less than or equal to the value being pushed down, break.
                if (a[k] <= value) {
                    break;
                }
            }
            // Store the value being pushed down at its correct position in the heap.
            a[p] = value;
        }




        private void insertionSort(double[] array, int left, int right) {
            // Iterate over the array from the second element to the last
            for (int i = left + 1; i <= right; i++) {
                // Store the current element as the key
                double key = array[i];
                int j = i;
                // Move elements of array[0..i-1], that are greater than key, to one position ahead of their current position
                while (j > left && array[j - 1] > key) {
                    array[j] = array[j - 1];
                    j--;
                }
                // Place the key in its correct location
                array[j] = key;
            }
        }


        private int choosePivotByGoldenRation(double[] a, int left, int right) {
            // Calculate the size of the portion of the array to be sorted
            int size = right - left + 1;
            // Determine the step size based on the golden ratio approximation, to choose sample elements for pivot selection
            int step = (size >> 3) * 3 + 3;

            // Select five elements from the array based on the calculated step, spread across the array
            int e1 = left + step;
            int e5 = right - step;
            int e3 = (e1 + e5) >>> 1; // Middle of e1 and e5
            int e2 = (e1 + e3) >>> 1; // Middle of e1 and e3
            int e4 = (e3 + e5) >>> 1; // Middle of e3 and e5
            double a3 = a[e3]; // Element in the middle of the five selected elements

            // Ensure e2 and e5 are in ascending order, swapping if necessary
            if (a[e5] < a[e2]) { double t = a[e5]; a[e5] = a[e2]; a[e2] = t; }
            // Ensure e1 and e4 are in ascending order, swapping if necessary
            if (a[e4] < a[e1]) { double t = a[e4]; a[e4] = a[e1]; a[e1] = t; }
            // Ensure e4 and e5 are in ascending order, swapping if necessary
            if (a[e5] < a[e4]) { double t = a[e5]; a[e5] = a[e4]; a[e4] = t; }
            // Ensure e1 and e2 are in ascending order, swapping if necessary
            if (a[e2] < a[e1]) { double t = a[e2]; a[e2] = a[e1]; a[e1] = t; }
            // Ensure e2 and e4 are in ascending order, swapping if necessary
            if (a[e4] < a[e2]) { double t = a[e4]; a[e4] = a[e2]; a[e2] = t; }

            // Adjust the position of the middle element (a3) based on its value relative to the other selected elements
            // This process ensures that the pivot (e3) is reasonably central among the selected elements
            if (a3 < a[e2]) {
                if (a3 < a[e1]) {
                    a[e3] = a[e2]; a[e2] = a[e1]; a[e1] = a3;
                } else {
                    a[e3] = a[e2]; a[e2] = a3;
                }
            } else if (a3 > a[e4]) {
                if (a3 > a[e5]) {
                    a[e3] = a[e4]; a[e4] = a[e5]; a[e5] = a3;
                } else {
                    a[e3] = a[e4]; a[e4] = a3;
                }
            }

            // Return the index of the chosen pivot element
            return e3;
        }



        private int partition(double[] array, int left, int right, int pivotIndex) {
            // Fetch the pivot value from the array using the pivotIndex.
            double pivotValue = array[pivotIndex];
            // Move the pivot element to the end of the section being partitioned.
            swap(array, pivotIndex, right);

            // Initialize storeIndex to the starting index of the section being partitioned.
            // This index will be used to separate values less than the pivot.
            int storeIndex = left;
            // Iterate over each element in the section, excluding the pivot at the end.
            for (int i = left; i < right; i++) {
                // If the current element is less than the pivot value,
                // swap it with the element at storeIndex, and increment storeIndex.
                if (array[i] < pivotValue) {
                    swap(array, i, storeIndex);
                    storeIndex++;
                }
            }
            // After all elements have been processed, swap the pivot (currently at the rightmost position of the section)
            // with the element at storeIndex. This positions the pivot correctly in the middle, with all elements less than
            // the pivot to its left and all greater to its right.
            swap(array, storeIndex, right);

            // Return the final position of the pivot element.
            return storeIndex;
        }


        private void swap(double[] array, int i, int j) {
            double temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    // Searches for the index of the first occurrence of zero in a sorted array using binary search.
    private static int findFirstZeroIndex(double[] array, int left, int right) {
        // Continues searching as long as 'left' is less than or equal to 'right'
        while (left <= right) {
            // Calculates the middle index of the current segment.
            // The '>>>' operator ensures that the result is an unsigned integer (avoids overflow issues).
            int middle = (left + right) >>> 1;

            // If the element at the middle index is negative, zero is not found in the left half,
            // so the search is continued in the right half by updating 'left'.
            if (array[middle] < 0) {
                left = middle + 1;
            }
            // If the element at the middle is positive, zero is not found in the right half,
            // so the search is continued in the left half by updating 'right'.
            else if (array[middle] > 0) {
                right = middle - 1;
            }
            // If the element at the middle is exactly zero, its index is returned as the first occurrence.
            else {
                return middle;
            }
        }
        // If zero is not found in the array, return -1.
        return -1;
    }

    private static void reverse(double[] array, int left, int right) {
        while (left < right) {
            double temp = array[left];
            array[left++] = array[right];
            array[right--] = temp;
        }
    }

    //  Checks if an array segment is potentially sorted or reverse sorted by examining three key groups within the segment.
    private static boolean isPotentialSortedOrReverse(double[] array, int left, int right) {
        // Calculate the starting index of the middle group. This divides the segment into three groups for checking.
        int midStart = left + (right - left) / 2 - 2;

        // Check if the first group (starting from 'left') is sorted or reverse sorted.
        boolean group1Sorted = isGroupSortedOrReverseSorted(array, left, left + 4);
        // Check if the middle group is sorted or reverse sorted.
        boolean group2Sorted = isGroupSortedOrReverseSorted(array, midStart, midStart + 4);
        // Check if the last group (ending at 'right') is sorted or reverse sorted.
        boolean group3Sorted = isGroupSortedOrReverseSorted(array, right - 4, right);

        // If any of the three groups are not sorted or reverse sorted, the whole segment is considered not sorted.
        if (!group1Sorted || !group2Sorted || !group3Sorted) {
            return false;
        }

        // Check if the entire segment is overall sorted by ensuring the end of the first group
        // is less than or equal to the middle of the middle group, and the middle of the middle group
        // is less than or equal to the start of the last group.
        boolean isOverallSorted = array[left + 4] <= array[midStart + 2] && array[midStart + 2] <= array[right - 4];
        // Check if the entire segment is overall reverse sorted by ensuring the end of the first group
        // is greater than or equal to the middle of the middle group, and the middle of the middle group
        // is greater than or equal to the start of the last group.
        boolean isOverallReverseSorted = array[left + 4] >= array[midStart + 2] && array[midStart + 2] >= array[right - 4];

        // The segment is considered potentially sorted or reverse sorted if either condition above holds true.
        return isOverallSorted || isOverallReverseSorted;
    }


    // Determines if a specified segment of an array is sorted in ascending order or descending order.
    private static boolean isGroupSortedOrReverseSorted(double[] array, int start, int end) {
        // Initially assume both sorted and reverseSorted to be true.
        boolean sorted = true;
        boolean reverseSorted = true;

        // Iterate through the segment of the array to check ordering between consecutive elements.
        for (int i = start + 1; i <= end; i++) {
            // If the current element is less than the previous one, it's not sorted in ascending order.
            if (array[i] < array[i - 1]) {
                sorted = false;
            }
            // If the current element is greater than the previous one, it's not sorted in descending order.
            if (array[i] > array[i - 1]) {
                reverseSorted = false;
            }
            // If the segment is neither sorted nor reverseSorted, return false immediately.
            if (!sorted && !reverseSorted) {
                return false;
            }
        }

        // Return true if the segment is either sorted in ascending order or descending order.
        return sorted || reverseSorted;
    }


    // Define a method for sorting an array segment using different strategies based on segment size and order.
    private static void sortArray(double[] array, int left, int right) {
        // Calculate the size of the array segment to be sorted.
        int size = right - left + 1;

        // Check if the segment size is greater than 100 elements.
        if (size > 100) {
            // Check if the array segment might already be sorted or in reverse order.
            if (isPotentialSortedOrReverse(array, left, right)) {
                // For very large segments (over 10,000 elements), use a parallel task to check order.
                if (size > 10000) {
                    CheckSortedTask checkSortedTask = new CheckSortedTask(array, left, right);
                    POOL.invoke(checkSortedTask); // Execute the task in a thread pool.
                    Boolean[] result = checkSortedTask.join(); // Wait for the result.
                    if (result[0]) return; // If the array is already sorted, return immediately.
                    if (result[1]) {
                        reverse(array, left, right); // If in reverse order, reverse the segment.
                        return;
                    }
                } else {
                    // For segments smaller than 10,000 elements, check order without parallelism.
                    boolean sortedOrReverse = isGroupSortedOrReverseSorted(array, left, right);
                    if (sortedOrReverse) return; // If sorted (or reverse sorted), no further action needed.
                }
            }
        } else {
            // For segments of 100 elements or fewer, check if sorted or reverse sorted without parallelism.
            boolean sortedOrReverse = isGroupSortedOrReverseSorted(array, left, right);
            if (sortedOrReverse) return; // If sorted, return immediately.
        }

        // For unsorted segments, or larger segments requiring sorting, delegate to a sorting method.
        sortArray(array, left, right, size);
    }

    // Overloaded sortArray method to handle actual sorting, including handling special cases like negative zeros.
    public static void sortArray(double[] array, int left, int right, int size) {
        int numNegativeZero = 0; // Counter for negative zero values within the segment.

        // Partition special values (NaNs and negative zeros) before sorting.
        for (int k = right; k > left; ) {
            double ak = array[--k];
            // Check for and handle negative zeros, ensuring they are treated distinctly from positive zeros.
            if (ak == 0.0d && Double.doubleToRawLongBits(ak) < 0) {
                numNegativeZero += 1;
                array[k] = 0.0d; // Convert to positive zero for consistent sorting.
            } else if (ak != ak) { // Check for NaN values.
                // Move NaNs to the end of the segment to be excluded from sorting.
                array[k] = array[--right];
                array[right] = ak;
            }
        }

        // Determine maximum recursion depth based on segment size to prevent stack overflow.
        int maxDepth = (int) (2 * Math.floor(Math.log(size) / Math.log(2)));
        // Create a sorting task with the prepared segment.
        SortTask sortTask = new SortTask(array, left, right, maxDepth);
        // Use parallel or sequential sorting based on the segment size.
        if (size > MIN_PARALLEL_THRESHOLD) {
            POOL.invoke(sortTask); // Parallel execution for larger segments.
        } else {
            sortTask.compute(); // Sequential execution for smaller segments.
        }

        // Handle the special case where only one negative zero is present.
        if (++numNegativeZero == 1) {
            return;
        }

        // Correct the placement of negative zeros post-sorting.
        int firstZeroIndex = findFirstZeroIndex(array, left, right);
        if (firstZeroIndex >= 0) {
            long negativeZeroBits = Double.doubleToRawLongBits(-0.0d);
            // Replace the appropriate number of positive zeros with negative zeros.
            for (int i = 0; i < numNegativeZero; i++) {
                array[firstZeroIndex + i] = Double.longBitsToDouble(negativeZeroBits);
            }
        }
    }

    // Entry method to sort an entire array.
    public static void sort(double[] a) {
        // Delegate to sortArray for the entire array range.
        sortArray(a, 0, a.length - 1);
    }


    public static void main(String[] args) {
        double[] array = new double[10000];
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.random() * 1000000;
        }
        sort(array);
        System.out.println(Arrays.toString(array));
    }
}
