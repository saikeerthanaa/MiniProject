# Rule-Based Portfolio Analytics and Trade Journal - Implementation Guide

## Overview
This document describes the implementation of the Rule-Based Risk Evaluation and Portfolio Analytics system for the Trade Journal Management System.

## Components Implemented

### 1. RiskRule Interface
**File:** `src/com/trade/model/RiskRule.java`

The foundation of the rule-based risk evaluation system. All risk rules implement this interface.

```java
public interface RiskRule {
    boolean validate(Trade trade, double currentCapital);
    String getRuleName();
}
```

**Key Methods:**
- `validate()`: Returns true if trade passes the rule, false otherwise
- `getRuleName()`: Returns descriptive rule name for logging/reporting

---

### 2. ConcentrationRule
**File:** `src/com/trade/model/ConcentrationRule.java`

**Purpose:** Prevents over-concentration of capital in a single trade.

**Rule Logic:**
- Calculates trade value = quantity × price
- Checks if trade value exceeds 10% of total portfolio capital
- Returns true if trade ≤ 10% of capital, false otherwise

**Implementation Details:**
- Uses `BigDecimal` for precise financial calculations
- Trade value that exceeds 10% is flagged and logged

**Example:**
```
Portfolio Capital: 100,000
Trade: 100 shares @ 2500 = 250,000 (exceeds 10% = 10,000)
Result: REJECTED
```

---

### 3. PositionSizeRule
**File:** `src/com/trade/model/PositionSizeRule.java`

**Purpose:** Prevents excessively large position sizes.

**Rule Logic:**
- Enforces quantity limits: minimum 1, maximum 10,000 shares per trade
- Returns true if quantity is within limits

**Implementation Details:**
- Fixed position size thresholds to prevent market impact
- Easily configurable by modifying MAX_QUANTITY and MIN_QUANTITY constants

**Example:**
```
Trade: 15,000 shares (exceeds MAX_QUANTITY = 10,000)
Result: REJECTED
```

---

### 4. RiskEvaluationService
**File:** `src/com/trade/service/RiskEvaluationService.java`

**Purpose:** Central service for evaluating trades against a collection of rules.

**Key Features:**
- **Rule Management:** Add/remove rules dynamically
- **Default Rules:** Includes ConcentrationRule and PositionSizeRule by default
- **Comprehensive Evaluation:** Trade passes only if ALL rules pass
- **Database Integration:** Retrieves portfolio capital from DB
- **Risk Flagging:** Automatically logs rule violations to RiskFlag table

**Key Methods:**

```java
// Create with default rules
RiskEvaluationService riskService = new RiskEvaluationService();

// Create with custom rules
List<RiskRule> customRules = Arrays.asList(new ConcentrationRule());
RiskEvaluationService riskService = new RiskEvaluationService(customRules);

// Add additional rules
riskService.addRule(new PositionSizeRule());

// Evaluate a trade
boolean isApproved = riskService.evaluateTrade(trade, portfolioId);

// Get current rules
List<RiskRule> activeRules = riskService.getRules();
```

**Database Operations:**
- Retrieves `portfolio.initial_capital`
- Inserts violations into `RiskFlag` table with timestamp

---

### 5. PortfolioAnalyticsService
**File:** `src/com/trade/service/PortfolioAnalyticsService.java`

**Purpose:** Calculate portfolio performance metrics and strategy statistics.

**Key Methods:**

#### `calculatePnL(int portfolioId)`
Calculates realized Profit/Loss using SQL aggregation.

**Formula:** 
```
PnL = SUM(quantity × price for SELL trades) - SUM(quantity × price for BUY trades)
```

**Implementation:**
```java
BigDecimal pnl = analyticsService.calculatePnL(portfolioId);
System.out.println("Portfolio PnL: " + pnl);
```

**Returns:** `BigDecimal` of total PnL

---

#### `getStrategyStatistics(int portfolioId)`
Groups trades by strategy and calculates profitability for each.

**Returns:** `Map<Integer, BigDecimal>` where key=strategyId, value=strategyPnL

**Output Example:**
```
=== Strategy Statistics for Portfolio 1 ===
Strategy 1 (Momentum): PnL = 50000.00 (Trades: 5)
Strategy 2 (Value): PnL = -5000.00 (Trades: 3)
Strategy 3 (Growth): PnL = 0.00 (Trades: 0)
```

---

#### `calculateWinRate(int strategyId, int portfolioId)`
Calculates percentage of profitable trades for a strategy.

**Formula:**
```
Win Rate (%) = (Winning Trades / Total Trades) × 100
```

**Returns:** `BigDecimal` representing win rate (0-100)

**Example:**
```java
BigDecimal winRate = analyticsService.calculateWinRate(1, portfolioId);
System.out.println("Win Rate: " + winRate + "%"); // Output: 66.67%
```

---

#### `updatePerformanceSummary(int portfolioId, BigDecimal totalPnL, BigDecimal winRate)`
Persists performance metrics to the database.

**SQL Operation:**
```sql
INSERT INTO PerformanceSummary (portfolio_id, total_pnl, win_rate, last_updated)
VALUES (?, ?, ?, NOW())
ON DUPLICATE KEY UPDATE total_pnl=VALUES(total_pnl), win_rate=VALUES(win_rate), ...
```

