
# Custom Sort

The objective of this project is to develop an optimized comparison-based sorting algorithm that outperforms Java's built-in Arrays.sort and Arrays.parallelSort methods when sorting arrays of double values.

# Quicksort Implementation and Pivot Selection

Quicksort is a divide-and-conquer algorithm that selects a 'pivot' element from the array and partitions the other elements into two sub-arrays, according to whether they are less than or greater than the pivot. The sub-arrays are then sorted recursively. This implementation of quicksort is unique in its method of pivot selection, which is based on a golden ratio approximation to choose a pivot that is close to the median. This approach aims to achieve more balanced partitions, which is crucial for maintaining quicksort's average-case time complexity of O(n log n). The use of a golden ratio for pivot selection is a sophisticated strategy that can reduce the likelihood of encountering the worst-case time complexity of O(n²), which occurs with poorly chosen pivots.

# Parallel Execution with Fork/Join Framework

Java's Fork/Join framework allows for recursive task-based parallelism, where a large task is split into smaller sub-tasks that are executed concurrently by multiple threads. This class leverages the framework to implement parallel versions of the quicksort algorithm, as well as for checking if segments of the array are already sorted. By executing these tasks in parallel, the algorithm can significantly reduce the total computation time, especially on multi-core processors. This is particularly effective for large datasets where the overhead of managing parallel tasks is offset by the performance gains from concurrent execution.

# Dynamic Algorithm Selection

The CustomSort class dynamically selects between insertion sort, heap sort, and quicksort based on the characteristics of the data segment being sorted:

**Insertion Sort:** Used for small segments (below a threshold size), where its simplicity and low overhead offer better performance than more complex algorithms.

**Heap Sort:** Utilized when the recursion depth limit is reached, providing a guaranteed O(n log n) performance without further recursion, thus avoiding stack overflow risks.

**Quicksort:** The primary algorithm for larger segments, with the aforementioned sophisticated pivot selection strategy to enhance performance and maintain balanced partitions.

# Advantages Over Arrays.sort and Arrays.parallelSort

while dual-pivot quicksort has its advantages in reducing the recursion depth and potentially the number of comparisons, the single-pivot approach, when executed in parallel, can leverage simpler partitioning logic, more effective load balancing, reduced overhead, and better cache utilization to outperform dual-pivot quicksort under certain conditions and data characteristics.

##### Simpler Partitioning Logic:
Single-pivot quicksort has a straightforward partitioning logic that splits the data into two segments. This simplicity can lead to more predictable and manageable task sizes for parallel execution, reducing overhead and potentially increasing efficiency.

##### Load Balancing:
In a parallel execution environment, load balancing—the distribution of work across processor cores—is crucial for performance. The binary nature of single-pivot partitioning (each partition operation results in two sub-tasks) lends itself well to dynamic load balancing in a fork/join framework. Although dual-pivot quicksort can theoretically reduce the depth of recursion, its three-way partitioning can lead to more variable task sizes, which may not always distribute as evenly across cores, especially in the presence of data that does not partition well into three distinct segments.

##### Reduced Overhead:
The overhead from managing multiple pivots and additional array segments in dual-pivot quicksort can negate its benefits in a parallel context. Each additional pivot requires additional comparisons and swaps during partitioning, which can increase the overhead, especially if the data does not lend itself well to being divided into three distinct segments. In contrast, single-pivot quicksort's lower overhead can make it more efficient when the sorting task is distributed across many processors.

##### Effective Use of Cache and Memory Access Patterns:
Single-pivot quicksort tends to have more predictable memory access patterns due to its binary partitioning. This predictability can lead to better cache utilization and less cache contention among cores, improving overall performance in parallel execution environments.

##### Data Size and Array Lengths:
For very large datasets, the reduced overhead and efficient parallelization of single-pivot quicksort can outweigh the theoretical efficiency of dual-pivot's reduced depth of recursion, especially when the Fork/Join framework is used to its full potential.

# Benchmarks

The ArraySorterBenchmark class is designed to provide a comprehensive and customizable framework for evaluating and comparing the performance of different sorting algorithms across a variety of conditions.

