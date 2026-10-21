import java.util.Arrays;
import java.util.Comparator;

class ReliefItem {
    String name;
    double weight;
    double value;
    double ratio;

    ReliefItem(String name, double weight, double value) {
        this.name = name;
        this.weight = weight;
        this.value = value;
        this.ratio = value / weight;
    }
}

public class FractionalKnapsackRelief {

    public static void fractionalKnapsack(ReliefItem[] items, double capacity) {
        Arrays.sort(items, Comparator.comparingDouble((ReliefItem i) -> i.ratio).reversed());

         double remainingCapacity = capacity;
         double totalValue = 0;

         System.out.println("Item selection (boat capacity = " + capacity + " kg):\n");

         for (ReliefItem item : items) {
             if (remainingCapacity <= 0) break;

            if (item.weight <= remainingCapacity) {
                remainingCapacity -= item.weight;
                totalValue += item.value;
                System.out.println("Taken FULLY: " + item.name + " | Weight:" + item.weight + " | Value:" + item.value);
            } else {
                double fraction = remainingCapacity / item.weight;
                totalValue += item.value * fraction;
                System.out.println("Taken PARTIALLY: " + item.name + " | Fraction:" + String.format("%.2f", fraction)
                        + " | Weight taken:" + String.format("%.2f", remainingCapacity) + " | Value gained:" + String.format("%.2f", item.value * fraction));
                remainingCapacity = 0;
            }
        }

        System.out.println("\nTotal utility value carried by the boat: " + String.format("%.2f", totalValue));
    }

    public static void main(String[] args) {
        ReliefItem[] items = {
            new ReliefItem("Medicine Kits", 20, 200),
            new ReliefItem("Drinking Water", 50, 160),
            new ReliefItem("Food Packets", 40, 120),
            new ReliefItem("Blankets", 30, 90),
            new ReliefItem("Tents", 60, 150)
        };

          double boatCapacity = 100;

          fractionalKnapsack(items, boatCapacity);
    }
}
