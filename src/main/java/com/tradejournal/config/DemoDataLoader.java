package com.tradejournal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DemoDataLoader - Temporarily disabled for debugging
 * Enable after confirming database connectivity
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n✓ DemoDataLoader disabled - checking database connectivity\n");
    }
}
