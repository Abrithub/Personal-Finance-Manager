import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.chart.*;
import java.time.LocalDate;
import java.time.YearMonth;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class app extends Application {
    private Stage primaryStage;
    private VBox loginLayout;
    private VBox registerLayout;
    private VBox mainLayout;
    private TabPane tabPane;
    private TextField amountField;
    private TextField descriptionField;
    private ComboBox<String> categoryComboBox;
    private DatePicker datePicker;
    private TableView<Transaction> transactionTable;
    private Label balanceLabel;
    private double currentBalance = 0.0;
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private ObservableList<Budget> budgets = FXCollections.observableArrayList();
    private Map<String, TextField> budgetFields = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showLoginScreen();
    }

    private void showLoginScreen() {
        primaryStage.setTitle("Personal Finance Manager - Login");

        loginLayout = new VBox(20);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setStyle("-fx-background-color: #f0f0f0;");
        loginLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Login");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameField = createStyledTextField("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 8px; -fx-background-radius: 5;");

        Button loginButton = createStyledButton("Login");
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setOnAction(e -> showRegisterScreen());

        loginLayout.getChildren().addAll(
                titleLabel,
                usernameField,
                passwordField,
                loginButton,
                registerLink);

        Scene scene = new Scene(loginLayout, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showRegisterScreen() {
        registerLayout = new VBox(20);
        registerLayout.setPadding(new Insets(20));
        registerLayout.setStyle("-fx-background-color: #f0f0f0;");
        registerLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Register");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameField = createStyledTextField("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 8px; -fx-background-radius: 5;");
        TextField emailField = createStyledTextField("Email");
        TextField fullNameField = createStyledTextField("Full Name");

        Button registerButton = createStyledButton("Register");
        registerButton.setOnAction(e -> handleRegister(
                usernameField.getText(),
                passwordField.getText(),
                emailField.getText(),
                fullNameField.getText()));

        Hyperlink loginLink = new Hyperlink("Already have an account? Login here");
        loginLink.setOnAction(e -> showLoginScreen());

        registerLayout.getChildren().addAll(
                titleLabel,
                usernameField,
                passwordField,
                emailField,
                fullNameField,
                registerButton,
                loginLink);

        Scene scene = new Scene(registerLayout, 400, 500);
        primaryStage.setScene(scene);
    }

    private void handleLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Please enter both username and password");
            return;
        }

        if (LoginManager.login(username, password)) {
            showMainApplication();
        } else {
            showAlert("Login Failed", "Invalid username or password");
        }
    }

    private void handleRegister(String username, String password, String email, String fullName) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            showAlert("Registration Error", "Please fill in all fields");
            return;
        }

        if (LoginManager.register(username, password, email, fullName)) {
            showAlert("Success", "Registration successful! Please login.");
            showLoginScreen();
        } else {
            showAlert("Registration Error", "Username already exists");
        }
    }

    private void showMainApplication() {
        primaryStage.setTitle("Personal Finance Manager");

        mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f0f0f0;");
        mainLayout.setAlignment(Pos.CENTER);

        // Top bar with user info and logout
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        Label welcomeLabel = new Label("Welcome, " + LoginManager.getCurrentUser().getFullName());
        welcomeLabel.setStyle("-fx-font-size: 18px;");
        Button logoutButton = createStyledButton("Logout");
        logoutButton.setOnAction(e -> handleLogout());
        topBar.getChildren().addAll(welcomeLabel, logoutButton);

        // Create TabPane for different sections
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs
        Tab dashboardTab = createDashboardTab();
        Tab expensesTab = createExpensesTab();
        Tab incomeTab = createIncomeTab();
        Tab budgetTab = createBudgetTab();
        Tab reportsTab = createReportsTab();

        tabPane.getTabs().addAll(dashboardTab, expensesTab, incomeTab, budgetTab, reportsTab);

        mainLayout.getChildren().addAll(topBar, tabPane);

        Scene scene = new Scene(mainLayout, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab createDashboardTab() {
        Tab tab = new Tab("Dashboard");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Balance section
        balanceLabel = new Label("Current Balance: $0.00");
        balanceLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Calculate totals
        double totalIncome = transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Summary cards
        HBox summaryCards = new HBox(20);
        summaryCards.getChildren().addAll(
                createSummaryCard("Total Income", String.format("$%.2f", totalIncome),
                        "-fx-background-color: #4CAF50;"),
                createSummaryCard("Total Expenses", String.format("$%.2f", totalExpenses),
                        "-fx-background-color: #f44336;"),
                createSummaryCard("Monthly Budget", "$0.00", "-fx-background-color: #2196F3;"));

        // Recent transactions
        Label recentTransactionsLabel = new Label("Recent Transactions");
        recentTransactionsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        transactionTable = createTransactionTable();
        transactionTable.setPrefHeight(300);

        content.getChildren().addAll(balanceLabel, summaryCards, recentTransactionsLabel, transactionTable);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private Tab createExpensesTab() {
        Tab tab = new Tab("Expenses");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Add expense form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        amountField = createStyledTextField("Amount");
        descriptionField = createStyledTextField("Description");
        categoryComboBox = createExpenseCategoryComboBox();
        datePicker = new DatePicker(LocalDate.now());

        form.addRow(0, new Label("Amount:"), amountField);
        form.addRow(1, new Label("Description:"), descriptionField);
        form.addRow(2, new Label("Category:"), categoryComboBox);
        form.addRow(3, new Label("Date:"), datePicker);

        Button addButton = createStyledButton("Add Expense");
        addButton.setOnAction(e -> addTransaction("EXPENSE"));
        form.add(addButton, 1, 4);

        // Expense history
        Label historyLabel = new Label("Expense History");
        historyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Transaction> expenseTable = createTransactionTable();
        expenseTable.setPrefHeight(300);

        content.getChildren().addAll(
                new Label("Add New Expense"), form,
                historyLabel, expenseTable);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private Tab createIncomeTab() {
        Tab tab = new Tab("Income");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Add income form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        // Create new fields specifically for income
        TextField incomeAmount = createStyledTextField("Amount");
        TextField incomeDescription = createStyledTextField("Description");
        ComboBox<String> incomeCategory = createIncomeCategoryComboBox();
        DatePicker incomeDatePicker = new DatePicker(LocalDate.now());

        form.addRow(0, new Label("Amount:"), incomeAmount);
        form.addRow(1, new Label("Description:"), incomeDescription);
        form.addRow(2, new Label("Source:"), incomeCategory);
        form.addRow(3, new Label("Date:"), incomeDatePicker);

        Button addButton = createStyledButton("Add Income");
        addButton.setOnAction(e -> {
            // Store the current global field values
            TextField tempAmount = amountField;
            TextField tempDesc = descriptionField;
            ComboBox<String> tempCategory = categoryComboBox;
            DatePicker tempDate = datePicker;

            // Set the global fields to the income form values
            amountField = incomeAmount;
            descriptionField = incomeDescription;
            categoryComboBox = incomeCategory;
            datePicker = incomeDatePicker;

            // Add the transaction
            addTransaction("INCOME");

            // Restore the global field values
            amountField = tempAmount;
            descriptionField = tempDesc;
            categoryComboBox = tempCategory;
            datePicker = tempDate;
        });
        form.add(addButton, 1, 4);

        // Income history
        Label historyLabel = new Label("Income History");
        historyLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create a filtered table for income transactions
        TableView<Transaction> incomeTable = createTransactionTable();
        incomeTable.setPrefHeight(300);

        // Filter to show only income transactions
        incomeTable.setItems(transactions.filtered(transaction -> "INCOME".equals(transaction.getType())));

        content.getChildren().addAll(
                new Label("Add New Income"), form,
                historyLabel, incomeTable);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private Tab createBudgetTab() {
        Tab tab = new Tab("Budget");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Monthly budget setup
        GridPane budgetGrid = new GridPane();
        budgetGrid.setHgap(10);
        budgetGrid.setVgap(10);
        budgetGrid.setPadding(new Insets(20));
        budgetGrid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        // Headers
        budgetGrid.addRow(0,
                new Label("Category"),
                new Label("Budget Amount"),
                new Label("Progress"),
                new Label("Remaining"));

        // Add budget categories with input fields
        addBudgetCategory(budgetGrid, 1, "Food & Dining", 500.0);
        addBudgetCategory(budgetGrid, 2, "Transportation", 300.0);
        addBudgetCategory(budgetGrid, 3, "Housing", 1000.0);
        addBudgetCategory(budgetGrid, 4, "Utilities", 200.0);
        addBudgetCategory(budgetGrid, 5, "Entertainment", 150.0);

        Button saveBudgetButton = createStyledButton("Save Budget");
        saveBudgetButton.setOnAction(e -> saveBudgets());

        content.getChildren().addAll(
                new Label("Monthly Budget Planning"),
                budgetGrid,
                saveBudgetButton);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private void addBudgetCategory(GridPane grid, int row, String category, double defaultAmount) {
        Label categoryLabel = new Label(category);
        TextField amountField = createStyledTextField(String.valueOf(defaultAmount));
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        Label remainingLabel = new Label("$0.00");

        budgetFields.put(category, amountField);

        grid.addRow(row, categoryLabel, amountField, progressBar, remainingLabel);
    }

    private void saveBudgets() {
        budgets.clear();
        YearMonth currentMonth = YearMonth.now();

        budgetFields.forEach((category, field) -> {
            try {
                double amount = Double.parseDouble(field.getText());
                Budget budget = new Budget(category, amount, currentMonth);

                // Calculate spent amount from transactions
                double spent = transactions.stream()
                        .filter(t -> t.getCategory().equals(category) &&
                                t.getType().equals("EXPENSE") &&
                                YearMonth.from(t.getDate()).equals(currentMonth))
                        .mapToDouble(Transaction::getAmount)
                        .sum();

                budget.setSpentAmount(spent);
                budgets.add(budget);

                // Update progress bars and remaining amounts
                GridPane budgetGrid = (GridPane) ((VBox) ((ScrollPane) tabPane.getTabs().get(3).getContent())
                        .getContent()).getChildren().get(1);
                int row = budgetGrid.getRowIndex(field);
                ProgressBar progressBar = (ProgressBar) getNodeFromGridPane(budgetGrid, 2, row);
                Label remainingLabel = (Label) getNodeFromGridPane(budgetGrid, 3, row);

                progressBar.setProgress(spent / amount);
                remainingLabel.setText(String.format("$%.2f", budget.getRemainingAmount()));

            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid budget amount for " + category);
            }
        });

        showAlert("Success", "Budgets saved successfully!");
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private Tab createReportsTab() {
        Tab tab = new Tab("Reports");
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Report options
        ComboBox<String> reportType = new ComboBox<>();
        reportType.getItems().addAll(
                "Monthly Expense Summary",
                "Income vs Expenses",
                "Category-wise Expenses",
                "Savings Trend");
        reportType.setValue("Monthly Expense Summary");

        // Date range selector
        HBox dateRange = new HBox(10);
        DatePicker startDate = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker endDate = new DatePicker(LocalDate.now());
        dateRange.getChildren().addAll(
                new Label("From:"), startDate,
                new Label("To:"), endDate);

        Button generateReport = createStyledButton("Generate Report");
        VBox chartContainer = new VBox();

        generateReport.setOnAction(e -> {
            chartContainer.getChildren().clear();

            switch (reportType.getValue()) {
                case "Monthly Expense Summary":
                    chartContainer.getChildren().add(createMonthlyExpenseSummary());
                    break;
                case "Income vs Expenses":
                    chartContainer.getChildren()
                            .add(createIncomeVsExpensesChart(startDate.getValue(), endDate.getValue()));
                    break;
                case "Category-wise Expenses":
                    chartContainer.getChildren()
                            .add(createCategoryExpensesChart(startDate.getValue(), endDate.getValue()));
                    break;
                case "Savings Trend":
                    chartContainer.getChildren().add(createSavingsTrendChart(startDate.getValue(), endDate.getValue()));
                    break;
            }
        });

        content.getChildren().addAll(
                new Label("Generate Reports"),
                reportType,
                dateRange,
                generateReport,
                chartContainer);
        tab.setContent(new ScrollPane(content));
        return tab;
    }

    private BarChart<String, Number> createMonthlyExpenseSummary() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle("Monthly Expense Summary");
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount ($)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        // Group expenses by category for current month
        YearMonth currentMonth = YearMonth.now();
        Map<String, Double> categoryExpenses = transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE") &&
                        YearMonth.from(t.getDate()).equals(currentMonth))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));

        categoryExpenses.forEach((category, amount) -> series.getData().add(new XYChart.Data<>(category, amount)));

        barChart.getData().add(series);
        return barChart;
    }

    private LineChart<String, Number> createIncomeVsExpensesChart(LocalDate start, LocalDate end) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Income vs Expenses");
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount ($)");

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");
        expenseSeries.setName("Expenses");

        // Group by date and type
        transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .forEach(t -> {
                    String date = t.getDate().toString();
                    if (t.getType().equals("INCOME")) {
                        incomeSeries.getData().add(new XYChart.Data<>(date, t.getAmount()));
                    } else {
                        expenseSeries.getData().add(new XYChart.Data<>(date, t.getAmount()));
                    }
                });

        lineChart.getData().addAll(incomeSeries, expenseSeries);
        return lineChart;
    }

    private PieChart createCategoryExpensesChart(LocalDate start, LocalDate end) {
        // Group expenses by category
        Map<String, Double> categoryExpenses = transactions.stream()
                .filter(t -> t.getType().equals("EXPENSE") &&
                        !t.getDate().isBefore(start) &&
                        !t.getDate().isAfter(end))
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        categoryExpenses.forEach((category, amount) -> pieChartData
                .add(new PieChart.Data(category + String.format(" ($%.2f)", amount), amount)));

        PieChart chart = new PieChart(pieChartData);
        chart.setTitle("Expenses by Category");
        return chart;
    }

    private LineChart<String, Number> createSavingsTrendChart(LocalDate start, LocalDate end) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Savings Trend");
        xAxis.setLabel("Date");
        yAxis.setLabel("Balance ($)");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Balance");

        double runningBalance = 0;
        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                if (t.getType().equals("INCOME")) {
                    runningBalance += t.getAmount();
                } else {
                    runningBalance -= t.getAmount();
                }
                series.getData().add(new XYChart.Data<>(t.getDate().toString(), runningBalance));
            }
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    // Helper methods
    private VBox createSummaryCard(String title, String amount, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(color + "; -fx-background-radius: 5;");
        card.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        Label amountLabel = new Label(amount);
        amountLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, amountLabel);
        return card;
    }

    private ComboBox<String> createExpenseCategoryComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(
                "Food & Dining",
                "Transportation",
                "Housing",
                "Utilities",
                "Healthcare",
                "Entertainment",
                "Shopping",
                "Other");
        comboBox.setValue("Other");
        return comboBox;
    }

    private ComboBox<String> createIncomeCategoryComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(
                "Salary",
                "Freelance",
                "Business",
                "Investments",
                "Other");
        comboBox.setValue("Salary");
        return comboBox;
    }

    private TableView<Transaction> createTransactionTable() {
        TableView<Transaction> table = new TableView<>();

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        table.getColumns().addAll(dateCol, descCol, categoryCol, amountCol, typeCol);
        table.setItems(transactions);

        return table;
    }

    private void handleLogout() {
        LoginManager.logout();
        showLoginScreen();
    }

    private TextField createStyledTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle("""
                -fx-font-size: 14px;
                -fx-padding: 8px;
                -fx-background-radius: 5;
                """);
        return field;
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-font-size: 14px;
                -fx-padding: 8px 16px;
                -fx-background-color: #2196F3;
                -fx-text-fill: white;
                -fx-background-radius: 5;
                """);
        return button;
    }

    private void showAlert(String title, String message) {
        Alert alert;
        if (title.contains("Error") || title.contains("Failed")) {
            alert = new Alert(Alert.AlertType.ERROR);
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
        }
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addTransaction(String type) {
        try {
            // Validate input
            if (amountField.getText().isEmpty() || descriptionField.getText().isEmpty()) {
                showAlert("Error", "Please fill in amount and description");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            String category = categoryComboBox.getValue();
            LocalDate date = datePicker.getValue();

            Transaction transaction = new Transaction(
                    date,
                    description,
                    category,
                    "", // subCategory
                    amount,
                    type,
                    "" // notes
            );

            transactions.add(transaction);

            // Update balance
            if (type.equals("INCOME")) {
                currentBalance += amount;
            } else {
                currentBalance -= amount;
            }

            // Update balance label
            balanceLabel.setText(String.format("Current Balance: $%.2f", currentBalance));

            // Refresh dashboard
            Tab dashboardTab = tabPane.getTabs().get(0); // Dashboard is the first tab
            VBox content = (VBox) ((ScrollPane) dashboardTab.getContent()).getContent();
            content.getChildren().clear();

            // Recalculate totals
            double totalIncome = transactions.stream()
                    .filter(t -> "INCOME".equals(t.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double totalExpenses = transactions.stream()
                    .filter(t -> "EXPENSE".equals(t.getType()))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            // Recreate dashboard content
            HBox summaryCards = new HBox(20);
            summaryCards.getChildren().addAll(
                    createSummaryCard("Total Income", String.format("$%.2f", totalIncome),
                            "-fx-background-color: #4CAF50;"),
                    createSummaryCard("Total Expenses", String.format("$%.2f", totalExpenses),
                            "-fx-background-color: #f44336;"),
                    createSummaryCard("Monthly Budget", "$0.00", "-fx-background-color: #2196F3;"));

            Label recentTransactionsLabel = new Label("Recent Transactions");
            recentTransactionsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            content.getChildren().addAll(balanceLabel, summaryCards, recentTransactionsLabel, transactionTable);

            // Clear form
            clearFields();
            showAlert("Success", "Transaction added successfully!");

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid amount");
        }
    }

    private void clearFields() {
        amountField.clear();
        descriptionField.clear();
        categoryComboBox.setValue("Other");
        datePicker.setValue(LocalDate.now());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