##### Command-Line Arguments and Their Effects
-warmup: Specifies the number of warm-up runs (NUM_WARMUP_RUNS) to be conducted before the actual timed runs. Warm-up runs are crucial for allowing the Java Virtual Machine (JVM) to perform just-in-time (JIT) compilation, thus ensuring that the code is optimized before performance measurements are taken. Increasing the number of warm-up runs can lead to more stable and reliable benchmarking results by reducing the impact of JIT compilation times on the measured performance.

-timed: Determines the number of timed runs (NUM_TIMED_RUNS) for each sorting algorithm on each array configuration. The results from these runs are averaged to calculate the final performance metric for that configuration. More timed runs can provide a more accurate average by smoothing out fluctuations in individual run times, but they also lengthen the total benchmarking time.

-iterations: Sets the number of iterations (NUM_ITERATIONS) the entire benchmark suite will be repeated. Multiple iterations can help in identifying performance anomalies and ensuring the consistency of the results across runs. This is particularly useful when benchmarking under environments with varying loads or when attempting to mitigate external factors that might influence the performance measurements.

-lengths: Defines the lengths of the arrays (ARRAY_LENGTHS) to be sorted, allowing the benchmark to assess how the sorting algorithms perform as the size of the dataset changes. This is critical for understanding the scalability of each algorithm and how well it adapts to sorting small versus large datasets.

-types: Specifies the types of arrays (ARRAY_TYPES) to be generated and sorted, covering a range of data distributions such as randomly ordered, nearly sorted, or reverse sorted arrays. This diversity ensures a thorough evaluation of each algorithm's adaptability and efficiency across different data characteristics.

-algorithms: Lists the sorting algorithms (SORTING_ALGORITHMS) to be benchmarked, including Arrays.sort(), Arrays.parallelSort(), and custom implementations like CustomSort.sort(). This argument enables direct performance comparisons between different sorting approaches under identical conditions.

##### Array Types
The benchmark generates arrays based on the specified types, which simulate various real-world and theoretical data conditions:

Random Wide: Arrays with elements randomly distributed across a wide range, testing the algorithm's general performance.

Random Narrow: Arrays with elements randomly distributed but within a narrow range, testing the algorithm's handling of duplicates and near-duplicates.

Nearly Sorted: Arrays that are already almost in order, challenging the algorithm's efficiency in recognizing and exploiting existing order.

Reverse Sorted: Arrays sorted in reverse order, testing the algorithm's ability to handle worst-case scenarios.

High Variance: Arrays with a high variance in element values, assessing the algorithm's performance with large jumps in value.

Small Numbers: Arrays consisting of small numbers, often testing the algorithm's behavior with integer or floating-point precision issues.

Large Numbers: Arrays with large numbers, which can test the algorithm's handling of larger value ranges and potential overflow issues.

Equal Distrib: Arrays with elements distributed equally among a set of values, challenging the algorithm's efficiency with high duplication rates.

Unequal Distrib: Arrays where some values are disproportionately represented, testing the algorithm's adaptability to skewed distributions.

##### Comparison Method
The comparison between sorting algorithms is made by measuring the time it takes for each algorithm to sort each array type and size configuration. Here's the process:

Timing: For each array generated based on the specified types and sizes, the benchmark records the time each sorting algorithm takes to complete the sorting operation. This is done across several timed runs to ensure accuracy.

Analysis: After collecting timing data, the framework calculates the average time taken by each algorithm for each array configuration. This average time serves as the primary performance metric.

Winner Determination: The algorithm with the lowest average time for a given array type and size is deemed the "winner" for that configuration, indicating it is the most efficient under those specific conditions.

Statistical Aggregation: The framework aggregates these results to provide an overview of each algorithm's performance, including total wins and average percentage improvement over others. This offers a broad perspective on the strengths and weaknesses of each sorting approach across all tested scenarios.


##### Testing same algorithms

All benchmarks were executed on an Apple MacBook Air 2020 M1 with 16GB RAM.

##### 1. Comparing Arrays.sort with itself

To determine the optimal array sizes for comparing sorting algorithms, we first tested Arrays.sort against itself using random wide arrays. Since they process the same array NUM_TIMED_RUNS times, the difference should not be significant.

