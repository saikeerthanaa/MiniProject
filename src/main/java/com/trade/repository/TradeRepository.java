package com.trade.repository;

import com.trade.model.Trade;
import java.sql.*;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class TradeRepository {
    private final String url = "jdbc:mysql://localhost:3306/TradeJournal";
    private final String user = "root";
    private final String pass = "redBlue3011!";

    /**
     * Save a trade to the database
     * @param trade Trade object to persist
     * @param portfolioId Portfolio ID this trade belongs to
     * @return The saved trade with assigned ID
     */
    public Trade saveTrade(Trade trade, int portfolioId) {
        String sql = "INSERT INTO Trade (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES (?, ?, ?, ?, ?, CURDATE(), ?)";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, portfolioId);
            pstmt.setString(2, trade.getSymbol());
            pstmt.setString(3, trade.getType());
            pstmt.setInt(4, trade.getQuantity());
            pstmt.setDouble(5, trade.getPrice());
            pstmt.setInt(6, trade.getStrategyId());
            
            pstmt.executeUpdate();
            
            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                trade.setId(rs.getInt(1));
            }
            
            System.out.println("✓ Trade for " + trade.getSymbol() + " saved to database!");
            return trade;
        } catch (SQLException e) {
            System.err.println("Error saving trade: " + e.getMessage());
            e.printStackTrace();
            return trade;
        }
    }

    /**
     * Retrieve all trades from the database
     * @return List of all Trade objects
     */
    public List<Trade> getAllTrades() {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM Trade ORDER BY trade_date DESC LIMIT 100";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Trade trade = new Trade(
                    rs.getString("stock_symbol"),
                    rs.getString("trade_type"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getInt("strategy_id")
                );
                trade.setId(rs.getInt("id"));
                trades.add(trade);
            }
            
            System.out.println("✓ Retrieved " + trades.size() + " trades from database");
            return trades;
        } catch (SQLException e) {
            System.err.println("Error retrieving trades: " + e.getMessage());
            e.printStackTrace();
            return trades;
        }
    }

    /**
     * Retrieve trades for a specific portfolio
     * @param portfolioId Portfolio ID to filter by
     * @return List of Trade objects for the portfolio
     */
    public List<Trade> getTradesByPortfolio(int portfolioId) {
        List<Trade> trades = new ArrayList<>();
        String sql = "SELECT * FROM Trade WHERE portfolio_id = ? ORDER BY trade_date DESC";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Trade trade = new Trade(
                    rs.getString("stock_symbol"),
                    rs.getString("trade_type"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getInt("strategy_id")
                );
                trade.setId(rs.getInt("id"));
                trades.add(trade);
            }
            
            return trades;
        } catch (SQLException e) {
            System.err.println("Error retrieving portfolio trades: " + e.getMessage());
            e.printStackTrace();
            return trades;
        }
    }
}
