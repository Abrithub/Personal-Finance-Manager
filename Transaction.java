import java.io.Serializable;
import java.time.LocalDate;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocalDate date;
    private String description;
    private String category;
    private String subCategory;
    private double amount;
    private String type; // "INCOME" or "EXPENSE"
    private String notes;

    public Transaction(LocalDate date, String description, String category, 
                      String subCategory, double amount, String type, String notes) {
        this.date = date;
        this.description = description;
        this.category = category;
        this.subCategory = subCategory;
        this.amount = amount;
        this.type = type;
        this.notes = notes;
    }

    // Getters and setters
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getSubCategory() { return subCategory; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getNotes() { return notes; }
} 