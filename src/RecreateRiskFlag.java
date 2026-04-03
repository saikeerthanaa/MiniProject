import java.sql.*;

public class RecreateRiskFlag {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/TradeJournal";
        String user = "root";
        String pass = "redBlue3011!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (Statement stmt = conn.createStatement()) {
                // Drop if exists
                stmt.execute("DROP TABLE IF EXISTS RiskFlag");
                System.out.println("✓ Dropped existing RiskFlag table");
                
                // Create new
                stmt.execute(
                    "CREATE TABLE RiskFlag (" +
                    "flag_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "portfolio_id INT NOT NULL," +
                    "trade_symbol VARCHAR(10)," +
                    "violated_rule VARCHAR(100)," +
                    "flag_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (portfolio_id) REFERENCES Portfolio(portfolio_id)" +
                    ")"
                );
                System.out.println("✓ RiskFlag table created successfully");
                
                // Verify
                DatabaseMetaData dbMeta = conn.getMetaData();
                ResultSet tables = dbMeta.getTables(null, null, "RiskFlag", null);
                if (tables.next()) {
                    System.out.println("✓ RiskFlag table verified");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
