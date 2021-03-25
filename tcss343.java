
import java.util.*;
import java.util.stream.Collectors;

/**
 * TCSS343 HW4 Solving the Subset Sum problem using 3 algorithms: brute force,
 * dynamic programming, and clever algorithm
 *
 * @author Khaled Al Ashor 
 * @author Ismael Jones 
 *
 */
public class tcss343 {
    static Set<Integer> foundSubset;
    static boolean cleverEqualityFound = false;
    static final boolean DEBUG = false;

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        for (int n = 5; n < 350; n++)
            driver(n, 1000, true);

        for (int n = 5; n < 400; n++)
            driver(n, 1000000, true);

        for (int n = 5; n < 500; n++)
            driver(n, 1000, false);

        for (int n = 5; n < 55; n++)
            driver(n, 1000000, false);

    }

    /**
     * Testing driver method that tests all 3 algorithms, calculates the time each
     * algorithm takes to solve the problem, and prints the results
     *
     * @param n number of set elements
     * @param r maximum value of generated elements (sampled from 1 to r)
     * @param v true to guarantee a subset is found, false to guarantee otherwise
     */
    public static void driver(int n, int r, boolean v) {
        boolean result;

        // skip infeasible combinations of n, r, and v, for some algorithms
        boolean skipClever = false, skipBruteForce = true, skipDP = true;

        if (v) {
            if (r == 1000) {
                if (n > 170)
                    skipClever = true;
            } else if (r == 1000000) {
                if (n > 42)
                    skipClever = true;
                if (n > 62)
                    skipDP = true;
            }
        } else {
            if (r == 1000) {
                if (n > 29)
                    skipClever = true;
                if (n > 32)
                    skipBruteForce = true;
            } else if (r == 1000000) {
                if (n > 29)
                    skipClever = true;
                if (n > 32)
                    skipBruteForce = true;
            }
        }

        // generate a random set of n values and calculate t based on v
        Random rand = new Random();
        Set<Integer> s = new HashSet<Integer>();
        long start, end; // Values used to store the start and end time for the algorithms
        int t = 0;
        int addedT = 0; // used to limit the number of elements in the random selection of t to 5

        // add random values to set sampled from 1 to r
        // depending on v, t is calculated as follows:
        // v = true - the sum of a random subset of 5 elements
        // v = false - the sum of all elements + a random number from 0 to 100
        while (s.size() < n) {
            int value = rand.nextInt(r + 1) + 1;
            s.add(value);

            if (v) {
                // randomly select elements and add their weight to t
                if (s.size() % (rand.nextInt(5) + 1) == 0 && addedT < 6) {
                    t += value;
                    addedT++;

                    if (DEBUG)
                        System.out.println("Added value " + value);
                }
            } else {
                addedT++;
                t += value;
            }

            // to ensure t is always calculated if previous random selection never occurs
            if (s.size() == n && addedT == 0)
                t += value;
        }

        if (!v) {
            t += rand.nextInt(100);
        }

        // print testing values n, r, v, the generated set S, and the calculated t
        System.out.print("n = " + n + "   r = " + r + "   v = " + v);
        System.out.println("\nS = " + s);
        System.out.println("t = " + t);

        // Clever algorithm
        if (!skipClever) {
            System.out.println("----------- Clever -----------");
            start = System.currentTimeMillis();
            result = cleverSolution(s.toArray(new Integer[s.size()]), t);
            end = System.currentTimeMillis();

            System.out.println("Time: " + (end - start) + " ms");
            System.out.println("Space: " + s.size() * Math.pow(2.0, s.size() / 2) + " bits or elements");

            if (result) {
                System.out.println("Subset: " + foundSubset);
            } else {
                System.out.println("No subsets found summing to " + t);
            }
        }

        // Brute force algorithm
        if (!skipBruteForce) {
            System.out.println("----------- Brute Force -----------");
            start = System.currentTimeMillis();
            result = bruteForceSubsetSum(s.toArray(new Integer[s.size()]), n, t);
            end = System.currentTimeMillis();

            System.out.println("Time: " + (end - start) + " ms");
            System.out.println("Space: " + s.size() + " bits or 1 elements");

            if (result) {
                System.out.println("Subset: " + foundSubset);
            } else {
                System.out.println("No subsets found summing to " + t);
            }
        }

        // Dynamic programming algorithm
        if (!skipDP) {
            System.out.println("----------- Dynamic Programming -----------");
            start = System.currentTimeMillis();
            result = dynamicSubsetSum(s.toArray(new Integer[s.size()]), n, t);
            end = System.currentTimeMillis();

            System.out.println("Time: " + (end - start) + " ms");
            System.out.println("Space: " + Math.pow(s.size(), 2.0) * Math.pow(2.0, s.size()) + " bits or "
                    + s.size() * Math.pow(2.0, s.size()) + " elements");

            if (result) {
                System.out.println("Subset: " + foundSubset);
            } else {
                System.out.println("No subsets found summing to " + t);
            }
        }

        System.out.println();

    }

    /**
     * The clever solution. Splits array into halves and finds all subsets of sub-arrays with a sum < target.
     * When not found, sums up all the subsets together in an attempt to make the sum.
     *
     * @param nums an array of integers, the 'set'
     * @param target, the target sum
     * */
    public static boolean cleverSolution(Integer[] nums, int target){
        //1. Split the indicies
        Set<HashSet<Integer>> leftSubsets = new HashSet<HashSet<Integer>>();
        Set<HashSet<Integer>> rightSubsets = new HashSet<HashSet<Integer>>();

        //Make left subsets, exit if we have a match with the target
        for(int i = 0; i < nums.length/2; i++){
            Set<HashSet<Integer>> temp = new HashSet<HashSet<Integer>>();
            for(Set<Integer> a : leftSubsets){
                temp.add(new HashSet<Integer>(a));
            }
            for(HashSet<Integer> a : temp) {
                a.add(nums[i]);
               		 //Check if each subset equals the sum, return if true, skip to next number if the sum is too big break
                int sum = sumHashSet(a);
                if(sum == target){
                    foundSubset =  a;
                    return true;
                }
                else if(sum > target) {
                    //Remove the last element because it is over target
                    a.remove(a.size()-1);
                    break;
                }
            }
            HashSet<Integer> single = new HashSet<Integer>();
            single.add(nums[i]);
            temp.add(single);
            //check if the single equals the sum, return if true
            if(sumHashSet(single)== target){
                foundSubset =  single;
                return true;
            }
            leftSubsets.addAll(temp);

        }

        //Make right subsets, exit if we have a match with the target
        int N  = 0;
        if(nums.length % 2 == 0){
            N = nums.length/2;
        }
        else{
            N = nums.length/2 + 1;
        }
        for(int i = 0; i < N; i++){
            Set<HashSet<Integer>> temp = new HashSet<HashSet<Integer>>();
            for(Set<Integer> a : rightSubsets){
                temp.add(new HashSet<Integer>(a));
            }
            for(HashSet<Integer> a : temp) {
                a.add(nums[i + nums.length/2]);
                //Check if each subset equals the sum, return if true
                int sum =sumHashSet(a);
                if(sum == target){
                    foundSubset =  a;
                    return true;
                }
                else if(sum > target){
                    break;
                }
            }
            HashSet<Integer> single = new HashSet<Integer>();
            single.add(nums[i+nums.length/2]);
            temp.add(single);
            //check if the single equals the sum, return if true
            if(sumHashSet(single)== target){
                foundSubset = single;
                return true;
            }
            rightSubsets.addAll(temp);

        }



        //Sort the right subset list


        //Compare the sum of the pairs, return true on success
        for(HashSet<Integer> list : leftSubsets){
            for(HashSet<Integer> list2: rightSubsets){
                int value = pairSum(list,list2);
                if(value == target){
                    HashSet<Integer> solution = new HashSet<Integer>();
                    solution.addAll(list);
                    solution.addAll(list2);
                    foundSubset = solution;
                    return true;
                }
            }
        }


        return false;
    }
    /**
     * Sums two HashSets together
     *
     * @param list1 a list of integers
     * @param list2 a list of integers
     * */
    public static int pairSum(HashSet<Integer> list1, HashSet<Integer> list2){
        return sumHashSet(list1) + sumHashSet(list2);
    }
    /**
     * Sums an HashSet
     *
     * @param list, a list to be summed
     * */
    public static int sumHashSet(HashSet<Integer> list){
        int count = 0;
        for(int num : list){
            count += num;
        }
        return count;
    }

    /**
     * Solves the subset sum problem using brute-force algorithm
     *
     * @param s Set
     * @param n Set size
     * @param t sum to find
     * @return true if a subset that sums to t is found, false otherwise
     */
    public static boolean bruteForceSubsetSum(Integer[] s, int n, int t) {
        foundSubset = new HashSet<Integer>();
        boolean elementFound;

        if (n == 0) {
            return t == 0;
        }

        if (bruteForceSubsetSum(s, n - 1, t)) {
            return true;
        }

        if (t - s[n - 1] >= 0) {
            elementFound = bruteForceSubsetSum(s, n - 1, t - s[n - 1]);

            // add elements to foundSubset to be able to show the subset which sums up to t
            if (elementFound)
                foundSubset.add(s[n - 1]);

            return elementFound;
        }

        return false;
    }

    /**
     * Solves the subset sum problem using dynamic programming.
     *
     * @param s Set
     * @param n Set size
     * @param t sum to find
     * @return true if a subset that sums to t is found, false otherwise
     */
    public static boolean dynamicSubsetSum(Integer[] s, int n, int t) {
        boolean[][] table = new boolean[n + 1][t + 1];
        for (int i = 0; i <= n; i++) {
            table[i][0] = true;
        }
        for (int i = 1; i <= t; i++) {
            table[0][i] = false;
        }

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= t; j++) {
                table[i][j] = table[i - 1][j];
                if (j >= s[i - 1]) {
                    table[i][j] = table[i][j] || table[i - 1][j - s[i - 1]];
                }
            }
        }

        // find the subset that sums up to t (if one exists)
        if (table[n][t]) {
            foundSubset = new HashSet<Integer>();
            int currentI = n, currentJ = t;

            while (currentI > 0 && currentJ > 0) {
                if (table[currentI - 1][currentJ])
                    currentI--;
                else {
                    foundSubset.add(s[currentI - 1]);
                    currentJ -= s[currentI - 1];
                    currentI--;

                }
            }
        } else {
            foundSubset = new HashSet<Integer>();
        }

        return table[n][t];
    }

    /**
     * Prints the content of an array in the set format {a,b,c}
     *
     * @param a array to print
     */
    public static void printArray(int[] a) {
        System.out.print("{");
        // print
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1)
                System.out.print(',');
        }
        System.out.print("}\n");
    }
}
