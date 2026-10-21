import java.util.*;

public class TSPBranchBound {
    static final int INF = Integer.MAX_VALUE / 4;
    static int N;
    static long nodesGenerated, nodesPruned;

    static class Node implements Comparable<Node> {
        int[][] matrix;
        int lb;
        List<Integer> path;
        int level;
        int city;

        Node(int[][] matrix, int lb, List<Integer> path, int level, int city) {
            this.matrix = matrix;
            this.lb = lb;
            this.path = path;
            this.level = level;
            this.city = city;
        }

        public int compareTo(Node o) {
            return Integer.compare(this.lb, o.lb);
        }
    }

    static int[][] copy(int[][] m) {
        int[][] c = new int[m.length][];
        for (int i = 0; i < m.length; i++) c[i] = m[i].clone();
        return c;
    }

    // Row/column reduction. Modifies m and returns the total amount subtracted.
    static int reduce(int[][] m) {
        int total = 0;
        for (int i = 0; i < N; i++) {
            int min = INF;
            for (int j = 0; j < N; j++) min = Math.min(min, m[i][j]);
            if (min != INF && min > 0) {
                total += min;
                for (int j = 0; j < N; j++) if (m[i][j] != INF) m[i][j] -= min;
            }
        }
        for (int j = 0; j < N; j++) {
            int min = INF;
            for (int i = 0; i < N; i++) min = Math.min(min, m[i][j]);
            if (min != INF && min > 0) {
                total += min;
                for (int i = 0; i < N; i++) if (m[i][j] != INF) m[i][j] -= min;
            }
        }
        return total;
    }

    static int bestCost;
    static List<Integer> bestPath;

    static void solve(int[][] cost) {
        N = cost.length;
        nodesGenerated = 0;
        nodesPruned = 0;
        bestCost = INF;
        bestPath = null;

        int[][] root = copy(cost);
        for (int i = 0; i < N; i++) root[i][i] = INF;
        int lb0 = reduce(root);

        PriorityQueue<Node> pq = new PriorityQueue<>();
        List<Integer> p0 = new ArrayList<>();
        p0.add(0);
        pq.add(new Node(root, lb0, p0, 0, 0));
        nodesGenerated++;

        while (!pq.isEmpty()) {
            Node node = pq.poll();
            if (node.lb >= bestCost) { nodesPruned++; continue; }

            if (node.level == N - 1) {          // all cities visited, lb is the exact tour cost
                bestCost = node.lb;
                bestPath = new ArrayList<>(node.path);
                bestPath.add(0);
                continue;
            }

            int u = node.city;
            for (int v = 0; v < N; v++) {
                if (node.path.contains(v)) continue;
                int[][] m = copy(node.matrix);
                int edge = m[u][v];
                for (int k = 0; k < N; k++) { m[u][k] = INF; m[k][v] = INF; }
                if (node.level + 1 < N - 1) m[v][0] = INF;    // block premature return to start
                int red = reduce(m);
                int childLb = node.lb + edge + red;
                nodesGenerated++;
                if (childLb < bestCost) {
                    List<Integer> np = new ArrayList<>(node.path);
                    np.add(v);
                    pq.add(new Node(m, childLb, np, node.level + 1, v));
                } else {
                    nodesPruned++;
                }
            }
        }
    }

    // Brute force for verification: all (N-1)! permutations
    static int bruteBest;
    static void permute(int[][] cost, int[] perm, int k, int cur) {
        if (cur >= bruteBest) return;
        if (k == N) {
            bruteBest = Math.min(bruteBest, cur + cost[perm[N - 1]][0]);
            return;
        }
        for (int i = k; i < N; i++) {
            int t = perm[k]; perm[k] = perm[i]; perm[i] = t;
            permute(cost, perm, k + 1, cur + cost[perm[k - 1]][perm[k]]);
            t = perm[k]; perm[k] = perm[i]; perm[i] = t;
        }
    }

    static int bruteForce(int[][] cost) {
        int n = cost.length;
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        bruteBest = INF;
        permute(cost, perm, 1, 0);
        return bruteBest;
    }

    static long factorial(int n) {
        long f = 1;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }

    static String pathString(List<Integer> p) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(p.get(i));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        int I = INF;
        int[][] cost = {
                {I, 20, 30, 10, 11},
                {15, I, 16, 4, 2},
                {3, 5, I, 2, 4},
                {19, 6, 18, I, 3},
                {16, 4, 7, 16, I}
        };

        System.out.println("=== Test 1: 5 cities (fuel-cost matrix, City 0 = depot) ===");
        System.out.println("Cost matrix:");
        for (int[] row : cost) {
            StringBuilder sb = new StringBuilder();
            for (int x : row) sb.append(String.format("%5s", x >= INF ? "INF" : String.valueOf(x)));
            System.out.println(sb);
        }
        solve(cost);
        System.out.println("\nLC Branch & Bound result:");
        System.out.println("  Minimum tour cost : " + bestCost);
        System.out.println("  Optimal route     : " + pathString(bestPath));
        System.out.println("  Nodes generated   : " + nodesGenerated + ", pruned : " + nodesPruned);
        System.out.println("  Brute-force check : " + bruteForce(cost) + "  (permutations = " + factorial(N - 1) + ")");

        System.out.println("\n=== Test 2: 10 random cities (seed = 1) ===");
        int n = 10;
        Random rnd = new Random(1);
        int[][] big = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) big[i][j] = (i == j) ? INF : 10 + rnd.nextInt(90);
        solve(big);
        System.out.println("LC Branch & Bound result:");
        System.out.println("  Minimum tour cost : " + bestCost);
        System.out.println("  Optimal route     : " + pathString(bestPath));
        System.out.println("  Nodes generated   : " + nodesGenerated + ", pruned : " + nodesPruned);
        System.out.println("  Brute-force nodes would be about (N-1)! = " + factorial(n - 1) + " complete tours");
        System.out.println("  Brute-force check : " + bruteForce(big));
    }
}
