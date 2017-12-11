package database;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cait
 */
public class Link {

    static String sql = null;
    static DBHelper db1 = null;
    static ResultSet ret = null;

    /**
     * 获取表名
     */
    public static List<String> getTableName(String databaseName, String tableName) throws Exception {
        sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + databaseName + "' AND TABLE_NAME LIKE '%" + tableName + "%'";
        db1 = new DBHelper(sql);
        List<String> list = new ArrayList<String>();
        ret = db1.pst.executeQuery();
        String str;
        while (ret.next()) {
            str = ret.getString(1);
            list.add(str);
        }
        ret.close();
        db1.close();
        return list;
    }
}