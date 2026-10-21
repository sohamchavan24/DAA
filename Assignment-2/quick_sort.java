import java.util.*;
import java.io.*;

class Movie {
    String title;
    float rating;
    int release_year;
    int popularity;

    void display() {
        System.out.println(title + " | Rating: " + rating
                + " | Year: " + release_year
                + " | Popularity: " + popularity);
    }
}

public class QuickSortMovies {

    // QuickSort
    static void quickSort(ArrayList<Movie> movies, int low, int high, String sortBy) {
        if (low < high) {
            int pivotIndex = low;
            Movie pivot = movies.get(high);

            for (int i = low; i < high; i++) {
                if (compare(movies.get(i), pivot, sortBy)) {
                    Collections.swap(movies, i, pivotIndex);
                    pivotIndex++;
                }
            }

            Collections.swap(movies, pivotIndex, high);

            quickSort(movies, low, pivotIndex - 1, sortBy);
            quickSort(movies, pivotIndex + 1, high, sortBy);
        }
    }

    // Compare movies
    static boolean compare(Movie a, Movie b, String sortBy) {
        if (sortBy.equals("rating"))
            return a.rating < b.rating;
        else if (sortBy.equals("year"))
            return a.release_year < b.release_year;
        else
            return a.popularity < b.popularity;
    }

    // Load movies from CSV
    static ArrayList<Movie> loadMovies(String filename) throws Exception {
        ArrayList<Movie> movies = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filename));

        br.readLine(); // Skip header

        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");

            if (data.length >= 4 && !data[0].isEmpty()) {
                Movie m = new Movie();
                m.title = data[0];
                m.rating = Float.parseFloat(data[1]);
                m.release_year = Integer.parseInt(data[2]);
                m.popularity = Integer.parseInt(data[3]);
                movies.add(m);
            }
        }

        br.close();
        return movies;
    }

    public static void main(String[] args) throws Exception {
        ArrayList<Movie> movies = loadMovies("movies.csv");

        if (movies.isEmpty()) {
            System.out.println("No movies loaded.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        System.out.print("Sort movies by (rating/year/popularity): ");
        String sortBy = sc.next().toLowerCase();

        if (!sortBy.equals("rating") &&
            !sortBy.equals("year") &&
            !sortBy.equals("popularity")) {
            System.out.println("Invalid choice. Defaulting to rating.");
            sortBy = "rating";
        }

        long start = System.nanoTime();

        quickSort(movies, 0, movies.size() - 1, sortBy);

        long end = System.nanoTime();
        double time = (end - start) / 1e9;

        System.out.println("\nTop 10 movies sorted by " + sortBy + ":");

        int count = 0;
        for (int i = movies.size() - 1; i >= 0 && count < 10; i--) {
            movies.get(i).display();
            count++;
        }

        System.out.println("\nSorted " + movies.size()
                + " movies in " + time + " seconds.");

        sc.close();
    }
}
