import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.scene.control.*;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.util.Base64;
import java.util.*;

public class Main extends Application {

    private VBox peopleList;
    private final List<Person> people = new ArrayList<>();
    private Label resultLabel;
    private javafx.scene.text.TextFlow settlementFlow;
    private VBox contributorsBox;
    // Contributors UI controls promoted to fields so other methods can update them
    private StackPane contributorsContainer;
    private javafx.scene.control.Label contributorsCaret;
    private javafx.beans.property.BooleanProperty contributorsExpanded;
    private javafx.scene.control.Label contributorsHeaderLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
    stage.setTitle("Offline Expense Splitter");

    Label title = new Label("Offline Expense Splitter");
    title.setFont(Font.font("Poppins", FontWeight.BOLD, 36));
    title.setTextFill(Color.WHITE);

    // Ensure a placeholder logo exists in the project root so the header area is always available.
    final ImageView logoView = new ImageView();
        try {
            Image logo = null;
            Path p1 = Path.of("logo.png");
            Path p2 = Path.of("resources", "logo.png");

            // If there's no logo file, write a tiny placeholder PNG to disk (1x1 transparent PNG).
            try {
                if (!Files.exists(p1)) {
                    byte[] placeholder = Base64.getDecoder().decode(
                        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg=="
                    );
                    Files.write(p1, placeholder);
                }
            } catch (Exception ignoredWrite) {
                // non-fatal: if we can't write a placeholder, continue (we'll try other sources)
            }

            if (Files.exists(p1)) {
                logo = new Image(p1.toUri().toString());
            } else if (Files.exists(p2)) {
                logo = new Image(p2.toUri().toString());
            } else {
                // try loading from classpath (if packaged into jar)
                InputStream is = getClass().getResourceAsStream("/logo.png");
                if (is != null) {
                    logo = new Image(is);
                }
            }

            // If no local/classpath logo found, try the provided remote URL (falls back gracefully if offline)
            if (logo == null) {
                // try a list of remote URLs (first available will be downloaded and cached)
                String[] remotes = new String[]{
                    "https://lh3.googleusercontent.com/gg-dl/ABS2GSloVsyVpn6VWMiXBqRYE9rTTIaGIT-H9ZV5_Nm1jfzNVXDV3OuHpEr9osIEENzq42kfvQ6FOII36LbIGAVgDgC1OeU8JYzw9TcBwo1Slbm_WMwnQ6Va9Rl0ebT-v9nmkAPlyfanVeNDUXzMJGMzzl4N4l1gEwpUQgs8m7jgY1xc7Mku9Q=s1024-rj",
                    "https://lh3.googleusercontent.com/gg/AIJ2gl8TqbH_7kuke8rcrILg2XKZFKVBHAqsVRJI7jSfE-wK3LAukwviXLXij8ani_eoOBMD1w1ilyHwE89GnPGyQEdJ3MqyLk7TuM7pQODvTMCOgcQAZOt8moNq5trwuxy3yJyknEsVygjon9CqZQwJrVIV57B78s_rIhThxlVM3WZ6dpfKHlxoN1ha0xOo_BvYnWj-k2c5t3ZzLeSNR_hYPueZ0Hxp9KEZOoDkfCEkQKCtJz-rSpbifGB359WAOawzFelQ-1SGnVlOHOAibj04wU68qBUxXx6hpfm8bH8H5RW4f5umai9xuu03h-AEeasbDh34Ohz2iLrh2U1mQcfBO9An=s1024-rj"
                };
                for (String remote : remotes) {
                    try {
                        // First try loading the remote image directly (synchronous). This
                        // avoids curl/redirect issues and will work if the URL is a direct
                        // image link that JavaFX can fetch.
                        try {
                            Image remoteImg = new Image(remote, false);
                            if (remoteImg.getWidth() > 0) {
                                logo = remoteImg;
                                break;
                            }
                        } catch (Exception e) {
                            // fall through to download attempt
                        }

                        // If direct load didn't work, attempt to download and cache the file
                        URL url = new URL(remote);
                        try (InputStream rin = url.openStream()) {
                            Files.copy(rin, p1, StandardCopyOption.REPLACE_EXISTING);
                            logo = new Image(p1.toUri().toString());
                            break; // stop after first successful download
                        }
                    } catch (Exception ignored2) {
                        // try next remote
                    }
                }
            }

            if (logo != null) {
                logoView.setImage(logo);
                // Make the ImageView square and prepare for circular clipping
                logoView.setFitWidth(92);
                logoView.setFitHeight(92);
                logoView.setPreserveRatio(false);
                logoView.setSmooth(true);
                logoView.setCache(true);

                // Create a circular clip that follows the ImageView size
                Circle clip = new Circle();
                clip.centerXProperty().bind(logoView.fitWidthProperty().divide(2));
                clip.centerYProperty().bind(logoView.fitHeightProperty().divide(2));
                clip.radiusProperty().bind(logoView.fitWidthProperty().divide(2));
                logoView.setClip(clip);

                // Create a subtle circular border and shadow behind the image
                Circle border = new Circle();
                border.radiusProperty().bind(logoView.fitWidthProperty().divide(2));
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.web("rgba(255,255,255,0.12)"));
                border.setStrokeWidth(2);
                DropShadow ds = new DropShadow(6, Color.rgb(0,0,0,0.35));
                border.setEffect(ds);

                // Wrap in a pane so header spacing remains consistent
                StackPane logoStack = new StackPane(border, logoView);
                logoStack.setPrefSize(92, 92);
                logoStack.setMaxSize(92, 92);
                logoStack.setMinSize(92, 92);

                // store the stack into the userData of logoView so we can retrieve it later when building header
                logoView.setUserData(logoStack);
            }
        } catch (Exception ignored) {
            // ignore - logoView will simply have no image
        }

        peopleList = new VBox(10);
        peopleList.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(peopleList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent;");

        Button addButton = new Button("+ Add Person");
    addButton.setStyle("-fx-background-color: linear-gradient(#00C896, #00A36C); -fx-text-fill: white; -fx-font-size: 15px; -fx-background-radius: 10; -fx-padding: 8 14;");
        addButton.setOnAction(e -> addPersonRow());

        Button calculateButton = new Button("⚡ Split Expenses");
    calculateButton.setStyle("-fx-background-color: linear-gradient(#3D5AFE, #0B5FFF); -fx-text-fill: white; -fx-font-size: 15px; -fx-background-radius: 10; -fx-padding: 8 14;");
        calculateButton.setOnAction(e -> calculateSplit());

        Button resetButton = new Button("Start New");
        resetButton.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.12); -fx-text-fill: #E6EEF8; -fx-background-radius: 10; -fx-padding: 8 14;");
        resetButton.setOnAction(e -> resetAll());

        // Allow the user to upload a custom logo that will replace logo.png in the project root
        Button uploadLogoBtn = new Button("Upload Logo");
        uploadLogoBtn.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 12;");
        uploadLogoBtn.setOnAction(ev -> {
            try {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Choose Logo Image");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
                File sel = chooser.showOpenDialog(stage);
                if (sel != null) {
                    Path dest = Path.of("logo.png");
                    Files.copy(sel.toPath(), dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Image newImg = new Image(dest.toUri().toString());
                    if (logoView != null) logoView.setImage(newImg);
                }
            } catch (Exception ex) {
                // show simple alert on failure
                Alert a = new Alert(Alert.AlertType.ERROR, "Failed to upload logo: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

    resultLabel = new Label("Enter expenses to see detailed settlements 💡");
    resultLabel.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 16));
    resultLabel.setTextFill(Color.web("#FFD166"));
    resultLabel.setWrapText(true);

    // Rich settlement flow (styled text) and contributors area
    settlementFlow = new javafx.scene.text.TextFlow();
    settlementFlow.setPrefWidth(780);
    settlementFlow.setLineSpacing(6);

    contributorsBox = new VBox(6);

    // Create a custom animated collapsible pane for Contributors
    HBox headerBox = new HBox();
    headerBox.setAlignment(Pos.CENTER_LEFT);
    headerBox.setSpacing(8);
    contributorsHeaderLabel = new Label("👥 Contributors (0)");
    contributorsHeaderLabel.setTextFill(Color.web("#E6EEF8"));
    contributorsHeaderLabel.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 14));
    Region headerSpacer = new Region();
    HBox.setHgrow(headerSpacer, Priority.ALWAYS);
    contributorsCaret = new Label("▾");
    contributorsCaret.setTextFill(Color.web("#B0BEC5"));
    contributorsCaret.setRotate(90); // pointing down when expanded
    headerBox.getChildren().addAll(contributorsHeaderLabel, headerSpacer, contributorsCaret);

    // content container that we animate by changing maxHeight; make it scrollable
    ScrollPane contributorsScroll = new ScrollPane(contributorsBox);
    contributorsScroll.setFitToWidth(true);
    contributorsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    contributorsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    contributorsScroll.setStyle("-fx-background: transparent; -fx-padding: 4;");

    contributorsContainer = new StackPane(contributorsScroll);
    contributorsContainer.setMaxHeight(Region.USE_COMPUTED_SIZE);
    contributorsContainer.setMinHeight(0);
    contributorsContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

    contributorsExpanded = new SimpleBooleanProperty(true); // expanded by default

    // style the contributors box for padding/background
    contributorsBox.setPadding(new Insets(8));
    contributorsBox.setSpacing(6);
    contributorsBox.setStyle("-fx-background-color: transparent;");

    // toggle animation (smooth expand/collapse)
    headerBox.setOnMouseClicked(evt -> {
        double contentHeight = Math.min(contributorsBox.prefHeight(-1) + 16, 300); // cap at 300px
        Timeline timeline = new Timeline();
        if (!contributorsExpanded.get()) {
            KeyValue kv = new KeyValue(contributorsContainer.maxHeightProperty(), contentHeight, Interpolator.EASE_BOTH);
            KeyValue kv2 = new KeyValue(contributorsCaret.rotateProperty(), 90, Interpolator.EASE_BOTH);
            timeline.getKeyFrames().addAll(new KeyFrame(Duration.millis(260), kv), new KeyFrame(Duration.millis(260), kv2));
            contributorsExpanded.set(true);
        } else {
            KeyValue kv = new KeyValue(contributorsContainer.maxHeightProperty(), 0, Interpolator.EASE_BOTH);
            KeyValue kv2 = new KeyValue(contributorsCaret.rotateProperty(), 0, Interpolator.EASE_BOTH);
            timeline.getKeyFrames().addAll(new KeyFrame(Duration.millis(220), kv), new KeyFrame(Duration.millis(220), kv2));
            contributorsExpanded.set(false);
        }
        timeline.play();
    });

    VBox contributorsPane = new VBox(6, headerBox, contributorsContainer);

    VBox resultCard = new VBox(12, settlementFlow, new Separator(), contributorsPane);
    resultCard.setPadding(new Insets(20));
    resultCard.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 15; -fx-border-color: rgba(255,215,0,0.06); -fx-border-radius:15; -fx-padding:12;");
    resultCard.setAlignment(Pos.CENTER_LEFT);

        // place logo to the left of the title and add a small subtitle under the title
        javafx.scene.Node titleNode;
        if (logoView.getImage() != null) {
            Label subtitle = new Label("by using ");
            // create stylized subtitle with 'Java' colored
            javafx.scene.text.Text jText = new javafx.scene.text.Text("Java");
            jText.setFill(Color.web("#2D9CDB"));
            jText.setStyle("-fx-font-weight: 700;");
            javafx.scene.text.TextFlow subtitleFlow = new javafx.scene.text.TextFlow(new javafx.scene.text.Text("by using "), jText);
            subtitleFlow.setPrefWidth(400);
            subtitleFlow.setStyle("-fx-opacity: 0.85; -fx-font-size: 12px;");

            VBox titleBlock = new VBox(4, title, subtitleFlow);
            titleBlock.setAlignment(Pos.CENTER_LEFT);

            // Retrieve the prepared circular stack if present
            javafx.scene.Node logoNode = logoView.getUserData() instanceof StackPane ? (StackPane) logoView.getUserData() : logoView;
            HBox header = new HBox(14, logoNode, titleBlock);
            header.setAlignment(Pos.CENTER_LEFT);
            titleNode = header;
        } else {
            titleNode = title;
        }

    VBox root = new VBox(20, titleNode, scrollPane, addButton, calculateButton, resetButton, resultCard);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #0F2027, #203A43, #2C5364);
            -fx-background-radius: 25;
        """);

        Scene scene = new Scene(root, 900, 700);
        stage.setScene(scene);
        stage.show();
    }

    private void addPersonRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        nameField.setPrefWidth(150);

        TextField expenseField = new TextField();
        expenseField.setPromptText("Expense (₹)");
        expenseField.setPrefWidth(120);

        TextField whereField = new TextField();
        whereField.setPromptText("Where (optional)");
        whereField.setPrefWidth(220);

        Button removeBtn = new Button("✖");
        removeBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8;");
        removeBtn.setOnAction(e -> peopleList.getChildren().remove(row));

        row.getChildren().addAll(nameField, expenseField, whereField, removeBtn);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-padding: 8; -fx-background-radius: 10;");
        peopleList.getChildren().add(row);
    }

    private void calculateSplit() {
        people.clear();
        double total = 0;

        for (javafx.scene.Node node : peopleList.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                TextField nameField = (TextField) row.getChildren().get(0);
                TextField expenseField = (TextField) row.getChildren().get(1);
                TextField whereField = (TextField) row.getChildren().get(2);

                String name = nameField.getText().trim();
                String expText = expenseField.getText().trim();
                String where = whereField.getText().trim();

                // include person even if expense field is left blank (treat as 0)
                if (!name.isEmpty()) {
                    double expense = 0.0;
                    if (!expText.isEmpty()) {
                        try {
                            expense = Double.parseDouble(expText);
                        } catch (NumberFormatException ignored) {
                            // skip malformed expense entries for that person
                            continue;
                        }
                    }
                    people.add(new Person(name, expense, where));
                    total += expense;
                }
            }
        }

        if (people.isEmpty()) {
            resultLabel.setText("⚠️ Please enter valid names and expenses first.");
            return;
        }

        double avg = total / people.size();

        Map<String, Double> balances = new HashMap<>();
        for (Person p : people) balances.put(p.name, p.expense - avg);

        List<Transaction> transactions = settleBalances(balances);

        // --- Styled settlement flow ---
        settlementFlow.getChildren().clear();

        javafx.scene.text.Text totalText = new javafx.scene.text.Text("💰 Total = ₹" + String.format("%.2f", total) + "\n");
        totalText.setFill(Color.web("#FFD166"));
        totalText.setStyle("-fx-font-weight: 700; -fx-font-size: 14pt;");

        javafx.scene.text.Text perText = new javafx.scene.text.Text("Each should pay = ₹" + String.format("%.2f", avg) + "\n\n");
        perText.setFill(Color.web("#E6EEF8"));
        perText.setStyle("-fx-font-size: 12pt;");

        settlementFlow.getChildren().addAll(totalText, perText);

        Map<String, List<Transaction>> byPayer = new LinkedHashMap<>();
        for (Transaction t : transactions) {
            byPayer.computeIfAbsent(t.from, k -> new ArrayList<>()).add(t);
        }

        for (Map.Entry<String, List<Transaction>> entry : byPayer.entrySet()) {
            String payer = entry.getKey();
            List<Transaction> txs = entry.getValue();

            javafx.scene.text.Text prefix = new javafx.scene.text.Text("➡️ " + payer + " will pay ");
            prefix.setFill(Color.WHITE);
            prefix.setStyle("-fx-font-size: 13pt;");

            settlementFlow.getChildren().add(prefix);

            for (int i = 0; i < txs.size(); i++) {
                Transaction t = txs.get(i);
                javafx.scene.text.Text payee = new javafx.scene.text.Text(t.to + " ");
                payee.setFill(Color.WHITE);
                javafx.scene.text.Text amt = new javafx.scene.text.Text("₹" + String.format("%.2f", t.amount));
                amt.setFill(Color.web("#FFD166"));
                amt.setStyle("-fx-font-weight: 700;");
                settlementFlow.getChildren().addAll(payee, amt);
                if (i < txs.size() - 1) settlementFlow.getChildren().add(new javafx.scene.text.Text(" and "));
            }
            settlementFlow.getChildren().add(new javafx.scene.text.Text("\n"));
        }

    // --- Contributors list ---
    contributorsBox.getChildren().clear();
        for (Person p : people) {
            HBox h = new HBox(10);
            h.setAlignment(Pos.CENTER_LEFT);
            h.setPadding(new Insets(8));
            h.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8;");

            VBox left = new VBox(2);
            Label nameLbl = new Label(p.name);
            nameLbl.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 13));
            nameLbl.setTextFill(Color.web("#E6EEF8"));
            Label whereLbl = new Label();
            if (p.where != null && !p.where.isEmpty()) {
                whereLbl.setText(p.where);
                whereLbl.setStyle("-fx-font-style: italic; -fx-text-fill: #B0BEC5; -fx-font-size: 11px;");
            }
            left.getChildren().addAll(nameLbl);
            if (p.where != null && !p.where.isEmpty()) left.getChildren().add(whereLbl);

            Label amtLbl = new Label("₹" + String.format("%.2f", p.expense));
            amtLbl.setFont(Font.font("Poppins", FontWeight.BOLD, 13));
            amtLbl.setTextFill(Color.web("#FFD166"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            h.getChildren().addAll(left, spacer, amtLbl);
            contributorsBox.getChildren().add(h);
        }

        // update contributors header with count
        contributorsHeaderLabel.setText("👥 Contributors (" + people.size() + ")");

        // copy button
        Button copyBtn = new Button("Copy Summary");
        copyBtn.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: white; -fx-background-radius: 8;");
        copyBtn.setOnAction(ev -> {
            StringBuilder sb = new StringBuilder();
            for (javafx.scene.Node n : settlementFlow.getChildren()) {
                sb.append(((javafx.scene.text.Text) n).getText());
            }
            sb.append("\nContributors:\n");
            for (Person p : people) {
                sb.append(p.name).append(": ₹").append(String.format("%.2f", p.expense));
                if (p.where != null && !p.where.isEmpty()) sb.append(" (").append(p.where).append(")");
                sb.append("\n");
            }
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(sb.toString());
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
        });

    // give copy button some top margin and center it
    HBox copyWrap = new HBox(copyBtn);
    copyWrap.setAlignment(Pos.CENTER);
    copyWrap.setPadding(new Insets(8,0,0,0));
    contributorsBox.getChildren().add(copyWrap);

    // ensure container is expanded and sized to fit content (with cap)
    double contentHeight = Math.min(contributorsBox.prefHeight(-1) + 16, 300);
    contributorsContainer.setMaxHeight(contentHeight);
    contributorsCaret.setRotate(90);

        // clear any simple message
        resultLabel.setText("");
    }

    private List<Transaction> settleBalances(Map<String, Double> balances) {
        List<Transaction> transactions = new ArrayList<>();
        List<Map.Entry<String, Double>> creditors = new ArrayList<>();
        List<Map.Entry<String, Double>> debtors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() > 0) creditors.add(entry);
            else if (entry.getValue() < 0) debtors.add(entry);
        }

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            String debtor = debtors.get(i).getKey();
            String creditor = creditors.get(j).getKey();
            double debt = -debtors.get(i).getValue();
            double credit = creditors.get(j).getValue();
            double amount = Math.min(debt, credit);

            transactions.add(new Transaction(debtor, creditor, amount));

            debtors.get(i).setValue(debtors.get(i).getValue() + amount);
            creditors.get(j).setValue(creditors.get(j).getValue() - amount);

            if (Math.abs(debtors.get(i).getValue()) < 1e-6) i++;
            if (Math.abs(creditors.get(j).getValue()) < 1e-6) j++;
        }

        return transactions;
    }

    // Reset the whole form and summary to start fresh
    private void resetAll() {
        peopleList.getChildren().clear();
        people.clear();
        settlementFlow.getChildren().clear();
        contributorsBox.getChildren().clear();
        resultLabel.setText("Enter expenses to see detailed settlements 💡");
        resultLabel.setTextFill(Color.web("#FFD166"));
    }

    static class Person {
        String name;
        double expense;
        String where;

        Person(String name, double expense, String where) {
            this.name = name;
            this.expense = expense;
            this.where = where;
        }
    }

    static class Transaction {
        String from, to;
        double amount;

        Transaction(String from, String to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }
    }
}





