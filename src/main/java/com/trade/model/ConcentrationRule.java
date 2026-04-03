package com.trade.model;

import java.math.BigDecimal;

/**
 * ConcentrationRule: Checks if a single trade exceeds 10% of total capital.
 * This prevents over-concentration in a single position.
 */
public class ConcentrationRule implements RiskRule {
    private static final double MAX_CONCENTRATION_PERCENT = 10.0;
    
    @Override
    public boolean validate(Trade trade, double currentCapital) {
        // Handle zero or invalid capital
        if (currentCapital <= 0) {
            System.err.println("ConcentrationRule ERROR: Invalid capital amount: " + currentCapital);
            return false;
        }
        
        // Calculate the trade value using BigDecimal for precision
        BigDecimal tradeValue = BigDecimal.valueOf(trade.getQuantity())
            .multiply(BigDecimal.valueOf(trade.getPrice()));
        
        BigDecimal capital = BigDecimal.valueOf(currentCapital);
        
        // Calculate percentage of capital
        BigDecimal tradePercent = tradeValue
            .divide(capital, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        // Check if trade exceeds 10% of capital
        boolean isValid = tradePercent.compareTo(BigDecimal.valueOf(MAX_CONCENTRATION_PERCENT)) <= 0;
        
        if (!isValid) {
            System.out.println("ConcentrationRule VIOLATED: Trade value " + tradeValue + 
                " exceeds " + MAX_CONCENTRATION_PERCENT + "% of capital. Trade percent: " + 
                tradePercent.setScale(2, java.math.RoundingMode.HALF_UP) + "%");
        }
        
        return isValid;
    }
    
    @Override
    public String getRuleName() {
        return "ConcentrationRule (Max 10% per trade)";
    }
}
