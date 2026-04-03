package com.trade.model;

/**
 * Interface for defining risk rules in the portfolio management system.
 * Each implementation validates trades against specific risk criteria.
 */
public interface RiskRule {
    /**
     * Validates if a trade meets the risk criteria.
     * @param trade The trade to validate
     * @param currentCapital The current portfolio capital
     * @return true if the trade passes the risk rule, false otherwise
     */
    boolean validate(Trade trade, double currentCapital);
    
    /**
     * Returns the name of this rule for logging/reporting purposes
     * @return The rule name
     */
    String getRuleName();
}
