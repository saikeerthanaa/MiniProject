import java.sql.*;

public class SchemaInspector {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/TradeJournal";
        String user = "root";
        String pass = "redBlue3011!";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            DatabaseMetaData dbMeta = conn.getMetaData();
            
            System.out.println("=== Portfolio Table Columns ===");
            ResultSet columns = dbMeta.getColumns(null, null, "Portfolio", null);
            while (columns.next()) {
                System.out.println("  - " + columns.getString("COLUMN_NAME") + 
                    " (" + columns.getString("TYPE_NAME") + ")");
            }
            
            System.out.println("\n=== Portfolio Table Data ===");
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM Portfolio LIMIT 5");
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                
                for (int i = 1; i <= colCount; i++) {
                    System.out.print(meta.getColumnName(i) + "\t");
                }
                System.out.println();
                
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        System.out.print(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
            }
        }
    }
}
