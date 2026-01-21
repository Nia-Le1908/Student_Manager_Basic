package util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/quanlydiem2?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8");
            config.setUsername("root");
            config.setPassword("YOUR_PASSWORD");
            
            // Tối ưu hóa hiệu năng
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.setMaximumPoolSize(10); // Giới hạn 10 kết nối cùng lúc
            
            dataSource = new HikariDataSource(config);
            System.out.println("✅ HikariCP Connection Pool started.");
        } catch (Exception e) {
            System.err.println("❌ Lỗi khởi tạo HikariCP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    // Đóng pool khi tắt ứng dụng (gọi ở MainApp.stop())
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
