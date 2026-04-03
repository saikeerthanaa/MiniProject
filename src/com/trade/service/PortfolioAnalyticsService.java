package com.trade.service;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;

/**
 * PortfolioAnalyticsService: Provides analytics for portfolio performance
 * including PnL calculations and strategy statistics.
 */
@Service
public class PortfolioAnalyticsService {
    private final String url = "jdbc:mysql://localhost:3306/TradeJournal";
    private final String user = "root";
    private final String pass = "redBlue3011!";
    
    /**
     * Calculate total realized Profit/Loss for a portfolio.
     * PnL = SUM(quantity * price for SELL trades) - SUM(quantity * price for BUY trades)
     * 
     * @param portfolioId The portfolio ID to calculate PnL for
     * @return Total realized PnL as BigDecimal
     */
    public BigDecimal calculatePnL(int portfolioId) {
        String sql = "SELECT " +
            "SUM(CASE WHEN trade_type = 'SELL' THEN quantity * price ELSE 0 END) AS sell_value, " +
            "SUM(CASE WHEN trade_type = 'BUY' THEN quantity * price ELSE 0 END) AS buy_value " +
            "FROM Trade WHERE portfolio_id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                BigDecimal sellValue = rs.getBigDecimal("sell_value");
                BigDecimal buyValue = rs.getBigDecimal("buy_value");
                
                // Handle null values
                if (sellValue == null) sellValue = BigDecimal.ZERO;
                if (buyValue == null) buyValue = BigDecimal.ZERO;
                
                BigDecimal pnl = sellValue.subtract(buyValue);
                System.out.println("PnL for Portfolio " + portfolioId + ": " + pnl);
                return pnl;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating PnL: ");
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Get strategy-wise statistics showing profitability by trading strategy.
     * Groups trades by strategy_id and calculates PnL for each strategy.
     * 
     * @param portfolioId The portfolio ID to analyze
     * @return Map of strategy_id -> strategy PnL
     */
    public Map<Integer, BigDecimal> getStrategyStatistics(int portfolioId) {
        Map<Integer, BigDecimal> strategyPnL = new HashMap<>();
        
        String sql = "SELECT " +
            "s.strategy_id, " +
            "s.strategy_name, " +
            "SUM(CASE WHEN t.trade_type = 'SELL' THEN t.quantity * t.price ELSE 0 END) AS sell_value, " +
            "SUM(CASE WHEN t.trade_type = 'BUY' THEN t.quantity * t.price ELSE 0 END) AS buy_value, " +
            "COUNT(t.trade_id) AS trade_count " +
            "FROM Strategy s " +
            "LEFT JOIN Trade t ON s.strategy_id = t.strategy_id " +
            "WHERE t.portfolio_id = ? " +
            "GROUP BY s.strategy_id, s.strategy_name " +
            "ORDER BY (SUM(CASE WHEN t.trade_type = 'SELL' THEN t.quantity * t.price ELSE 0 END) - " +
            "         SUM(CASE WHEN t.trade_type = 'BUY' THEN t.quantity * t.price ELSE 0 END)) DESC";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n=== Strategy Statistics for Portfolio " + portfolioId + " ===");
            while (rs.next()) {
                int strategyId = rs.getInt("strategy_id");
                String strategyName = rs.getString("strategy_name");
                BigDecimal sellValue = rs.getBigDecimal("sell_value");
                BigDecimal buyValue = rs.getBigDecimal("buy_value");
                int tradeCount = rs.getInt("trade_count");
                
                // Handle null values
                if (sellValue == null) sellValue = BigDecimal.ZERO;
                if (buyValue == null) buyValue = BigDecimal.ZERO;
                
                BigDecimal pnl = sellValue.subtract(buyValue);
                strategyPnL.put(strategyId, pnl);
                
                System.out.printf("Strategy %d (%s): PnL = %s (Trades: %d)%n", 
                    strategyId, strategyName, pnl, tradeCount);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving strategy statistics: ");
            e.printStackTrace();
        }
        
        return strategyPnL;
    }
    
    /**
     * Calculate win rate for a strategy (percentage of profitable trades).
     * 
     * @param strategyId The strategy ID to analyze
     * @param portfolioId The portfolio ID
     * @return Win rate as percentage (0-100)
     */
    public BigDecimal calculateWinRate(int strategyId, int portfolioId) {
        String sql = "SELECT " +
            "COUNT(CASE WHEN (SELECT SUM(quantity * price) FROM Trade t2 " +
            "WHERE t2.strategy_id = ? AND t2.trade_type = 'SELL') > " +
            "(SELECT SUM(quantity * price) FROM Trade t3 " +
            "WHERE t3.strategy_id = ? AND t3.trade_type = 'BUY') THEN 1 END) AS winning_trades, " +
            "COUNT(*) AS total_trades " +
            "FROM Trade WHERE strategy_id = ? AND portfolio_id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, strategyId);
            pstmt.setInt(2, strategyId);
            pstmt.setInt(3, strategyId);
            pstmt.setInt(4, portfolioId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long winningTrades = rs.getLong("winning_trades");
                long totalTrades = rs.getLong("total_trades");
                
                if (totalTrades == 0) return BigDecimal.ZERO;
                
                BigDecimal winRate = BigDecimal.valueOf(winningTrades)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTrades), 2, java.math.RoundingMode.HALF_UP);
                
                return winRate;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating win rate: ");
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Insert or update performance summary into the PerformanceSummary table.
     * 
     * @param portfolioId The portfolio ID
     * @param totalPnL The total profit/loss
     * @param winRate The win rate percentage
     */
    public void updatePerformanceSummary(int portfolioId, BigDecimal totalPnL, BigDecimal winRate) {
        String sql = "INSERT INTO PerformanceSummary (portfolio_id, total_pnl, win_rate, last_updated) " +
            "VALUES (?, ?, ?, NOW()) " +
            "ON DUPLICATE KEY UPDATE total_pnl = VALUES(total_pnl), win_rate = VALUES(win_rate), last_updated = NOW()";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolioId);
            pstmt.setBigDecimal(2, totalPnL);
            pstmt.setBigDecimal(3, winRate);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("PerformanceSummary updated: " + rowsAffected + " row(s) affected");
        } catch (SQLException e) {
            System.err.println("Error updating performance summary: ");
            e.printStackTrace();
        }
    }
}
