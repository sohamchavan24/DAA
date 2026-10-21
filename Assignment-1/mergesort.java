import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class Order {
    int orderId;
    long timestamp;       // epoch seconds, derived from Order Time
    String orderTime;     // original text from CSV

    Order(int orderId, long timestamp, String orderTime) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.orderTime = orderTime;
    }

    public String toString() {
        return "OrderID:" + orderId + " OrderTime:" + orderTime + " Timestamp:" + timestamp;
    }
}

public class MergeSortOrders {

    public static void mergeSort(Order[] orders, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;

            mergeSort(orders, left, mid);
            mergeSort(orders, mid + 1, right);

            merge(orders, left, mid, right);
        }
    }

    public static void merge(Order[] orders, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Order[] leftArr = new Order[n1];
        Order[] rightArr = new Order[n2];

        for (int i = 0; i < n1; i++) {
            leftArr[i] = orders[left + i];
        }

        for (int j = 0; j < n2; j++) {
            rightArr[j] = orders[mid + 1 + j];
        }

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            if (leftArr[i].timestamp <= rightArr[j].timestamp) {
                orders[k] = leftArr[i];
                i++;
            } else {
                orders[k] = rightArr[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            orders[k] = leftArr[i];
            i++;
            k++;
        }

        while (j < n2) {
            orders[k] = rightArr[j];
            j++;
            k++;
        }
    }

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "restaurant_orders.csv";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        List<Order> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                int id = Integer.parseInt(p[0].trim());
                String time = p[p.length - 1].trim();
                long ts = LocalDateTime.parse(time, fmt).toEpochSecond(ZoneOffset.UTC);
                list.add(new Order(id, ts, time));
            }
        }

        Order[] orders = list.toArray(new Order[0]);
        System.out.println("Total orders read: " + orders.length);

        System.out.println("\nUnsorted Orders (first 10):");
        for (int i = 0; i < 10; i++) {
            System.out.println(orders[i]);
        }

        long startTime = System.nanoTime();
        mergeSort(orders, 0, orders.length - 1);
        long endTime = System.nanoTime();

        System.out.println("\nAll " + orders.length + " Sorted Orders (by Order Time, earliest first):");
        for (Order o : orders) {
            System.out.println(o);
        }

        System.out.println("\nSorted " + orders.length + " orders successfully.");
        System.out.println("Time taken: " + (endTime - startTime) / 1000000.0 + " ms");

        boolean isSorted = true;
        for (int i = 0; i < orders.length - 1; i++) {
            if (orders[i].timestamp > orders[i + 1].timestamp) {
                isSorted = false;
                break;
            }
        }
        System.out.println("Array correctly sorted: " + isSorted);
    }
}
