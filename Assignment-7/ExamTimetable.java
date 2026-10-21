import java.util.*;

public class ExamTimetable {

    static List<Set<Integer>> adj;
    static int[] size;
    static int V;

    static void buildGraph(int numCourses, List<int[]> enrollments) {
        V = numCourses;
        adj = new ArrayList<>();
        size = new int[V];
        for (int i = 0; i < V; i++) adj.add(new HashSet<>());
        for (int[] courses : enrollments) {
            for (int c : courses) size[c]++;
            for (int a = 0; a < courses.length; a++)
                for (int b = a + 1; b < courses.length; b++) {
                    adj.get(courses[a]).add(courses[b]);
                    adj.get(courses[b]).add(courses[a]);
                }
        }
    }

    static int smallestFreeColour(int u, int[] colour) {
        boolean[] used = new boolean[V + 1];
        for (int v : adj.get(u)) if (colour[v] != -1) used[colour[v]] = true;
        int c = 0;
        while (used[c]) c++;
        return c;
    }

    // 1. Greedy colouring in the given (natural) order
    static int[] greedy() {
        int[] colour = new int[V];
        Arrays.fill(colour, -1);
        for (int u = 0; u < V; u++) colour[u] = smallestFreeColour(u, colour);
        return colour;
    }

    // 2. Welsh-Powell: sort by degree (descending), then greedy
    static int[] welshPowell() {
        Integer[] order = new Integer[V];
        for (int i = 0; i < V; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> adj.get(b).size() - adj.get(a).size());
        int[] colour = new int[V];
        Arrays.fill(colour, -1);
        for (int u : order) colour[u] = smallestFreeColour(u, colour);
        return colour;
    }

    // 3. DSATUR: pick the vertex with the most distinct neighbour colours (ties -> higher degree)
    static int[] dsatur() {
        int[] colour = new int[V];
        Arrays.fill(colour, -1);
        for (int step = 0; step < V; step++) {
            int best = -1, bestSat = -1, bestDeg = -1;
            for (int u = 0; u < V; u++) {
                if (colour[u] != -1) continue;
                Set<Integer> seen = new HashSet<>();
                for (int v : adj.get(u)) if (colour[v] != -1) seen.add(colour[v]);
                int sat = seen.size(), deg = adj.get(u).size();
                if (sat > bestSat || (sat == bestSat && deg > bestDeg)) {
                    best = u; bestSat = sat; bestDeg = deg;
                }
            }
            colour[best] = smallestFreeColour(best, colour);
        }
        return colour;
    }

    static int countColours(int[] colour) {
        int m = 0;
        for (int c : colour) m = Math.max(m, c + 1);
        return m;
    }

    static boolean isValid(int[] colour) {
        for (int u = 0; u < V; u++)
            for (int v : adj.get(u)) if (colour[u] == colour[v]) return false;
        return true;
    }

    static void allocateRooms(int[] colour, String[] roomNames, int[] roomCap) {
        int slots = countColours(colour);
        System.out.printf("%-8s %-10s %-10s %-8s%n", "Course", "Time Slot", "Students", "Room");
        for (int t = 0; t < slots; t++) {
            List<Integer> exams = new ArrayList<>();
            for (int c = 0; c < V; c++) if (colour[c] == t) exams.add(c);
            exams.sort((a, b) -> size[b] - size[a]);
            boolean[] roomBusy = new boolean[roomNames.length];
            for (int e : exams) {
                int pick = -1;
                for (int r = 0; r < roomNames.length; r++) {
                    if (!roomBusy[r] && roomCap[r] >= size[e] && (pick == -1 || roomCap[r] < roomCap[pick])) pick = r;
                }
                if (pick != -1) roomBusy[pick] = true;
                System.out.printf("%-8s %-10s %-10d %-8s%n", "C" + (e + 1), "Slot " + (t + 1), size[e],
                        pick == -1 ? "NO ROOM" : roomNames[pick]);
            }
        }
    }

    static void runAll(String title, int numCourses, List<int[]> enrollments, String[] rn, int[] rc) {
        System.out.println("=== " + title + " ===");
        buildGraph(numCourses, enrollments);
        int edges = 0;
        for (Set<Integer> s : adj) edges += s.size();
        System.out.println("Courses (vertices): " + V + ", Conflicts (edges): " + edges / 2);
        for (int u = 0; u < V; u++) {
            StringBuilder sb = new StringBuilder();
            for (int v : new TreeSet<>(adj.get(u))) sb.append(sb.length() > 0 ? ", " : "").append("C").append(v + 1);
            System.out.println("  C" + (u + 1) + " (" + size[u] + " students) conflicts with: " + (sb.length() == 0 ? "none" : sb));
        }
        int[] g = greedy(), w = welshPowell(), d = dsatur();
        System.out.println("\nAlgorithm comparison (number of exam slots):");
        System.out.println("  Greedy        : " + countColours(g) + "  valid = " + isValid(g));
        System.out.println("  Welsh-Powell  : " + countColours(w) + "  valid = " + isValid(w));
        System.out.println("  DSATUR        : " + countColours(d) + "  valid = " + isValid(d));
        System.out.println("\nTimetable using DSATUR with room allocation:");
        allocateRooms(d, rn, rc);
        System.out.println();
    }

    public static void main(String[] args) {
        // Test 1: example from the problem statement (C1..C4, S1..S4)
        List<int[]> small = new ArrayList<>();
        small.add(new int[]{0, 1});   // S1: C1, C2
        small.add(new int[]{1, 2});   // S2: C2, C3
        small.add(new int[]{0, 2});   // S3: C1, C3
        small.add(new int[]{3});      // S4: C4
        runAll("Test 1: Example input (4 courses, 4 students)", 4, small,
                new String[]{"R1", "R2", "R3"}, new int[]{100, 80, 50});

        // Test 2: 15 courses, 40 students, each enrolled in 2 random courses (fixed seed)
        Random rnd = new Random(7);
        List<int[]> big = new ArrayList<>();
        for (int s = 0; s < 40; s++) {
            Set<Integer> pick = new LinkedHashSet<>();
            while (pick.size() < 2) pick.add(rnd.nextInt(15));
            int[] arr = new int[2];
            int k = 0;
            for (int c : pick) arr[k++] = c;
            big.add(arr);
        }
        runAll("Test 2: 15 courses, 40 students, 2 courses each", 15, big,
                new String[]{"R1", "R2", "R3", "R4", "R5", "R6"}, new int[]{12, 10, 8, 7, 6, 5});
    }
}
