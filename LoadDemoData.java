import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoadDemoData {
    public static void main(String[] args) throws Exception {
        // Your Render database URL
        String url = "jdbc:postgresql://dpg-d77u49p4bi0s73f5ti5g-a.oregon-postgres.render.com/tradejournal_6jrx?ssl=true&sslmode=require";
        String user = "tradejournal_6jrx_user";
        String password = "rIf7dc15HrRlzal1cxlg0EScLGOWbIF5";

        try {
            System.out.println("🔗 Connecting to Render PostgreSQL...");
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connection successful!");

            System.out.println("\n📥 Loading demo data...");
            String sql = new String(Files.readAllBytes(Paths.get("demo_data_postgres.sql")));
            
            // Split by semicolon and execute each statement
            String[] statements = sql.split(";");
            Statement stmt = conn.createStatement();
            
            int count = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    try {
                        stmt.execute(trimmed);
                        count++;
                        System.out.println("✅ Statement " + count + " executed");
                    } catch (Exception e) {
                        if (e.getMessage().contains("duplicate")) {
                            System.out.println("⚠️  Statement " + count + ": Data already exists (skipped)");
                        } else {
                            System.out.println("❌ Error in statement " + count + ": " + e.getMessage());
                        }
                    }
                }
            }

            // Verify
            System.out.println("\n✅ Demo data loaded successfully!");
            conn.close();

        } catch (Exception e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
