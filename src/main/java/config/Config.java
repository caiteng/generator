package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置
 *
 * @author cait
 */
public class Config {
    public static String DB_URL;// 数据库路径
    public static String DRIVER;// 数据库驱动
    public static String USERNAME;// 数据库用户名
    public static String PASSWORD;// 数据库密码
    public static String PATH;// 文件输出路径

    /*
     * 从配置文件获取默认配置
     */
    static {
        Properties p = new Properties();
        InputStream inputStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
        try {
            p.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DB_URL = p.getProperty("url");
        DRIVER = p.getProperty("driver");
        USERNAME = p.getProperty("username");
        PASSWORD = p.getProperty("password");
        PATH = p.getProperty("path");
    }

}
