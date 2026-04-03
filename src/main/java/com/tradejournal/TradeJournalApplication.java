package com.tradejournal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * TradeJournal Application — Main entry point
 *
 * Spring Boot application that runs the Portfolio Analytics and Trade Journal system.
 *
 * Entry point: java -cp ... com.tradejournal.TradeJournalApplication
 * Or with Maven: mvn spring-boot:run
 *
 * Runs on:
 *   http://localhost:8080/
 *
 * API Endpoints:
 *   POST   /api/trades          → Submit trade (risk evaluation + save)
 *   GET    /api/trades          → Get all trades
 *   GET    /api/analytics       → Get portfolio summary + strategy stats
 *   GET    /api/health          → Health check
 */
@SpringBootApplication(scanBasePackages = {
    "com.tradejournal",
    "com.trade"  // Scans existing model/service packages
})
public class TradeJournalApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeJournalApplication.class, args);
        System.out.println("\n");
        System.out.println("████████╗██████╗  █████╗ ██████╗ ███████╗███████╗ █████╗ ██╗     ██╗   ██╗██████╗ ███╗   ██╗ █████╗ ██╗");
        System.out.println("╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗██╔════╝██╔════╝██╔══██╗██║     ██║   ██║██╔══██╗████╗  ██║██╔══██╗██║");
        System.out.println("   ██║   ██████╔╝███████║██║  ██║█████╗  ███████╗███████║██║     ██║   ██║██████╔╝██╔██╗ ██║███████║██║");
        System.out.println("   ██║   ██╔══██╗██╔══██║██║  ██║██╔══╝  ╚════██║██╔══██║██║     ██║   ██║██╔══██╗██║╚██╗██║██╔══██║██║");
        System.out.println("   ██║   ██║  ██║██║  ██║██████╔╝███████╗███████║██║  ██║███████╗╚██████╔╝██║  ██║██║ ╚████║██║  ██║███████╗");
        System.out.println("   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ╚══════╝╚══════╝╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝");
        System.out.println("\n");
        System.out.println("✓ TradeJournal started successfully!");
        System.out.println("✓ API available at: http://localhost:8080/api");
        System.out.println("✓ Dashboard available at: http://localhost:8080/index.html");
        System.out.println("\n");
    }
}
