package com.trade.model;

public class Trade {
    private int id;
    private String symbol;
    private String type; // BUY or SELL
    private int quantity;
    private double price;
    private int strategyId;

    public Trade(String symbol, String type, int quantity, double price, int strategyId) {
        this.id = 0;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.strategyId = strategyId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public String getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public int getStrategyId() { return strategyId; }
}