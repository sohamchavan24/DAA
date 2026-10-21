import java.util.*;

public class KnapsackRelief {

    static String[] names = {"Medical Kit", "Drinking Water", "Food Packets", "Baby Formula",
            "Blankets", "Tent", "Torch Set", "Power Tools"};
    static int[] weight = {8, 15, 12, 5, 10, 20, 3, 18};
    static int[] utility = {90, 70, 60, 45, 40, 50, 20, 30};
    static boolean[] critical = {true, true, true, true, false, false, false, false};

    // ---------- 0/1 Knapsack using Dynamic Programming ----------
    static int dpKnapsack(int[] w, int[] v, int W, List<Integer> chosen) {
        int n = w.length;
        int[][] dp = new int[n + 1][W + 1];
        for (int i = 1; i <= n; i++) {
            for (int c = 1; c <= W; c++) {
                if (w[i - 1] <= c)
                    dp[i][c] = Math.max(v[i - 1] + dp[i - 1][c - w[i - 1]], dp[i - 1][c]);
                else
                    dp[i][c] = dp[i - 1][c];
            }
        }
        int c = W;
        for (int i = n; i >= 1; i--) {
            if (dp[i][c] != dp[i - 1][c]) {
                chosen.add(i - 1);
                c -= w[i - 1];
            }
        }
        Collections.reverse(chosen);
        return dp[n][W];
    }

    // ---------- Brute Force: try all 2^n subsets ----------
    static int bruteForce(int[] w, int[] v, int W, List<Integer> chosen) {
        int n = w.length, best = 0, bestMask = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            int tw = 0, tv = 0;
            for (int i = 0; i < n; i++)
                if ((mask & (1 << i)) != 0) { tw += w[i]; tv += v[i]; }
            if (tw <= W && tv > best) { best = tv; bestMask = mask; }
        }
        for (int i = 0; i < n; i++) if ((bestMask & (1 << i)) != 0) chosen.add(i);
        return best;
    }

    // ---------- Greedy: sort by value/weight ratio, take whatever fits ----------
    static int greedy(int[] w, int[] v, int W, List<Integer> chosen) {
        Integer[] idx = new Integer[w.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Double.compare((double) v[b] / w[b], (double) v[a] / w[a]));
        int used = 0, total = 0;
        for (int i : idx) {
            if (used + w[i] <= W) { used += w[i]; total += v[i]; chosen.add(i); }
        }
        Collections.sort(chosen);
        return total;
    }

    static String describe(List<Integer> chosen) {
        StringBuilder sb = new StringBuilder();
        int tw = 0;
        for (int i : chosen) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(names[i]);
            tw += weight[i];
        }
        return sb + "\n  Total weight used: " + tw + " kg";
    }

    public static void main(String[] args) {
        int W = 50;
        int n = names.length;

        System.out.println("Truck capacity: " + W + " kg\n");
        System.out.printf("%-16s %8s %8s %10s%n", "Item", "Weight", "Utility", "Critical");
        for (int i = 0; i < n; i++)
            System.out.printf("%-16s %8d %8d %10s%n", names[i], weight[i], utility[i], critical[i] ? "Yes" : "No");

        System.out.println("\n=== Comparison of approaches (base utilities) ===");
        List<Integer> c1 = new ArrayList<>(), c2 = new ArrayList<>(), c3 = new ArrayList<>();
        int dp = dpKnapsack(weight, utility, W, c1);
        int bf = bruteForce(weight, utility, W, c2);
        int gr = greedy(weight, utility, W, c3);
        System.out.println("Dynamic Programming : utility = " + dp + "\n  Items: " + describe(c1));
        System.out.println("Brute Force         : utility = " + bf + "\n  Items: " + describe(c2));
        System.out.println("Greedy (ratio)      : utility = " + gr + "\n  Items: " + describe(c3));

        System.out.println("\nWork done for N = " + n + ", W = " + W + ":");
        System.out.println("  Brute Force subsets checked : " + (1L << n) + "  (O(2^N))");
        System.out.println("  DP table cells computed     : " + (n * (long) W) + "  (O(N*W))");
        int bigN = 30, bigW = 1000;
        System.out.println("For N = " + bigN + ", W = " + bigW + ":");
        System.out.println("  Brute Force subsets : " + (1L << bigN));
        System.out.println("  DP table cells      : " + (bigN * (long) bigW));

        System.out.println("\n=== Priority for perishable / life-saving items (utility x 1.5) ===");
        int[] boosted = new int[n];
        for (int i = 0; i < n; i++) boosted[i] = critical[i] ? utility[i] + utility[i] / 2 : utility[i];
        List<Integer> c4 = new ArrayList<>();
        int pr = dpKnapsack(weight, boosted, W, c4);
        System.out.println("DP with priority    : utility = " + pr + "\n  Items: " + describe(c4));

        System.out.println("\n=== Extension: multiple trucks (each " + W + " kg, loaded one after another) ===");
        boolean[] taken = new boolean[n];
        for (int truck = 1; truck <= 2; truck++) {
            List<Integer> avail = new ArrayList<>();
            for (int i = 0; i < n; i++) if (!taken[i]) avail.add(i);
            if (avail.isEmpty()) break;
            int[] w2 = new int[avail.size()], v2 = new int[avail.size()];
            for (int k = 0; k < avail.size(); k++) {
                w2[k] = weight[avail.get(k)];
                v2[k] = boosted[avail.get(k)];
            }
            List<Integer> local = new ArrayList<>();
            int val = dpKnapsack(w2, v2, W, local);
            List<Integer> real = new ArrayList<>();
            for (int k : local) { real.add(avail.get(k)); taken[avail.get(k)] = true; }
            System.out.println("Truck " + truck + ": utility = " + val + "\n  Items: " + describe(real));
        }
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < n; i++) if (!taken[i]) left.append(left.length() > 0 ? ", " : "").append(names[i]);
        System.out.println("Left behind: " + (left.length() == 0 ? "None" : left));
    }
}
