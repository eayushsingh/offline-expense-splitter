import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class SplitterApp extends Application {
    private ListView<String> listView = new ListView<>();
    private List<Expense> expenses = new ArrayList<>();
    private static final String FILE_PATH = "expenses.txt";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Offline Expense Splitter");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter person name");

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount");

        Button addBtn = new Button("Add Expense");
        Button splitBtn = new Button("Split Equally");
        Button saveBtn = new Button("Save");

        HBox inputBox = new HBox(10, nameField, amountField, addBtn);
        VBox layout = new VBox(15, inputBox, splitBtn, saveBtn, listView);
        layout.setStyle("-fx-padding: 15; -fx-background-color: #f4f4f4;");

        addBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double amount = Double.parseDouble(amountField.getText());
                Expense exp = new Expense(name, amount);
                expenses.add(exp);
                listView.getItems().add(exp.toString());
                nameField.clear();
                amountField.clear();
            } catch (Exception ex) {
                showAlert("Error", "Please enter valid data!");
            }
        });

        splitBtn.setOnAction(e -> {
            if (expenses.isEmpty()) {
                showAlert("No Data Found", "Add some expenses first!");
                return;
            }

            double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
            double share = total / expenses.size();

            listView.getItems().add("\nTotal: ₹" + total + " | Each Pays: ₹" + String.format("%.2f", share));
        });

        saveBtn.setOnAction(e -> saveExpenses());

        loadExpenses();

        Scene scene = new Scene(layout, 420, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void saveExpenses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Expense exp : expenses) {
                writer.write(exp.getName() + "," + exp.getAmount());
                writer.newLine();
            }
            showAlert("Saved", "Expenses saved offline!");
        } catch (IOException e) {
            showAlert("Error", "Failed to save expenses!");
        }
    }

    private void loadExpenses() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    Expense exp = new Expense(parts[0], Double.parseDouble(parts[1]));
                    expenses.add(exp);
                    listView.getItems().add(exp.toString());
                }
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load expenses!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
