-- ========================================================
-- TRADEJOURNAL - DEMO DATA FOR PRESENTATION
-- ========================================================
-- This script populates the database with realistic sample data
-- to demonstrate the system functionality during presentation.
-- Run after: setup_tables.sql
-- ========================================================

USE TradeJournal;

-- SECTION 1: INSERT USERS
-- ========================================================
INSERT INTO User (name) VALUES 
  ('Alex Morrison'),
  ('Jessica Chen'),
  ('Marcus Williams');

-- SECTION 2: INSERT STRATEGIES
-- ========================================================
INSERT INTO Strategy (name, description) VALUES 
  ('Momentum Trading', 'Follow strong price trends with volume confirmation'),
  ('Mean Reversion', 'Fade extreme moves and trade back to support/resistance'),
  ('Swing Trading', 'Multi-day positions capturing 5-15% moves'),
  ('Breakout Trading', 'Trade through resistance and support levels'),
  ('Dividend Growth', 'Long-term holds with dividend reinvestment');

-- SECTION 3: INSERT PORTFOLIOS
-- ========================================================
INSERT INTO Portfolio (user_id, total_capital, created_date) VALUES 
  (1, 100000.00, '2026-01-15'),
  (2, 150000.00, '2026-02-01'),
  (3, 75000.00, '2026-03-01');

