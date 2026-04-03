import java.sql.*;

/**
 * Utility to initialize database schema for the Portfolio Analytics system
 */
public class DatabaseSetup {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/TradeJournal";
        String user = "root";
        String pass = "redBlue3011!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to TradeJournal database...");
            
            // Create RiskFlag table
            String createRiskFlag = "CREATE TABLE IF NOT EXISTS RiskFlag (" +
                "flag_id INT PRIMARY KEY AUTO_INCREMENT," +
                "portfolio_id INT NOT NULL," +
                "trade_symbol VARCHAR(10)," +
                "violated_rule VARCHAR(100)," +
                "flag_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)" +
                ")";
            
            // Create PerformanceSummary table
            String createPerformanceSummary = "CREATE TABLE IF NOT EXISTS PerformanceSummary (" +
                "portfolio_id INT PRIMARY KEY," +
                "total_pnl DECIMAL(15,2)," +
                "win_rate DECIMAL(5,2)," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)" +
                ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createRiskFlag);
                System.out.println("✓ RiskFlag table created successfully");
                
                stmt.execute(createPerformanceSummary);
                System.out.println("✓ PerformanceSummary table created successfully");
                
                // Verify tables exist
                DatabaseMetaData dbMeta = conn.getMetaData();
                ResultSet tables = dbMeta.getTables(null, null, "RiskFlag", null);
                if (tables.next()) {
                    System.out.println("✓ RiskFlag table verified in database");
                }
                
                tables = dbMeta.getTables(null, null, "PerformanceSummary", null);
                if (tables.next()) {
                    System.out.println("✓ PerformanceSummary table verified in database");
                }
                
                System.out.println("\n✓ Database setup completed successfully!");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error setting up database:");
            e.printStackTrace();
        }
    }
}
