package com.tradejournal.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

/**
 * TradeJournal JavaFX Desktop Application
 * Modern, professional UI with trade entry and portfolio analytics
 */
public class TradeJournalFX extends Application {
    
    private static final String API_BASE = "http://localhost:8080/api";
    private static final String DARK_BG = "#1a1a1a";
    private static final String CARD_BG = "#2d2d2d";
    private static final String ACCENT_COLOR = "#00d4ff";
    private static final String SUCCESS_COLOR = "#4caf50";
    private static final String DANGER_COLOR = "#f44336";
    
    private HttpClient httpClient = HttpClient.newHttpClient();
    private ObjectMapper mapper = new ObjectMapper();
    
    private Label portfolioTotalLabel;
    private Label winRateLabel;
    private Label totalTradesLabel;
    private TextArea logArea;
    private TabPane mainTabs;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TradeJournal Professional");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + DARK_BG);
        
        // Header
        root.setTop(createHeader());
        
        // Main content with tabs
        root.setCenter(createMainContent());
        
        // Footer/Status
        root.setBottom(createFooter());
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        checkApiConnection();
    }
    
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #0d0d0d; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        
        Label title = new Label("📊 TradeJournal");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: " + ACCENT_COLOR);
        
        Separator sep = new Separator();
        sep.setPrefWidth(1);
        
        Button refreshBtn = createStyledButton("🔄 Refresh", this::refreshDashboard);
        Button tradesBtn = createStyledButton("📈 Trades", this::switchToTrades);
        Button settingsBtn = createStyledButton("⚙ Settings", this::showSettings);
        
        Label statusLabel = new Label("Connecting...");
        statusLabel.setStyle("-fx-text-fill: #999;");
        
        header.getChildren().addAll(
            title, 
            new Separator(),
            refreshBtn, 
            tradesBtn, 
            settingsBtn,
            new Region(),
            statusLabel
        );
        
        HBox.setHgrow(new Region(), Priority.ALWAYS);
        return header;
    }
    
    private VBox createMainContent() {
        mainTabs = new TabPane();
        mainTabs.setStyle("-fx-control-inner-background: " + DARK_BG);
        mainTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Dashboard Tab
        Tab dashboardTab = new Tab("Dashboard", createDashboard());
        dashboardTab.setStyle("-fx-text-fill: " + ACCENT_COLOR);
        
        // Trade Entry Tab
        Tab tradeEntryTab = new Tab("Add Trade", createTradeEntryForm());
        tradeEntryTab.setStyle("-fx-text-fill: " + ACCENT_COLOR);
        
        mainTabs.getTabs().addAll(dashboardTab, tradeEntryTab);
        
        VBox content = new VBox();
        content.getChildren().add(mainTabs);
        VBox.setVgrow(mainTabs, Priority.ALWAYS);
        
        return content;
    }
    
    private VBox createDashboard() {
        VBox dashboard = new VBox(15);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: " + DARK_BG);
        
        // KPI Cards
        HBox kpiBox = new HBox(15);
        kpiBox.setPrefHeight(120);
        kpiBox.setStyle("-fx-spacing: 15;");
        
        kpiBox.getChildren().addAll(
            createKPICard("Total P&L", portfolioTotalLabel = new Label("—"), ACCENT_COLOR),
            createKPICard("Win Rate", winRateLabel = new Label("—"), SUCCESS_COLOR),
            createKPICard("Total Trades", totalTradesLabel = new Label("—"), "#ff9800")
        );
        
        // Trade List
        VBox tradesSection = new VBox(10);
        tradesSection.setStyle(
            "-fx-border-color: #333; " +
            "-fx-border-radius: 5; " +
            "-fx-background-color: " + CARD_BG + "; " +
            "-fx-padding: 15;"
        );
        
        Label tradesTitle = new Label("Recent Trades");
        tradesTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        tradesTitle.setStyle("-fx-text-fill: white;");
        
        ListView<String> tradeList = new ListView<>();
        tradeList.setStyle("-fx-control-inner-background: " + CARD_BG + "; -fx-text-fill: white;");
        tradeList.setPrefHeight(250);
        
        tradesSection.getChildren().addAll(tradesTitle, tradeList);
        VBox.setVgrow(tradeList, Priority.ALWAYS);
        
        dashboard.getChildren().addAll(kpiBox, tradesSection);
        VBox.setVgrow(tradesSection, Priority.ALWAYS);
        
        return dashboard;
    }
    
    private VBox createTradeEntryForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: " + DARK_BG);
        
        Label formTitle = new Label("Add New Trade");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        formTitle.setStyle("-fx-text-fill: white;");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-padding: 20; -fx-background-color: " + CARD_BG + "; -fx-border-color: #333; -fx-border-radius: 5;");
        
        TextField symbolField = createStyledTextField("RELIANCE");
        ComboBox<String> typeCombo = createStyledComboBox("BUY", "SELL");
        TextField quantityField = createStyledTextField("100");
        TextField priceField = createStyledTextField("2500");
        ComboBox<String> strategyCombo = createStyledComboBox("1", "2", "3");
        
        grid.add(createLabel("Symbol"), 0, 0);
        grid.add(symbolField, 1, 0);
        grid.add(createLabel("Type"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(createLabel("Quantity"), 0, 2);
        grid.add(quantityField, 1, 2);
        grid.add(createLabel("Price"), 0, 3);
        grid.add(priceField, 1, 3);
        grid.add(createLabel("Strategy"), 0, 4);
        grid.add(strategyCombo, 1, 4);
        
        Button submitBtn = new Button("Submit Trade");
        submitBtn.setStyle(
            "-fx-padding: 12 40; " +
            "-fx-font-size: 12; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: " + SUCCESS_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-cursor: hand;"
        );
        submitBtn.setOnAction(e -> submitTrade(symbolField, typeCombo, quantityField, priceField, strategyCombo));
        
        form.getChildren().addAll(formTitle, grid, submitBtn);
        return form;
    }
    
    private VBox createKPICard(String title, Label value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: " + CARD_BG + "; " +
            "-fx-border-color: #333; " +
            "-fx-border-radius: 5; " +
            "-fx-border-width: 2; " +
            "-fx-border-color: " + color
        );
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
        
        value.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 20; -fx-font-weight: bold;");
        
        card.getChildren().addAll(titleLabel, value);
        return card;
    }
    
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setStyle("-fx-background-color: #0d0d0d; -fx-border-color: #333; -fx-border-width: 1 0 0 0;");
        
        logArea = new TextArea();
        logArea.setPrefHeight(80);
        logArea.setStyle("-fx-control-inner-background: " + CARD_BG + "; -fx-text-fill: #00ff00;");
        logArea.setWrapText(true);
        logArea.setEditable(false);
        
        footer.getChildren().add(logArea);
        HBox.setHgrow(logArea, Priority.ALWAYS);
        
        return footer;
    }
    
    private Button createStyledButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-padding: 8 15; " +
            "-fx-font-size: 11; " +
            "-fx-background-color: " + CARD_BG + "; " +
            "-fx-text-fill: " + ACCENT_COLOR + "; " +
            "-fx-border-color: " + ACCENT_COLOR + "; " +
            "-fx-border-radius: 3; " +
            "-fx-cursor: hand;"
        );
        btn.setOnAction(e -> action.run());
        return btn;
    }
    
    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-control-inner-background: " + CARD_BG + "; " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: #666; " +
            "-fx-padding: 8; " +
            "-fx-border-color: #333;"
        );
        field.setPrefHeight(35);
        return field;
    }
    
    private <T> ComboBox<T> createStyledComboBox(T... items) {
        ComboBox<T> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setStyle(
            "-fx-control-inner-background: " + CARD_BG + "; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 8;"
        );
        combo.setPrefHeight(35);
        if (items.length > 0) combo.setValue(items[0]);
        return combo;
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        return label;
    }
    
    private void refreshDashboard() {
        log("📊 Refreshing dashboard...");
        loadAnalytics();
    }
    
    private void switchToTrades() {
        mainTabs.getSelectionModel().selectFirst();
        log("📈 Switched to trades view");
    }
    
    private void showSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("TradeJournal Settings");
        alert.setContentText("Backend: " + API_BASE + "\n\nMore settings coming soon...");
        alert.showAndWait();
    }
    
    private void submitTrade(TextField symbol, ComboBox<String> type, TextField qty, TextField price, ComboBox<String> strategy) {
        log("📤 Submitting trade: " + symbol.getText() + " | " + type.getValue());
    }
    
    private void checkApiConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/health"))
                .GET()
                .build();
            
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        log("✓ Connected to backend");
                        loadAnalytics();
                    } else {
                        log("✗ API error: " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    log("✗ Connection failed: " + e.getMessage());
                    return null;
                });
        } catch (Exception e) {
            log("✗ Error: " + e.getMessage());
        }
    }
    
    private void loadAnalytics() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "/analytics"))
                .GET()
                .build();
            
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            JsonNode json = mapper.readTree(response.body());
                            javafx.application.Platform.runLater(() -> {
                                portfolioTotalLabel.setText(json.get("totalPnl").asText() + " ₹");
                                winRateLabel.setText(json.get("winRate").asText() + "%");
                                totalTradesLabel.setText(json.get("totalTrades").asText());
                                log("✓ Analytics updated");
                            });
                        } catch (Exception e) {
                            log("✗ Parse error: " + e.getMessage());
                        }
                    }
                })
                .exceptionally(e -> {
                    log("✗ Failed to load analytics");
                    return null;
                });
        } catch (Exception e) {
            log("✗ Error: " + e.getMessage());
        }
    }
    
    private void log(String message) {
        javafx.application.Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