```
java ArraySorterBenchmark -warmup 5 -timed 10 -iterations 10 -lengths "10,100,1000,10000,100000,1000000,10000000" -types "Random Wide" -algorithms "java.util.Arrays::sort,java.util.Arrays::sort"
```

```
iteration 1/10

ArrayType           Length    java.util.Arrays::sort      java.util.Arrays::sort1     
------------------------------------------------------------------------------------------------------------------------------------------------------
Random Wide         10         0.0309                       0.0272                       Winner: java.util.Arrays::sort1, faster by 12.07%
Random Wide         100        0.0448                       0.0474                       Winner: java.util.Arrays::sort, faster by 5.47%
Random Wide         1000       0.0899                       0.0771                       Winner: java.util.Arrays::sort1, faster by 14.24%
Random Wide         10000      0.7832                       0.6180                       Winner: java.util.Arrays::sort1, faster by 21.09%
Random Wide         100000     5.7129                       5.6494                       Winner: java.util.Arrays::sort1, faster by 1.11%
Random Wide         1000000    69.7735                      69.7710                      Winner: java.util.Arrays::sort1, faster by 0.00%
Random Wide         10000000   802.7906                     799.7930                     Winner: java.util.Arrays::sort1, faster by 0.37%



Displaying overall results...
Sorting Algorithm           Average % Improvement    Wins           
--------------------------------------------------------------------
java.util.Arrays::sort      5.47                     1/70 wins      
java.util.Arrays::sort1     8.15                     6/70 wins      

Overall Winner: java.util.Arrays::sort1      2.67% better than java.util.Arrays::sort
```

iteration 1 is only shown full output can be found in benchmark folder

The results showed that using array sizes smaller than 100,000 elements led to significant percentage differences, which are not optimal for comparing sorting algorithms due to memory effects.

##### 2. Comparing Arrays.parallelSort with itself

We tested the same scenario with Arrays.parallelSort:

```
java ArraySorterBenchmark -warmup 5 -timed 10 -iterations 10 -lengths "10,100,1000,10000,100000,1000000,10000000" -types "Random Wide" -algorithms "java.util.Arrays::parallelSort,java.util.Arrays::parallelSort"
```

```
iteration 1/10

ArrayType           Length    java.util.Arrays::parallelSort      java.util.Arrays::parallelSort1     
------------------------------------------------------------------------------------------------------------------------------------------------------
Random Wide         10         0.0313                               0.0269                               Winner: java.util.Arrays::parallelSort1, faster by 13.89%
Random Wide         100        0.0472                               0.0501                               Winner: java.util.Arrays::parallelSort, faster by 5.64%
Random Wide         1000       0.0859                               0.0872                               Winner: java.util.Arrays::parallelSort, faster by 1.57%
Random Wide         10000      1.0263                               0.5424                               Winner: java.util.Arrays::parallelSort1, faster by 47.14%
Random Wide         100000     1.9136                               1.8597                               Winner: java.util.Arrays::parallelSort1, faster by 2.82%
Random Wide         1000000    16.9068                              17.2921                              Winner: java.util.Arrays::parallelSort, faster by 2.23%
Random Wide         10000000   197.9442                             197.9235                             Winner: java.util.Arrays::parallelSort1, faster by 0.01%
```

iteration 1/10 is only shown full output can be found in benchmark folder

The difference was even more pronounced, confirming that array sizes smaller than 100,000 are not suitable for comparing sorting algorithms.

##### 3. Comparing Arrays.sort with CustomSort

With the minimum array size determined as 100,000, we compared Arrays.sort with CustomSort across various array types:

```
java ArraySorterBenchmark -warmup 5 -timed 10 -iterations 10 -lengths 100000,1000000,10000000 -types Random Wide,Random Narrow,Nearly Sorted,Reverse Sorted,High Variance,Small Numbers,Large Numbers,Equal Distrib,Unequal Distrib -algorithms custom.sorts.CustomSort::sort,java.util.Arrays::sort
```

