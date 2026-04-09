package com.tradejournal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DemoDataLoader - Loads demo data on startup if database is empty
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        if (jdbcTemplate == null) {
            System.out.println("\n⚠️  JdbcTemplate not available - skipping demo data\n");
            return;
        }

        try {
            // Check if data already exists
            Integer tradeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"Trade\"", 
                Integer.class
            );

            if (tradeCount != null && tradeCount > 0) {
                System.out.println("\n✓ Trades already exist (" + tradeCount + " found) - skipping demo data\n");
                return;
            }

            System.out.println("\n📥 Loading demo data...\n");

            // Insert sample strategies
            jdbcTemplate.update("INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Momentum Trading", "Follow strong price trends");
            jdbcTemplate.update("INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Mean Reversion", "Fade extreme moves");
            jdbcTemplate.update("INSERT INTO \"Strategy\" (name, description) VALUES (?, ?)",
                "Swing Trading", "Multi-day positions");
            System.out.println("✅ Strategies created");

            System.out.println("✓ Database initialized\n");

        } catch (Exception e) {
            System.out.println("⚠️  Database initialization: " + e.getMessage() + "\n");
        }
    }
}
