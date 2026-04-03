import java.sql.*;

public class VerifyData {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/TradeJournal";
        String user = "root";
        String pass = "redBlue3011!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            // Check RiskFlag table
            System.out.println("=== RiskFlag Records ===");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM RiskFlag");
                if (rs.next()) {
                    System.out.println("Total violations recorded: " + rs.getInt("cnt"));
                }
                
                rs = stmt.executeQuery("SELECT * FROM RiskFlag LIMIT 10");
                while (rs.next()) {
                    System.out.println("  Flag #" + rs.getInt("flag_id") + 
                        " | Portfolio: " + rs.getInt("portfolio_id") +
                        " | Symbol: " + rs.getString("trade_symbol") +
                        " | Rule: " + rs.getString("violated_rule"));
                }
            }
            
            // Check PerformanceSummary table
            System.out.println("\n=== PerformanceSummary Records ===");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM PerformanceSummary");
                while (rs.next()) {
                    System.out.println("  Portfolio: " + rs.getInt("portfolio_id") +
                        " | PnL: " + rs.getBigDecimal("total_pnl") +
                        " | Win Rate: " + rs.getBigDecimal("win_rate") + "%");
                }
            }
            
            System.out.println("\n✓ Database verification complete!");
        }
    }
}
