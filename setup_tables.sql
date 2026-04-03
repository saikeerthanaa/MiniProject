-- Create RiskFlag table for recording risk violations
CREATE TABLE IF NOT EXISTS RiskFlag (
    flag_id INT PRIMARY KEY AUTO_INCREMENT,
    portfolio_id INT NOT NULL,
    trade_symbol VARCHAR(10),
    violated_rule VARCHAR(100),
    flag_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)
);

-- Create PerformanceSummary table for storing portfolio analytics
CREATE TABLE IF NOT EXISTS PerformanceSummary (
    portfolio_id INT PRIMARY KEY,
    total_pnl DECIMAL(15,2),
    win_rate DECIMAL(5,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)
);

-- Verify tables created
SHOW TABLES LIKE 'RiskFlag';
SHOW TABLES LIKE 'PerformanceSummary';
