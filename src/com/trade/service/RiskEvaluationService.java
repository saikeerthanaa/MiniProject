package com.trade.service;

import com.trade.model.*;
import java.sql.*;
import java.util.*;
import org.springframework.stereotype.Service;

/**
 * RiskEvaluationService: Evaluates trades against a set of risk rules.
 * Supports pluggable risk rules via the RiskRule interface.
 */
@Service
public class RiskEvaluationService {
    private final String url = "jdbc:mysql://localhost:3306/TradeJournal";
    private final String user = "root";
    private final String pass = "redBlue3011!";
    private List<RiskRule> rules;
    
    /**
     * Initialize the service with default risk rules.
     */
    public RiskEvaluationService() {
        this.rules = new ArrayList<>();
        // Add default rules
        this.rules.add(new ConcentrationRule());
        this.rules.add(new PositionSizeRule());
    }
    
    /**
     * Initialize the service with custom rules.
     * @param rules List of RiskRule implementations to evaluate
     */
    public RiskEvaluationService(List<RiskRule> rules) {
        this.rules = rules;
    }
    
    /**
     * Add a new rule to the evaluation list.
     * @param rule The RiskRule to add
     */
    public void addRule(RiskRule rule) {
        this.rules.add(rule);
        System.out.println("Added rule: " + rule.getRuleName());
    }
    
    /**
     * Evaluate a trade against all configured rules.
     * Returns true only if the trade passes ALL rules.
     * 
     * @param trade The trade to evaluate
     * @param portfolioId The portfolio ID to retrieve current capital
     * @return true if trade passes all rules, false otherwise
     */
    public boolean evaluateTrade(Trade trade, int portfolioId) {
        double currentCapital = getCurrentCapital(portfolioId);
        
        System.out.println("\n=== Evaluating Trade: " + trade.getSymbol() + " ===");
        System.out.println("Current Capital: " + currentCapital);
        
        for (RiskRule rule : rules) {
            boolean ruleResult = rule.validate(trade, currentCapital);
            
            if (!ruleResult) {
                System.out.println("TRADE REJECTED by rule: " + rule.getRuleName());
                flagRiskViolation(trade, portfolioId, rule.getRuleName());
                return false;
            }
            System.out.println("PASS: " + rule.getRuleName());
        }
        
        System.out.println("TRADE APPROVED: All rules passed");
        return true;
    }
    
    /**
     * Get the current capital of a portfolio from the database.
     * 
     * @param portfolioId The portfolio ID
     * @return Current capital amount
     */
    private double getCurrentCapital(int portfolioId) {
        String sql = "SELECT total_capital FROM Portfolio WHERE portfolio_id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolioId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double capital = rs.getDouble("total_capital");
                if (capital <= 0) {
                    System.err.println("Warning: Portfolio capital is 0 or invalid");
                }
                return capital;
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving portfolio capital: ");
            e.printStackTrace();
        }
        
        return 100000.0; // Default fallback capital
    }
    
    /**
     * Flag a risk violation in the database for audit and review.
     * 
     * @param trade The trade that violated the rule
     * @param portfolioId The portfolio ID
     * @param ruleName The name of the violated rule
     */
    private void flagRiskViolation(Trade trade, int portfolioId, String ruleName) {
        String sql = "INSERT INTO RiskFlag (portfolio_id, trade_symbol, violated_rule, flag_date) " +
            "VALUES (?, ?, ?, NOW())";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, portfolioId);
            pstmt.setString(2, trade.getSymbol());
            pstmt.setString(3, ruleName);
            
            pstmt.executeUpdate();
            System.out.println("Risk violation flagged in database for: " + trade.getSymbol());
        } catch (SQLException e) {
            System.err.println("Error flagging risk violation (may occur if RiskFlag table not properly set up): ");
            // Continue execution even if flagging fails
        }
    }
    
    /**
     * Get all active rules.
     * @return List of configured RiskRule objects
     */
    public List<RiskRule> getRules() {
        return new ArrayList<>(this.rules);
    }
}
