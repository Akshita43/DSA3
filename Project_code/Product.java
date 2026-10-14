public class Product {
    public final String name;
    public final String category;
    public final double price;
    public final String description;

    public Product(String name, String category, double price, String description) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
    }

    public String toDetailString() {
        return "  Name        : " + capitalize(name) + "\n" +
               "  Category    : " + category + "\n" +
               "  Price       : Rs. " + String.format("%.2f", price) + "\n" +
               "  Description : " + description;
    }

    @Override
    public String toString() {
        return String.format("%-15s | %-14s | Rs.%.2f", capitalize(name), category, price);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}