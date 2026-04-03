import com.trade.model.*;
import com.trade.service.*;

/**
 * Demo application showing:
 * 1. Risk evaluation using rule-based system
 * 2. Portfolio analytics with PnL calculations
 * 3. Strategy statistics and performance tracking
 */
public class PortfolioDemo {
    public static void main(String[] args) {
        System.out.println("===== Rule-Based Portfolio Analytics Demo =====\n");
        
        // Initialize services
        RiskEvaluationService riskService = new RiskEvaluationService();
        PortfolioAnalyticsService analyticsService = new PortfolioAnalyticsService();
        
        // Demo 1: Risk Rule Evaluation
        System.out.println("\n--- Demo 1: Risk Rule Evaluation ---");
        demonstrateRiskRules(riskService);
        
        // Demo 2: Portfolio Analytics
        System.out.println("\n--- Demo 2: Portfolio Analytics ---");
        demonstratePortfolioAnalytics(analyticsService);
    }
    
    /**
     * Demonstrates risk evaluation with ConcentrationRule and PositionSizeRule
     */
    private static void demonstrateRiskRules(RiskEvaluationService riskService) {
        // Portfolio ID: 1 (assumed capital: 100000)
        int portfolioId = 1;
        
        // Test Case 1: Good trade (within limits)
        Trade goodTrade = new Trade("RELIANCE", "BUY", 100, 2500.00, 1);
        System.out.println("\nTest 1: Small trade (quantity=100, price=2500)");
        boolean result1 = riskService.evaluateTrade(goodTrade, portfolioId);
        System.out.println("Result: " + (result1 ? "APPROVED" : "REJECTED"));
        
        // Test Case 2: Concentration Risk (exceeds 10% of capital)
        Trade concentrationRiskTrade = new Trade("BHARTI", "BUY", 5000, 600.00, 2);
        System.out.println("\nTest 2: Large trade (quantity=5000, price=600, value=3M)");
        boolean result2 = riskService.evaluateTrade(concentrationRiskTrade, portfolioId);
        System.out.println("Result: " + (result2 ? "APPROVED" : "REJECTED"));
        
        // Test Case 3: Position Size Risk (exceeds 10000 limit)
        Trade positionSizeRiskTrade = new Trade("INFY", "BUY", 15000, 1500.00, 3);
        System.out.println("\nTest 3: Excessive quantity (quantity=15000 > 10000 limit)");
        boolean result3 = riskService.evaluateTrade(positionSizeRiskTrade, portfolioId);
        System.out.println("Result: " + (result3 ? "APPROVED" : "REJECTED"));
        
        // Test Case 4: Custom rule added
        System.out.println("\nTest 4: Adding custom rule (PriceThresholdRule)");
        riskService.addRule(new PriceThresholdRule(5000));
        Trade highPriceTrade = new Trade("TCS", "BUY", 50, 6000.00, 1);
        boolean result4 = riskService.evaluateTrade(highPriceTrade, portfolioId);
        System.out.println("Result: " + (result4 ? "APPROVED" : "REJECTED"));
    }
    
    /**
     * Demonstrates portfolio analytics functionality
     */
    private static void demonstratePortfolioAnalytics(PortfolioAnalyticsService analyticsService) {
        int portfolioId = 1;
        
        // Calculate and display PnL
        System.out.println("\nCalculating Portfolio PnL...");
        java.math.BigDecimal pnl = analyticsService.calculatePnL(portfolioId);
        
        // Get strategy-wise statistics
        System.out.println("\nRetrieving Strategy Statistics...");
        java.util.Map<Integer, java.math.BigDecimal> strategyStats = 
            analyticsService.getStrategyStatistics(portfolioId);
        
        System.out.println("\nStrategy Count: " + strategyStats.size());
        for (java.util.Map.Entry<Integer, java.math.BigDecimal> entry : strategyStats.entrySet()) {
            System.out.println("  Strategy " + entry.getKey() + ": PnL = " + entry.getValue());
        }
        
        // Calculate win rate for strategy 1
        System.out.println("\nCalculating Win Rate for Strategy 1...");
        java.math.BigDecimal winRate = analyticsService.calculateWinRate(1, portfolioId);
        System.out.println("Win Rate: " + winRate + "%");
        
        // Update performance summary
        System.out.println("\nUpdating Performance Summary...");
        analyticsService.updatePerformanceSummary(portfolioId, pnl, winRate);
    }
}

/**
 * Example custom rule: Prevent trades above a price threshold
 */
class PriceThresholdRule implements RiskRule {
    private double maxPrice;
    
    public PriceThresholdRule(double maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    @Override
    public boolean validate(Trade trade, double currentCapital) {
        boolean isValid = trade.getPrice() <= maxPrice;
        
        if (!isValid) {
            System.out.println("PriceThresholdRule VIOLATED: Price " + trade.getPrice() + 
                " exceeds limit of " + maxPrice);
        }
        
        return isValid;
    }
    
    @Override
    public String getRuleName() {
        return "PriceThresholdRule (Max: " + maxPrice + ")";
    }
}