-- SECTION 4: INSERT PROFITABLE TRADES
-- ========================================================
-- Portfolio 1 - Momentum trades (Alex Morrison)
INSERT INTO Trade (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES 
  (1, 'AAPL', 'BUY', 100, 145.50, '2026-03-15', 1),
  (1, 'AAPL', 'SELL', 100, 158.75, '2026-03-28', 1),  -- +$1,325 profit
  (1, 'MSFT', 'BUY', 50, 320.00, '2026-03-20', 1),
  (1, 'MSFT', 'SELL', 50, 345.25, '2026-04-02', 1),   -- +$1,262.50 profit
  (1, 'GOOGL', 'BUY', 25, 135.40, '2026-03-25', 1),
  (1, 'GOOGL', 'SELL', 25, 142.80, '2026-04-05', 1),  -- +$185 profit
  (1, 'NVDA', 'BUY', 30, 875.50, '2026-03-10', 1),
  (1, 'NVDA', 'SELL', 30, 920.00, '2026-03-22', 1);   -- +$1,335 profit

-- Portfolio 2 - Swing Trading (Jessica Chen)
INSERT INTO Trade (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES 
  (2, 'TSLA', 'BUY', 40, 185.25, '2026-02-15', 3),
  (2, 'TSLA', 'SELL', 40, 202.50, '2026-02-28', 3),   -- +$690 profit
  (2, 'AMD', 'BUY', 75, 155.30, '2026-03-01', 3),
  (2, 'AMD', 'SELL', 75, 168.75, '2026-03-18', 3),    -- +$1,008.75 profit
  (2, 'META', 'BUY', 60, 320.50, '2026-03-05', 3),
  (2, 'META', 'SELL', 60, 340.25, '2026-03-20', 3),   -- +$1,185 profit
  (2, 'NFLX', 'BUY', 35, 245.00, '2026-03-12', 3),
  (2, 'NFLX', 'SELL', 35, 265.80, '2026-03-26', 3);   -- +$728 profit

-- Portfolio 3 - Mean Reversion (Marcus Williams)
INSERT INTO Trade (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES 
  (3, 'SPY', 'BUY', 45, 420.50, '2026-02-20', 2),
  (3, 'SPY', 'SELL', 45, 435.75, '2026-03-05', 2),    -- +$687.75 profit
  (3, 'QQQ', 'BUY', 30, 350.25, '2026-03-10', 2),
  (3, 'QQQ', 'SELL', 30, 368.50, '2026-03-24', 2),    -- +$547.50 profit
  (3, 'IWM', 'BUY', 60, 195.30, '2026-03-15', 2),
  (3, 'IWM', 'SELL', 60, 208.75, '2026-04-01', 2);    -- +$810 profit

-- SECTION 5: INSERT SOME LOSING POSITIONS (for realism)
-- ========================================================
INSERT INTO Trade (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES 
  (1, 'AMZN', 'BUY', 20, 155.50, '2026-03-18', 1),
  (1, 'AMZN', 'SELL', 20, 149.75, '2026-03-25', 1),   -- -$115 loss
  (2, 'COIN', 'BUY', 50, 105.00, '2026-02-25', 3),
  (2, 'COIN', 'SELL', 50, 98.50, '2026-03-10', 3),    -- -$325 loss
  (3, 'ARKK', 'BUY', 80, 62.50, '2026-03-12', 2),
  (3, 'ARKK', 'SELL', 80, 59.85, '2026-03-28', 2);    -- -$212 loss

-- Additional winning trades to show activity
INSERT INTO Trade (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES 
  (1, 'INTC', 'BUY', 60, 42.30, '2026-04-01', 1),
  (1, 'INTC', 'SELL', 60, 45.80, '2026-04-06', 1),    -- +$210 profit
  (2, 'GE', 'BUY', 100, 85.50, '2026-03-20', 3),
  (2, 'GE', 'SELL', 100, 89.25, '2026-04-03', 3),     -- +$375 profit
  (3, 'XLV', 'BUY', 50, 128.75, '2026-03-22', 2),
  (3, 'XLV', 'SELL', 50, 134.50, '2026-04-04', 2);    -- +$287.50 profit

-- SECTION 6: INSERT RISK FLAGS (Risk Violations)
-- ========================================================
-- Show a few concentration rule violations and position size violations
INSERT INTO RiskFlag (portfolio_id, rule_name, violation_details, timestamp) VALUES 
  (1, 'Concentration Rule', 'NVDA position exceeded 10% of portfolio capital', '2026-04-02 10:30:00'),
  (2, 'Position Size Rule', 'TSLA trade quantity of 150 shares exceeds max 10,000 limit - ALLOWED', '2026-03-15 14:15:00'),
  (1, 'Concentration Rule', 'MSFT position reached 9.8% of capital - approaching limit', '2026-03-25 09:45:00');

-- SECTION 7: INSERT PERFORMANCE SUMMARY
-- ========================================================
-- Portfolio 1 Analytics (Alex Morrison - 15 trades, 12 winners)
INSERT INTO PerformanceSummary (portfolio_id, total_pnl, win_rate, total_trades, winning_trades, last_updated) VALUES 
  (1, 5332.50, 80.00, 15, 12, '2026-04-07 16:30:00');

-- Portfolio 2 Analytics (Jessica Chen - 12 trades, 10 winners)
INSERT INTO PerformanceSummary (portfolio_id, total_pnl, win_rate, total_trades, winning_trades, last_updated) VALUES 
  (2, 3661.75, 83.33, 12, 10, '2026-04-07 16:30:00');

-- Portfolio 3 Analytics (Marcus Williams - 10 trades, 8 winners)
INSERT INTO PerformanceSummary (portfolio_id, total_pnl, win_rate, total_trades, winning_trades, last_updated) VALUES 
  (3, 2232.75, 80.00, 10, 8, '2026-04-07 16:30:00');

-- ========================================================
-- VERIFICATION QUERIES
-- ========================================================
-- Verify all data inserted correctly:

SELECT 'USERS' as data_type, COUNT(*) as count FROM User
UNION ALL
SELECT 'STRATEGIES', COUNT(*) FROM Strategy
UNION ALL
SELECT 'PORTFOLIOS', COUNT(*) FROM Portfolio
UNION ALL
SELECT 'TRADES', COUNT(*) FROM Trade
UNION ALL
SELECT 'RISK FLAGS', COUNT(*) FROM RiskFlag
UNION ALL
SELECT 'PERFORMANCE DATA', COUNT(*) FROM PerformanceSummary;

-- Show portfolio summary
SELECT 
  p.id as portfolio_id,
  u.name as trader_name,
  p.total_capital,
  ps.total_pnl as profit_loss,
  ps.win_rate,
  ps.total_trades,
  ps.winning_trades
FROM Portfolio p
LEFT JOIN User u ON p.user_id = u.id
LEFT JOIN PerformanceSummary ps ON p.id = ps.portfolio_id
ORDER BY p.id;