```
iteration 10/10

ArrayType           Length    custom.sorts.CustomSort::sort     java.util.Arrays::sort            
------------------------------------------------------------------------------------------------------------------------------------------------------
Random Wide         100000     1.6315                             5.6743                             Winner: custom.sorts.CustomSort::sort, faster by 71.25%
Random Narrow       100000     1.5300                             5.8570                             Winner: custom.sorts.CustomSort::sort, faster by 73.88%
Nearly Sorted       100000     0.6712                             2.3677                             Winner: custom.sorts.CustomSort::sort, faster by 71.65%
Reverse Sorted      100000     0.1310                             0.1378                             Winner: custom.sorts.CustomSort::sort, faster by 4.88%
High Variance       100000     1.5819                             5.6674                             Winner: custom.sorts.CustomSort::sort, faster by 72.09%
Small Numbers       100000     1.6879                             5.5066                             Winner: custom.sorts.CustomSort::sort, faster by 69.35%
Large Numbers       100000     1.5373                             5.5728                             Winner: custom.sorts.CustomSort::sort, faster by 72.41%
Equal Distrib       100000     1.5814                             5.4860                             Winner: custom.sorts.CustomSort::sort, faster by 71.17%
Unequal Distrib     100000     1.6580                             5.7875                             Winner: custom.sorts.CustomSort::sort, faster by 71.35%
Random Wide         1000000    17.5871                            68.1937                            Winner: custom.sorts.CustomSort::sort, faster by 74.21%
Random Narrow       1000000    17.5810                            69.3295                            Winner: custom.sorts.CustomSort::sort, faster by 74.64%
Nearly Sorted       1000000    6.0970                             29.9404                            Winner: custom.sorts.CustomSort::sort, faster by 79.64%
Reverse Sorted      1000000    0.8103                             1.4090                             Winner: custom.sorts.CustomSort::sort, faster by 42.49%
High Variance       1000000    14.9233                            68.4998                            Winner: custom.sorts.CustomSort::sort, faster by 78.21%
Small Numbers       1000000    16.3209                            68.4326                            Winner: custom.sorts.CustomSort::sort, faster by 76.15%
Large Numbers       1000000    16.4778                            68.9415                            Winner: custom.sorts.CustomSort::sort, faster by 76.10%
Equal Distrib       1000000    17.5529                            69.3142                            Winner: custom.sorts.CustomSort::sort, faster by 74.68%
Unequal Distrib     1000000    15.3183                            69.2279                            Winner: custom.sorts.CustomSort::sort, faster by 77.87%
Random Wide         10000000   178.9448                           836.6393                           Winner: custom.sorts.CustomSort::sort, faster by 78.61%
Random Narrow       10000000   173.3238                           815.2551                           Winner: custom.sorts.CustomSort::sort, faster by 78.74%
Nearly Sorted       10000000   67.2742                            340.6503                           Winner: custom.sorts.CustomSort::sort, faster by 80.25%
Reverse Sorted      10000000   6.1788                             14.0735                            Winner: custom.sorts.CustomSort::sort, faster by 56.10%
High Variance       10000000   195.1164                           819.1687                           Winner: custom.sorts.CustomSort::sort, faster by 76.18%
Small Numbers       10000000   169.6406                           812.2494                           Winner: custom.sorts.CustomSort::sort, faster by 79.11%
Large Numbers       10000000   199.3388                           816.8313                           Winner: custom.sorts.CustomSort::sort, faster by 75.60%
Equal Distrib       10000000   186.7783                           809.0825                           Winner: custom.sorts.CustomSort::sort, faster by 76.91%
Unequal Distrib     10000000   189.7057                           789.9318                           Winner: custom.sorts.CustomSort::sort, faster by 75.98%



Displaying overall results...
Sorting Algorithm                 Average % Improvement    Wins           
--------------------------------------------------------------------------
custom.sorts.CustomSort::sort     71.16                    266/270 wins   
java.util.Arrays::sort            17.04                    4/270 wins     

Overall Winner: custom.sorts.CustomSort::sort      54.12% better than java.util.Arrays::sort 
```

iteration 10/10 is only shown full output can be found in benchmark folder

As expected, CustomSort outperformed the single-threaded Arrays.sort in nearly every array type since it utilizes multiple cores.

