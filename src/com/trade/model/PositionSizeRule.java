package com.trade.model;

/**
 * PositionSizeRule: Checks if the trade quantity is within acceptable limits.
 * Prevents excessively large position sizes that could impact market liquidity.
 */
public class PositionSizeRule implements RiskRule {
    private static final int MAX_QUANTITY = 10000; // Maximum shares per trade
    private static final int MIN_QUANTITY = 1;      // Minimum shares per trade
    
    @Override
    public boolean validate(Trade trade, double currentCapital) {
        int quantity = trade.getQuantity();
        
        // Check if quantity is within acceptable limits
        boolean isValid = quantity >= MIN_QUANTITY && quantity <= MAX_QUANTITY;
        
        if (!isValid) {
            System.out.println("PositionSizeRule VIOLATED: Trade quantity " + quantity + 
                " is outside acceptable range [" + MIN_QUANTITY + ", " + MAX_QUANTITY + "]");
        }
        
        return isValid;
    }
    
    @Override
    public String getRuleName() {
        return "PositionSizeRule (Qty: 1-10000)";
    }
}
