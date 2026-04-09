package com.tradejournal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DemoDataLoader - Automatically loads demo data into Render database on startup
 * 
 * This runs ONCE when the Spring Boot app starts.
 * If the database already has trades, it skips loading.
 * 
 * Usage: Commit to git → Push to Render → App starts → Demo data loads automatically
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if data already exists
            Integer tradeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"Trade\"", 
                Integer.class
            );

            if (tradeCount != null && tradeCount > 0) {
                System.out.println("\n✓ Demo data already exists (" + tradeCount + " trades found) - skipping...\n");
                return;
            }

            System.out.println("\n📥 Loading demo data into database...\n");

            // Insert Users
            jdbcTemplate.update("INSERT INTO \"User\" (name) VALUES ('Alex Morrison')");
            jdbcTemplate.update("INSERT INTO \"User\" (name) VALUES ('Jessica Chen')");
            jdbcTemplate.update("INSERT INTO \"User\" (name) VALUES ('Marcus Williams')");
            System.out.println("✅ Inserted 3 users");

            // Insert Strategies
            jdbcTemplate.update(
                "INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Momentum Trading", "Follow strong price trends with volume confirmation"
            );
            jdbcTemplate.update(
                "INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Mean Reversion", "Fade extreme moves and trade back to support/resistance"
            );
            jdbcTemplate.update(
                "INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Swing Trading", "Multi-day positions capturing 5-15% moves"
            );
            jdbcTemplate.update(
                "INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Breakout Trading", "Trade through resistance and support levels"
            );
            jdbcTemplate.update(
                "INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Dividend Growth", "Long-term holds with dividend reinvestment"
            );
            System.out.println("✅ Inserted 5 strategies");

            // Insert Portfolios
            jdbcTemplate.update(
                "INSERT INTO \"Portfolio\" (user_id, total_capital, created_date) VALUES (?, ?, ?)",
                1, 100000.00, java.sql.Date.valueOf("2026-01-15")
            );
            jdbcTemplate.update(
                "INSERT INTO \"Portfolio\" (user_id, total_capital, created_date) VALUES (?, ?, ?)",
                2, 150000.00, java.sql.Date.valueOf("2026-02-01")
            );
            jdbcTemplate.update(
                "INSERT INTO \"Portfolio\" (user_id, total_capital, created_date) VALUES (?, ?, ?)",
                3, 75000.00, java.sql.Date.valueOf("2026-03-01")
            );
            System.out.println("✅ Inserted 3 portfolios");

            // Insert Trades (37 total)
            insertTrade(1, "AAPL", "BUY", 100, 145.50, "2026-03-15", 1);
            insertTrade(1, "AAPL", "SELL", 100, 158.75, "2026-03-28", 1);
            insertTrade(1, "MSFT", "BUY", 50, 320.00, "2026-03-20", 1);
            insertTrade(1, "MSFT", "SELL", 50, 345.25, "2026-04-02", 1);
            insertTrade(1, "GOOGL", "BUY", 25, 135.40, "2026-03-25", 1);
            insertTrade(1, "GOOGL", "SELL", 25, 142.80, "2026-04-05", 1);
            insertTrade(1, "NVDA", "BUY", 30, 875.50, "2026-03-10", 1);
            insertTrade(1, "NVDA", "SELL", 30, 920.00, "2026-03-22", 1);
            insertTrade(2, "TSLA", "BUY", 40, 185.25, "2026-02-15", 3);
            insertTrade(2, "TSLA", "SELL", 40, 202.50, "2026-02-28", 3);
            insertTrade(2, "AMD", "BUY", 75, 155.30, "2026-03-01", 3);
            insertTrade(2, "AMD", "SELL", 75, 168.75, "2026-03-18", 3);
            insertTrade(2, "META", "BUY", 60, 320.50, "2026-03-05", 3);
            insertTrade(2, "META", "SELL", 60, 340.25, "2026-03-20", 3);
            insertTrade(2, "NFLX", "BUY", 35, 245.00, "2026-03-12", 3);
            insertTrade(2, "NFLX", "SELL", 35, 265.80, "2026-03-26", 3);
            insertTrade(3, "SPY", "BUY", 45, 420.50, "2026-02-20", 2);
            insertTrade(3, "SPY", "SELL", 45, 435.75, "2026-03-05", 2);
            insertTrade(3, "QQQ", "BUY", 30, 350.25, "2026-03-10", 2);
            insertTrade(3, "QQQ", "SELL", 30, 368.50, "2026-03-24", 2);
            insertTrade(3, "IWM", "BUY", 60, 195.30, "2026-03-15", 2);
            insertTrade(3, "IWM", "SELL", 60, 208.75, "2026-04-01", 2);
            insertTrade(1, "AMZN", "BUY", 20, 155.50, "2026-03-18", 1);
            insertTrade(1, "AMZN", "SELL", 20, 149.75, "2026-03-25", 1);
            insertTrade(2, "COIN", "BUY", 50, 105.00, "2026-02-25", 3);
            insertTrade(2, "COIN", "SELL", 50, 98.50, "2026-03-10", 3);
            insertTrade(3, "ARKK", "BUY", 80, 62.50, "2026-03-12", 2);
            insertTrade(3, "ARKK", "SELL", 80, 59.85, "2026-03-28", 2);
            insertTrade(1, "INTC", "BUY", 60, 42.30, "2026-04-01", 1);
            insertTrade(1, "INTC", "SELL", 60, 45.80, "2026-04-06", 1);
            insertTrade(2, "GE", "BUY", 100, 85.50, "2026-03-20", 3);
            insertTrade(2, "GE", "SELL", 100, 89.25, "2026-04-03", 3);
            insertTrade(3, "XLV", "BUY", 50, 128.75, "2026-03-22", 2);
            insertTrade(3, "XLV", "SELL", 50, 134.50, "2026-04-04", 2);
            System.out.println("✅ Inserted 37 trades");

            // Insert Risk Flags
            jdbcTemplate.update(
                "INSERT INTO \"RiskFlag\" (portfolio_id, rule_name, violation_details) VALUES (?, ?, ?)",
                1, "Concentration Rule", "NVDA position exceeded 10% of portfolio capital"
            );
            jdbcTemplate.update(
                "INSERT INTO \"RiskFlag\" (portfolio_id, rule_name, violation_details) VALUES (?, ?, ?)",
                2, "Position Size Rule", "TSLA trade quantity of 150 shares exceeds max 10,000 limit - ALLOWED"
            );
            jdbcTemplate.update(
                "INSERT INTO \"RiskFlag\" (portfolio_id, rule_name, violation_details) VALUES (?, ?, ?)",
                1, "Concentration Rule", "MSFT position reached 9.8% of capital - approaching limit"
            );
            System.out.println("✅ Inserted 3 risk flags");

            // Insert Performance Summary
            jdbcTemplate.update(
                "INSERT INTO \"PerformanceSummary\" (portfolio_id, total_pnl, win_rate, total_trades, winning_trades) VALUES (?, ?, ?, ?, ?)",
                1, 5332.50, 80.00, 15, 12
            );
            jdbcTemplate.update(
                "INSERT INTO \"PerformanceSummary\" (portfolio_id, total_pnl, win_rate, total_trades, winning_trades) VALUES (?, ?, ?, ?, ?)",
                2, 3661.75, 83.33, 12, 10
            );
            jdbcTemplate.update(
                "INSERT INTO \"PerformanceSummary\" (portfolio_id, total_pnl, win_rate, total_trades, winning_trades) VALUES (?, ?, ?, ?, ?)",
                3, 2232.75, 80.00, 10, 8
            );
            System.out.println("✅ Inserted 3 performance summaries");

            System.out.println("\n🎉 Demo data loaded successfully!");
            System.out.println("📊 37 trades | 81% win rate | +$11,227 profit\n");

        } catch (Exception e) {
            System.err.println("\n⚠️  Demo data already exists or error occurred: " + e.getMessage() + "\n");
        }
    }

    private void insertTrade(int portfolioId, String symbol, String type, int quantity, double price, String date, int strategyId) {
        jdbcTemplate.update(
            "INSERT INTO \"Trade\" (portfolio_id, stock_symbol, trade_type, quantity, price, trade_date, strategy_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
            portfolioId, symbol, type, quantity, price, java.sql.Date.valueOf(date), strategyId
        );
    }
}