##### 4. Comparing Arrays.parallelSort with CustomSort

To ensure a fair comparison between the multi-threaded algorithms, we increased the number of timed runs and iterations:

```
java ArraySorterBenchmark -warmup 5 -timed 10 -iterations 50 -lengths "100000,1000000,10000000" -types "Random Wide,Random Narrow,Nearly Sorted,Reverse Sorted,High Variance,Small Numbers,Large Numbers,Equal Distrib,Unequal Distrib" -algorithms "custom.sorts.CustomSort::sort,java.util.Arrays::parallelSort"
```

```
iteration 50/50

ArrayType           Length    custom.sorts.CustomSort::sort      java.util.Arrays::parallelSort     
------------------------------------------------------------------------------------------------------------------------------------------------------
Random Wide         100000     1.8520                              2.1215                              Winner: custom.sorts.CustomSort::sort, faster by 12.70%
Random Narrow       100000     1.7348                              2.0464                              Winner: custom.sorts.CustomSort::sort, faster by 15.23%
Nearly Sorted       100000     0.7336                              0.7564                              Winner: custom.sorts.CustomSort::sort, faster by 3.01%
Reverse Sorted      100000     0.1347                              0.1597                              Winner: custom.sorts.CustomSort::sort, faster by 15.61%
High Variance       100000     1.7004                              1.7897                              Winner: custom.sorts.CustomSort::sort, faster by 4.99%
Small Numbers       100000     1.7290                              1.7528                              Winner: custom.sorts.CustomSort::sort, faster by 1.36%
Large Numbers       100000     1.7407                              1.7290                              Winner: java.util.Arrays::parallelSort, faster by 0.67%
Equal Distrib       100000     1.7342                              1.8507                              Winner: custom.sorts.CustomSort::sort, faster by 6.29%
Unequal Distrib     100000     1.7108                              1.8785                              Winner: custom.sorts.CustomSort::sort, faster by 8.93%
Random Wide         1000000    18.3769                             19.9152                             Winner: custom.sorts.CustomSort::sort, faster by 7.72%
Random Narrow       1000000    21.1768                             19.6295                             Winner: java.util.Arrays::parallelSort, faster by 7.31%
Nearly Sorted       1000000    6.7111                              8.1643                              Winner: custom.sorts.CustomSort::sort, faster by 17.80%
Reverse Sorted      1000000    0.9076                              1.5214                              Winner: custom.sorts.CustomSort::sort, faster by 40.35%
High Variance       1000000    16.8551                             20.0510                             Winner: custom.sorts.CustomSort::sort, faster by 15.94%
Small Numbers       1000000    17.6550                             20.2249                             Winner: custom.sorts.CustomSort::sort, faster by 12.71%
Large Numbers       1000000    19.2746                             20.2435                             Winner: custom.sorts.CustomSort::sort, faster by 4.79%
Equal Distrib       1000000    18.3074                             19.2615                             Winner: custom.sorts.CustomSort::sort, faster by 4.95%
Unequal Distrib     1000000    18.4075                             19.1620                             Winner: custom.sorts.CustomSort::sort, faster by 3.94%
Random Wide         10000000   198.0576                            223.2552                            Winner: custom.sorts.CustomSort::sort, faster by 11.29%
Random Narrow       10000000   197.4693                            221.0947                            Winner: custom.sorts.CustomSort::sort, faster by 10.69%
Nearly Sorted       10000000   76.9559                             93.2924                             Winner: custom.sorts.CustomSort::sort, faster by 17.51%
Reverse Sorted      10000000   6.9273                              14.1224                             Winner: custom.sorts.CustomSort::sort, faster by 50.95%
High Variance       10000000   213.3195                            200.9294                            Winner: java.util.Arrays::parallelSort, faster by 5.81%
Small Numbers       10000000   213.3944                            209.5350                            Winner: java.util.Arrays::parallelSort, faster by 1.81%
Large Numbers       10000000   198.6508                            216.0209                            Winner: custom.sorts.CustomSort::sort, faster by 8.04%
Equal Distrib       10000000   217.1676                            216.3234                            Winner: java.util.Arrays::parallelSort, faster by 0.39%
Unequal Distrib     10000000   203.7543                            199.2648                            Winner: java.util.Arrays::parallelSort, faster by 2.20%



Displaying overall results...
Sorting Algorithm                  Average % Improvement    Wins           
---------------------------------------------------------------------------
custom.sorts.CustomSort::sort      12.00                    1144/1350 wins 
java.util.Arrays::parallelSort     3.47                     206/1350 wins  

Overall Winner: custom.sorts.CustomSort::sort       8.53% better than java.util.Arrays::parallelSort 
```

