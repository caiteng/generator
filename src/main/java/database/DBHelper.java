package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import config.Config;
/**
 * CREATED_BY caiT
 */
public class DBHelper {

    public Connection conn = null;
    public PreparedStatement pst = null;

    public DBHelper(String sql) throws Exception {
        Class.forName(Config.DRIVER);
        conn = DriverManager.getConnection(Config.DB_URL, Config.USERNAME, Config.PASSWORD);
        // 准备执行语句
        pst = conn.prepareStatement(sql);
    }

    public void close() {
        try {
            this.conn.close();
            this.pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}