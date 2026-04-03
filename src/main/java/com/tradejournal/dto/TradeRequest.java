package com.tradejournal.dto;

/**
 * TradeRequest — Incoming trade data from frontend
 * Expected JSON:
 * {
 *   "symbol": "RELIANCE",
 *   "tradeType": "BUY",
 *   "quantity": 100,
 *   "price": 2500.00,
 *   "strategyId": "1"
 * }
 */
public class TradeRequest {
    public String symbol;
    public String tradeType;
    public Double quantity;
    public Double price;
    public String strategyId;
}
