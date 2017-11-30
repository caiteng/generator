package Util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

/**
 * CREATED_BY caiT
 */
public class DbUtils {

	private String DRIVER;
	private String URL;
	private String USERNAME;
	private String PASSWOED;

	public DbUtils(String driver, String url, String userName, String password) {
		DRIVER = driver;
		URL = url;
		USERNAME = userName;
		PASSWOED = password;
	}

	public void writeProperties(String file) {
		if (StringUtils.isBlank(file))
			return;
		try {
			File f = new File(file);
			if (!f.exists())
				f.createNewFile();
			Properties prop = new Properties();
			InputStream is = new FileInputStream(f);
			prop.load(is);
			is.close();
			OutputStream fos = new FileOutputStream(f);
			prop.setProperty("jdbc.driverClassName", DRIVER);
			prop.setProperty("jdbc.url", URL);
			prop.setProperty("jdbc.username", USERNAME);
			prop.setProperty("jdbc.password", PASSWOED);
			prop.store(fos, null);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean checkConnection() throws ClassNotFoundException, SQLException {
		Connection con = null;
		Class.forName(DRIVER);
		con = DriverManager.getConnection(URL, USERNAME, PASSWOED);
		if (con != null) {
			try {
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public List getTables() throws ClassNotFoundException, SQLException {
		Connection con;
		Statement stmt;
		con = null;
		stmt = null;
		List list;
		Class.forName(DRIVER);
		con = DriverManager.getConnection(URL, USERNAME, PASSWOED);
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("show tables");
		List tableNames = new ArrayList();
		for (; rs.next(); tableNames.add(rs.getString(1)))
			;
		list = tableNames;
		try {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList();
		}
		return list;
	}

	public LinkedHashMap[] getTableInfo(String tableName) throws SQLException, ClassNotFoundException {
		Connection con;
		Statement stmt;
		LinkedHashMap result[];
		con = null;
		stmt = null;
		result = new LinkedHashMap[2];
		LinkedHashMap alinkedhashmap[];
		Class.forName(DRIVER);
		con = DriverManager.getConnection(URL, USERNAME, PASSWOED);
		stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery((new StringBuilder("show full columns from ")).append(tableName).toString());
		LinkedHashMap fields = new LinkedHashMap();
		LinkedHashMap comments = new LinkedHashMap();
		List prilist = new ArrayList();
		for (; rs.next(); comments.put(rs.getString("Field"), rs.getString("Comment"))) {
			if (StringUtils.isBlank((String) fields.get("PrimaryKey")) && "PRI".equals(rs.getString("Key")))
				prilist.add(rs.getString("Field"));
			fields.put(rs.getString("Field"), rs.getString("Type"));
		}

		if (prilist.size() > 0)
			fields.put("PrimaryKey", (String) prilist.get(0));
		result[0] = fields;
		result[1] = comments;
		alinkedhashmap = result;
		try {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return alinkedhashmap;
	}

	public void addSystemMenu(String className) throws SQLException, ClassNotFoundException {
		String menuSql;
		Connection con;
		Statement stmt;
		menuSql = (new StringBuilder("INSERT INTO `sys_common_menu` (`text`,`iconCls`,`url`,`iframeId`,`moduleName`,`parentId`,`status`,`type`,`group_id`,`remark`,`listorder`) VALUES ('"))
				.append(className).append("Menu','").append(className).append("','/index.php/system/").append(className).append("/List").append(className).append("','','SYS',0,1,0,0,'',99);")
				.toString();
		con = null;
		stmt = null;
		ResultSet check;
		Class.forName(DRIVER);
		con = DriverManager.getConnection(URL, USERNAME, PASSWOED);
		stmt = con.createStatement();
		check = stmt
				.executeQuery((new StringBuilder("select count(*) from sys_common_menu where text = '")).append(className).append("Menu' and iconCls = '").append(className).append("'").toString());
		while (check.next())
			if (check.getInt(1) > 0) {
				try {
					if (stmt != null)
						stmt.close();
					if (con != null)
						con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
		stmt.executeUpdate(menuSql, 1);
		ResultSet rs = stmt.getGeneratedKeys();
		String menuId;
		for (menuId = ""; rs.next(); menuId = rs.getString(1))
			;
		String roleMenuSql = (new StringBuilder("INSERT INTO `sys_common_role_menu` (`roleId`,`menuId`) VALUES (1,")).append(menuId).append(");").toString();
		stmt.executeUpdate(roleMenuSql);

		try {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
			if (con != null)
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getDRIVER() {
		return DRIVER;
	}

	public void setDRIVER(String dRIVER) {
		DRIVER = dRIVER;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getUSERNAME() {
		return USERNAME;
	}

	public void setUSERNAME(String uSERNAME) {
		USERNAME = uSERNAME;
	}

	public String getPASSWOED() {
		return PASSWOED;
	}

	public void setPASSWOED(String pASSWOED) {
		PASSWOED = pASSWOED;
	}
}