iteration 50/50 is only shown full output can be found in benchmark folder

The results showed that CustomSort outperformed Arrays.parallelSort by 8.53% overall and won in 1144 out of 1350 array types.

To further validate the results, we ran another benchmark focusing only on random wide and random narrow arrays:

```
java ArraySorterBenchmark -warmup 5 -timed 10 -iterations 50 -lengths "100000,1000000,10000000" -types "Random Wide,Random Narrow" -algorithms "custom.sorts.CustomSort::sort,java.util.Arrays::parallelSort"
```

```
iteration 50/50


ArrayType           Length    custom.sorts.CustomSort::sort      java.util.Arrays::parallelSort
------------------------------------------------------------------------------------------------------------------------------------------------------
Random Wide         100000     1.9790                              2.0901                              Winner: custom.sorts.CustomSort::sort, faster by 5.32%
Random Narrow       100000     1.7818                              1.8607                              Winner: custom.sorts.CustomSort::sort, faster by 4.24%
Random Wide         1000000    17.8090                             20.9042                             Winner: custom.sorts.CustomSort::sort, faster by 14.81%
Random Narrow       1000000    19.6908                             19.4240                             Winner: java.util.Arrays::parallelSort, faster by 1.35%
Random Wide         10000000   215.8020                            232.2722                            Winner: custom.sorts.CustomSort::sort, faster by 7.09%
Random Narrow       10000000   200.6642                            214.0631                            Winner: custom.sorts.CustomSort::sort, faster by 6.26%



Displaying overall results...

Sorting Algorithm                  Average % Improvement    Wins
---------------------------------------------------------------------------
custom.sorts.CustomSort::sort      6.63                     226/300 wins
java.util.Arrays::parallelSort     4.38                     74/300 wins


Overall Winner: custom.sorts.CustomSort::sort       2.25% better than java.util.Arrays::parallelSort
```

In both cases, CustomSort outperformed Arrays.parallelSort.


Here's a more concise way to present the instructions for running the benchmarks locally:



# Running the Benchmarks Locally

To run the sorting algorithm benchmarks on your local machine, follow these steps:

1. Clone the repository:
   ```
   git clone https://github.com/ISmillex/CustomSort.git
   ```

2. Navigate to the project directory:
   ```
   cd CustomSort
   ```

3. Place your custom sorting algorithm in the `customs/sorts` folder. Ensure that your class has a `public void sort()` method. Alternatively, you can use the provided `CustomSort.java`.

4. Compile your sorting algorithm:
   ```
   javac customs/sorts/YOUR_SORTING_ALGORITHM.java
   ```
   (or `javac customs/sorts/CustomSort.java` for the provided `CustomSort` class)

5. Compile the `ArraySorterBenchmark` class:
   ```
   javac ArraySorterBenchmark.java
   ```

6. Run the benchmark with the desired arguments:
   ```
   java ArraySorterBenchmark [arguments]
   ```

   For example, to run the benchmark with a warm-up of 5 iterations, 10 timed runs, 10 iterations, array lengths of 10, 100, 1000, 10000, 100000, 1000000, and 10000000, using random wide arrays and comparing `Arrays.sort` with `CustomSort.sort`, you would run:

   ```
   java ArraySorterBenchmark -warmup 5 -timed 10 -iterations 10 -lengths "10,100,1000,10000,100000,1000000,10000000" -types "Random Wide" -algorithms "java.util.Arrays::sort,custom.sorts.CustomSort::sort"
   ```

Make sure to replace `YOUR_SORTING_ALGORITHM.java` with the name of your sorting algorithm class file, and adjust the arguments as needed for your benchmarking requirements.