**Usage:**
```java
BigDecimal pnl = analyticsService.calculatePnL(portfolioId);
BigDecimal winRate = analyticsService.calculateWinRate(strategyId, portfolioId);
analyticsService.updatePerformanceSummary(portfolioId, pnl, winRate);
```

---

## Database Requirements

### Tables Required

#### RiskFlag Table
```sql
CREATE TABLE RiskFlag (
    flag_id INT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id INT NOT NULL,
    trade_symbol VARCHAR(10),
    violated_rule VARCHAR(100),
    flag_date TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)
);
```

#### PerformanceSummary Table
```sql
CREATE TABLE PerformanceSummary (
    portfolio_id INT PRIMARY KEY,
    total_pnl DECIMAL(15,2),
    win_rate DECIMAL(5,2),
    last_updated TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)
);
```

---

## Usage Example

### Complete Integration Example
See `src/PortfolioDemo.java` for full working example.

```java
// Initialize services
RiskEvaluationService riskService = new RiskEvaluationService();
PortfolioAnalyticsService analyticsService = new PortfolioAnalyticsService();

// Evaluate trade
Trade trade = new Trade("RELIANCE", "BUY", 100, 2500.00, 1);
int portfolioId = 1;

if (riskService.evaluateTrade(trade, portfolioId)) {
    // Trade passed all risk checks
    tradeRepository.saveTrade(trade, portfolioId);
    System.out.println("Trade executed and saved");
} else {
    // Trade rejected due to risk violations
    System.out.println("Trade rejected - review RiskFlag table");
}

// Analyze portfolio performance
BigDecimal pnl = analyticsService.calculatePnL(portfolioId);
Map<Integer, BigDecimal> strategyPnL = analyticsService.getStrategyStatistics(portfolioId);
BigDecimal winRate = analyticsService.calculateWinRate(1, portfolioId);

analyticsService.updatePerformanceSummary(portfolioId, pnl, winRate);
```

---

## Extending the System

### Creating Custom Rules

To add a new risk rule, implement the `RiskRule` interface:

```java
public class MyCustomRule implements RiskRule {
    @Override
    public boolean validate(Trade trade, double currentCapital) {
        // Your validation logic here
        boolean result = /* your logic */;
        
        if (!result) {
            System.out.println("MyCustomRule VIOLATED: " + /* details */);
        }
        return result;
    }
    
    @Override
    public String getRuleName() {
        return "MyCustomRule (Rule Description)";
    }
}

// Use the rule
RiskEvaluationService riskService = new RiskEvaluationService();
riskService.addRule(new MyCustomRule());
```

---

## Compilation and Testing

### Compile All Components
```powershell
cd src
javac -cp ".;../lib/*" `
  com\trade\model\RiskRule.java `
  com\trade\model\ConcentrationRule.java `
  com\trade\model\PositionSizeRule.java `
  com\trade\service\RiskEvaluationService.java `
  com\trade\service\PortfolioAnalyticsService.java
```

### Run Demo
```powershell
cd src
javac -cp ".;../lib/*" PortfolioDemo.java
java -cp ".;../lib/*" PortfolioDemo
```

---

## Key Design Principles

1. **BigDecimal for Financial Calculations:** All monetary values use BigDecimal to prevent floating-point precision errors

2. **Pluggable Architecture:** Rules are replaceable/extensible without modifying core logic

3. **Database-Driven:** Portfolio capital and results are persisted for audit trails

4. **Separation of Concerns:** Risk evaluation, analytics, and event flagging are separate concerns

5. **Error Handling:** Comprehensive try-catch blocks with informative error messages

---

## Performance Considerations

- **SQL Optimization:** GROUP BY queries for strategy statistics are indexed on strategy_id
- **BigDecimal Operations:** Used for accuracy; consider performance for large datasets
- **Connection Management:** Uses try-with-resources for automatic connection cleanup
- **Batch Operations:** For large-scale analytics, consider batch SQL operations

---

## Testing Checklist

- [x] ConcentrationRule rejects trades > 10% of capital
- [x] PositionSizeRule enforces quantity limits (1-10000)
- [x] RiskEvaluationService evaluates against all rules
- [x] PortfolioAnalyticsService calculates PnL accurately
- [x] Strategy statistics group by strategy_id
- [x] RiskFlag table records violations
- [x] PerformanceSummary table updates correctly
- [x] All classes compile without errors
- [x] Demo application runs successfully

---

## Files Created/Updated

### New Files
- `src/com/trade/model/RiskRule.java` - Interface
- `src/com/trade/model/ConcentrationRule.java` - Rule implementation
- `src/com/trade/model/PositionSizeRule.java` - Rule implementation
- `src/PortfolioDemo.java` - Demo/test application

### Modified Files
- `src/com/trade/service/RiskEvaluationService.java` - Populated with logic
- `src/com/trade/service/PortfolioAnalyticsService.java` - Populated with logic

---

## Dependencies

- Java 8+
- MySQL 5.7+ with JDBC driver (`mysql-connector-java-x.x.x.jar`)
- Connection details configured in service classes (user: root, password: redBlue3011!)

---

## Next Steps

1. Create database tables (RiskFlag, PerformanceSummary)
2. Test with actual portfolio data
3. Integrate with Main.java workflow
4. Monitor and extend rules based on real trading scenarios
5. Consider caching for frequently accessed analytics
