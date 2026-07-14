import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;

public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;
    private String category;
    private double budgetAmount;
    private YearMonth yearMonth;
    private double spentAmount;

    public Budget(String category, double budgetAmount, YearMonth yearMonth) {
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.yearMonth = yearMonth;
        this.spentAmount = 0.0;
    }

    // Getters and setters
    public String getCategory() { return category; }
    public double getBudgetAmount() { return budgetAmount; }
    public YearMonth getYearMonth() { return yearMonth; }
    public double getSpentAmount() { return spentAmount; }
    public void setSpentAmount(double amount) { this.spentAmount = amount; }
    
    public double getRemainingAmount() {
        return budgetAmount - spentAmount;
    }
    
    public double getProgressPercentage() {
        return (spentAmount / budgetAmount) * 100;
    }
} 