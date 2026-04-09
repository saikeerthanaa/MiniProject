-- ========================================================
-- TRADEJOURNAL - DATABASE SCHEMA
-- ========================================================
-- This file is automatically executed by Spring Boot on startup
-- Location: src/main/resources/schema.sql
-- ========================================================

-- Create User table
CREATE TABLE IF NOT EXISTS "User" (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL
);

-- Create Strategy table
CREATE TABLE IF NOT EXISTS "Strategy" (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT
);

-- Create Portfolio table
CREATE TABLE IF NOT EXISTS "Portfolio" (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL,
  total_capital DECIMAL(15, 2) DEFAULT 100000.00,
  created_date DATE DEFAULT CURRENT_DATE,
  FOREIGN KEY(user_id) REFERENCES "User"(id)
);

-- Create Trade table
CREATE TABLE IF NOT EXISTS "Trade" (
  id SERIAL PRIMARY KEY,
  portfolio_id INT NOT NULL,
  stock_symbol VARCHAR(10) NOT NULL,
  trade_type VARCHAR(10) NOT NULL,
  quantity INT NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  trade_date DATE DEFAULT CURRENT_DATE,
  strategy_id INT,
  FOREIGN KEY(portfolio_id) REFERENCES "Portfolio"(id),
  FOREIGN KEY(strategy_id) REFERENCES "Strategy"(id)
);

-- Create RiskFlag table
CREATE TABLE IF NOT EXISTS "RiskFlag" (
  id SERIAL PRIMARY KEY,
  portfolio_id INT NOT NULL,
  rule_name VARCHAR(100),
  violation_details TEXT,
  flag_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(portfolio_id) REFERENCES "Portfolio"(id)
);

-- Create PerformanceSummary table
CREATE TABLE IF NOT EXISTS "PerformanceSummary" (
  id SERIAL PRIMARY KEY,
  portfolio_id INT NOT NULL,
  total_pnl DECIMAL(15, 2),
  win_rate DECIMAL(5, 2),
  total_trades INT,
  winning_trades INT,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(portfolio_id) REFERENCES "Portfolio"(id)
);
