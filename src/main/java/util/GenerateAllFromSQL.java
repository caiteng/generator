package util;

import config.Config;
import io.IoFile;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * @author cait
 */
public class GenerateAllFromSQL {
    private static boolean isRelTable = false;

    public GenerateAllFromSQL(List<String> list) throws ClassNotFoundException, SQLException {

        Map<String, String> map = GenerateAllFromSQL.doExecute(new DbUtils(Config.DRIVER, Config.DB_URL, Config.USERNAME, Config.PASSWORD), list, Config.PATH, Config.PATH,
                null, true, true);
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            /**
             * 生成文件
             */
            IoFile.createFile(key, map.get(key));
        }

    }

    public static Map<String, String> doExecute(DbUtils dbUtils, List<String> tables, String javaPath, String batisPath, String phpPath, boolean isGeneMfhPHP, boolean advancedPath)
            throws ClassNotFoundException, SQLException {
        Map<String, String> resMap = new HashMap<String, String>();
        for (Iterator<String> iterator = tables.iterator(); iterator.hasNext(); ) {
            String table = (String) iterator.next();
            isRelTable = false;
            String className = getClassName(table);
            LinkedHashMap result[] = dbUtils.getTableInfo(table);
            if (result[1].size() == 2)
                isRelTable = true;
            generateMybatisFile(resMap, (new StringBuilder(String.valueOf(batisPath))).append(File.separator).append("mybatis").toString(), table, className, result[0]);
            generatePojoFile(resMap, (new StringBuilder(String.valueOf(javaPath))).append(File.separator).append("entity").toString(), table, className, result[0], result[1]);
            generateDaoFile(resMap, (new StringBuilder(String.valueOf(javaPath))).append(File.separator).append("dao").toString(), table, className);
            generateServiceFile(resMap, (new StringBuilder(String.valueOf(javaPath))).append(File.separator).append("service").toString(), table, className, result[0]);
            if (!isRelTable)
                generateControllerFile(resMap, (new StringBuilder(String.valueOf(javaPath))).append(File.separator).append("controller").toString(), table, className);
        }
        return resMap;
    }

    private static String camelCase(String str) {
        String ns = StringUtils.lowerCase(str);
        return StringUtils.capitalize(ns);
    }

    private static String getClassName(String tableName) {
        String strs[] = tableName.split("_");
        String className = "";
        for (int i = 0; i < strs.length; i++)
            if (i != 0)
                className = (new StringBuilder(String.valueOf(className))).append(camelCase(strs[i])).toString();

        return className;
    }

    private static String getJavaType(String type) {
        String lowcaseType = type.toLowerCase();
        if (lowcaseType.contains("bigint"))
            return "Long";
        if (lowcaseType.contains("int"))
            return "Integer";
        if (lowcaseType.contains("double") || lowcaseType.contains("float"))
            return "Double";
        if (lowcaseType.contains("char") || lowcaseType.contains("text"))
            return "String";
        if (lowcaseType.contains("date") || lowcaseType.contains("timestamp") || lowcaseType.contains("datetime"))
            return "Date";
        if (lowcaseType.contains("boolean") || lowcaseType.contains("tinyint"))
            return "Boolean";
        else
            return "String";
    }

    private static void generateMybatisFile(Map resMap, String filePath, String tableName, String className, Map result) {
        String type1 = "";
        String type2 = "";
        String type3 = "";
        String type4 = "";
        String type5 = "";
        String type6 = "";
        String type7 = "";
        Set keys = result.keySet();
        String primaryKey = "";
        int n = 0;
        boolean innerDel = false;
        boolean relatif = isRelTable;
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            if ("del_flag".equals(key))
                innerDel = true;
            if ("primarykey".equals(key.toLowerCase()) || "updated_date".equals(key.toLowerCase()) || "updated_by".equals(key.toLowerCase())) {
                if (n == keys.size() - 1) {
                    type1 = StringUtils.removeEnd(type1, ",");
                    type2 = StringUtils.removeEnd(type2, ",");
                    type4 = StringUtils.removeEnd(type4, ",");
                }
                if ("primarykey".equals(key.toLowerCase()))
                    primaryKey = (String) result.get("PrimaryKey");
                n++;
            } else if ("created_by".equals(key.toLowerCase())) {
                if (n == keys.size() - 1) {
                    type1 = (new StringBuilder(String.valueOf(type1))).append("CREATED_BY").toString();
                    type2 = (new StringBuilder(String.valueOf(type2))).append("#{createdBy}").toString();
                    type4 = (new StringBuilder(String.valueOf(type4))).append("CREATED_BY").toString();
                } else {
                    type1 = (new StringBuilder(String.valueOf(type1))).append("CREATED_BY,").toString();
                    type2 = (new StringBuilder(String.valueOf(type2))).append("#{createdBy},").toString();
                    type4 = (new StringBuilder(String.valueOf(type4))).append("CREATED_BY,").toString();
                }
                n++;
            } else if ("created_date".equals(key.toLowerCase())) {
                if (n == keys.size() - 1) {
                    type1 = (new StringBuilder(String.valueOf(type1))).append("CREATED_DATE").toString();
                    type2 = (new StringBuilder(String.valueOf(type2))).append("#{createdDate}").toString();
                    type4 = (new StringBuilder(String.valueOf(type4))).append("CREATED_DATE").toString();
                } else {
                    type1 = (new StringBuilder(String.valueOf(type1))).append("CREATED_DATE,").toString();
                    type2 = (new StringBuilder(String.valueOf(type2))).append("#{createdDate},").toString();
                    type4 = (new StringBuilder(String.valueOf(type4))).append("CREATED_DATE,").toString();
                }
                n++;
            } else {
                if (n == keys.size() - 1) {
                    type1 = (new StringBuilder(String.valueOf(type1))).append(key).toString();
                    type2 = (new StringBuilder(String.valueOf(type2))).append("#{").append(key).append("}").toString();
                    if (!primaryKey.equals(key) || relatif) {
                        type3 = (new StringBuilder(String.valueOf(type3))).append("\t\t<if test=\"").append(key).append("!=null and ").append(key).append("!=''\">").append(key).append("=#{")
                                .append(key).append("},</if>\r\n").toString();
                        type5 = (new StringBuilder(String.valueOf(type5))).append("\t\t<if test=\"").append(key).append("!=null and ").append(key).append("!=''\"> and ").append(key).append("=#{")
                                .append(key).append("}</if>\r\n").toString();
                        type6 = (new StringBuilder(String.valueOf(type6))).append("\t\t<if test=\"").append(key).append("!=null and ").append(key).append("!=''\"> and ").append(key).append("=#{")
                                .append(key).append("}</if>\r\n").toString();
                        type7 = (new StringBuilder(String.valueOf(type7))).append("and ").append(key).append("=#{").append(key).append("} ").toString();
                    }
                    type4 = (new StringBuilder(String.valueOf(type4))).append(key).toString();
                } else {
                    type1 = (new StringBuilder(String.valueOf(type1))).append(key).append(",").toString();
                    type2 = (new StringBuilder(String.valueOf(type2))).append("#{").append(key).append("},").toString();
                    if (!primaryKey.equals(key) || relatif) {
                        type3 = (new StringBuilder(String.valueOf(type3))).append("\t\t<if test=\"").append(key).append("!=null and ").append(key).append("!=''\">").append(key).append("=#{")
                                .append(key).append("},</if>\r\n").toString();
                        type5 = (new StringBuilder(String.valueOf(type5))).append("\t\t<if test=\"").append(key).append("!=null and ").append(key).append("!=''\"> and ").append(key).append("=#{")
                                .append(key).append("}</if>\r\n").toString();
                        type6 = (new StringBuilder(String.valueOf(type6))).append("\t\t<if test=\"").append(key).append("!=null and ").append(key).append("!=''\"> and ").append(key).append("=#{")
                                .append(key).append("}</if>\r\n").toString();
                        type7 = (new StringBuilder(String.valueOf(type7))).append("and ").append(key).append("=#{").append(key).append("} ").toString();
                    }
                    type4 = (new StringBuilder(String.valueOf(type4))).append(key).append(",").toString();
                }
                n++;
            }
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>").append("\r\n");
        buffer.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">").append("\r\n");
        buffer.append((new StringBuilder("<mapper namespace=\"com.dmp.web.dao.")).append(className).append("Dao\">").toString()).append("\r\n").append("\r\n");
        if (relatif) {
            buffer.append("\t").append((new StringBuilder("<insert id=\"create\" parameterType=\"")).append(className).append("\">").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("insert into ")).append(tableName).append(" ( ").append(type1).append(" )").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("values ( ")).append(type2).append(" )").toString()).append("\r\n");
            buffer.append("\t").append("</insert>").append("\r\n").append("\r\n");
            buffer.append("\t").append((new StringBuilder("<delete id=\"delete\" parameterType=\"")).append(className).append("\" >").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("delete from ")).append(tableName).toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder(" where ")).append(StringUtils.removeStart(type7, "and")).toString()).append("\r\n");
            buffer.append("\t").append("</delete>").append("\r\n").append("\r\n");
        } else {
            buffer.append("\t").append((new StringBuilder("<select id=\"get\" parameterType=\"long\" resultType=\"")).append(className).append("\">").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("select ")).append(type4).toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("from ")).append(tableName).toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("where ")).append(primaryKey).append("=#{id}").toString()).append("\r\n");
            buffer.append("\t").append("</select>").append("\r\n").append("\r\n");
            buffer.append("\t").append((new StringBuilder("<select id=\"getAll\" resultType=\"")).append(className).append("\">").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("select ")).append(type4).toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("from ")).append(tableName).toString()).append("\r\n");
            if (innerDel)
                buffer.append("\t\t").append("and del_flag = 0").append("\r\n");
            buffer.append("\t").append("</select>").append("\r\n").append("\r\n");
            buffer.append("\t")
                    .append((new StringBuilder("<insert id=\"create\" parameterType=\"")).append(className).append("\" useGeneratedKeys=\"true\" keyProperty=\"").append(primaryKey).append("\">")
                            .toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("insert into ")).append(tableName).append(" ( ").append(type1).append(" )").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("values ( ")).append(type2).append(" )").toString()).append("\r\n");
            buffer.append("\t").append("</insert>").append("\r\n").append("\r\n");
            buffer.append("\t").append((new StringBuilder("<update id=\"update\" parameterType=\"")).append(className).append("\" >").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("update ")).append(tableName).toString()).append("\r\n");
            buffer.append("\t\t").append("<set>").append("\r\n");
            buffer.append(type3).append("\r\n");
            buffer.append("\t\t").append("</set>").append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("where ")).append(primaryKey).append("=#{").append(primaryKey).append("}").toString()).append("\r\n");
            buffer.append("\t").append("</update>").append("\r\n").append("\r\n");
            if (innerDel) {
                buffer.append("\t").append("<update id=\"delete\" parameterType=\"long\" >").append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("update ")).append(tableName).toString()).append(" set del_flag=1 ").append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("where ")).append(primaryKey).append("=#{id}").toString()).append("\r\n");
                buffer.append("\t").append("</update>").append("\r\n").append("\r\n");
                buffer.append("\t").append("<update id=\"multiDelete\" parameterType=\"Map\" >").append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("update ")).append(tableName).toString()).append(" set del_flag=1 ").append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("where ")).append(primaryKey).append(" in (${ids})").toString()).append("\r\n");
                buffer.append("\t").append("</update>").append("\r\n").append("\r\n");
            } else {
                buffer.append("\t").append("<delete id=\"delete\" parameterType=\"long\" >").append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("delete from ")).append(tableName).toString()).append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("where ")).append(primaryKey).append("=#{id}").toString()).append("\r\n");
                buffer.append("\t").append("</delete>").append("\r\n").append("\r\n");
                buffer.append("\t").append("<delete id=\"multiDelete\" parameterType=\"Map\" >").append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("delete from ")).append(tableName).toString()).append("\r\n");
                buffer.append("\t\t").append((new StringBuilder("where ")).append(primaryKey).append(" in (${ids})").toString()).append("\r\n");
                buffer.append("\t").append("</delete>").append("\r\n").append("\r\n");
            }
            buffer.append("\t").append("<select id=\"count\" resultType=\"long\" >").append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("select count(*) from ")).append(tableName).toString()).append("\r\n");
            buffer.append("\t").append("</select>").append("\r\n").append("\r\n");
            buffer.append("\t").append((new StringBuilder("<select id=\"findByWhere\" parameterType=\"Map\" resultType=\"")).append(className).append("\">").toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("select ")).append(type4).toString()).append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("from ")).append(tableName).toString()).append("\r\n");
            buffer.append("\t\t").append("<where>").append("\r\n");
            if (innerDel)
                buffer.append("\t\t").append("and del_flag = 0").append("\r\n");
            buffer.append(type5).append("\r\n");
            buffer.append("\t\t").append("</where>").append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("ORDER BY ")).append(primaryKey).append(" desc").toString()).append("\r\n");
            buffer.append("\t\t").append("LIMIT #{lowerLimit},#{upperLimit}").append("\r\n");
            buffer.append("\t").append("</select>").append("\r\n").append("\r\n");
            buffer.append("\t").append("<select id=\"getCountByWhere\" parameterType=\"Map\" resultType=\"long\">").append("\r\n");
            buffer.append("\t\t").append("select count(*)").append("\r\n");
            buffer.append("\t\t").append((new StringBuilder("from ")).append(tableName).toString()).append("\r\n");
            buffer.append("\t\t").append("<where>").append("\r\n");
            if (innerDel)
                buffer.append("\t\t").append("and del_flag = 0").append("\r\n");
            buffer.append(type6).append("\r\n");
            buffer.append("\t\t").append("</where>").append("\r\n");
            buffer.append("\t").append("</select>").append("\r\n").append("\r\n");
        }
        buffer.append("</mapper>");
        resMap.put((new StringBuilder(String.valueOf(filePath))).append(File.separator).append(className).append("Mapper.xml").toString(), buffer.toString());
    }

    private static void generatePojoFile(Map resMap, String filePath, String tableName, String className, Map result, Map comments) {
        boolean relatif = isRelTable;
        StringBuffer buffer = new StringBuffer();
        buffer.append("package com.dmp.web.entity;").append("\r\n").append("\r\n");
        if (relatif) {
            buffer.append((new StringBuilder("public class ")).append(className).append(" implements java.io.Serializable {").toString()).append("\r\n").append("\r\n");
        } else {
            buffer.append("import java.util.Date;").append("\r\n");
            buffer.append("import com.dmp.modules.entity.AbstractEntity;").append("\r\n");
            buffer.append((new StringBuilder("public class ")).append(className).append(" extends AbstractEntity {").toString()).append("\r\n").append("\r\n");
        }
        buffer.append("\t").append("private static final long serialVersionUID = 1L;").append("\r\n");
        Set keys = result.keySet();
        for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            if (!"id".equals(key.toLowerCase()) && !"primarykey".equals(key.toLowerCase()) && !"updated_date".equals(key.toLowerCase()) && !"updated_by".equals(key.toLowerCase())
                    && !"created_by".equals(key.toLowerCase()) && !"created_date".equals(key.toLowerCase())) {
                String comment = (String) comments.get(key);
                String javaType = getJavaType((String) result.get(key));
                if ("del_flag".equals(key))
                    buffer.append("\t").append((new StringBuilder("private ")).append(javaType).append(" ").append(key).append(" = 0;").toString());
                else
                    buffer.append("\t").append((new StringBuilder("private ")).append(javaType).append(" ").append(key).append(" ;").toString());
                if (StringUtils.isNotBlank(comment))
                    buffer.append((new StringBuilder("//")).append(comment).toString());
                buffer.append("\r\n");
            }
        }

        StringBuffer methodBuffer = new StringBuffer();
        for (Iterator iterator1 = result.keySet().iterator(); iterator1.hasNext(); ) {
            String key = (String) iterator1.next();
            if (!"id".equals(key.toLowerCase()) && !"primarykey".equals(key.toLowerCase()) && !"updated_date".equals(key.toLowerCase()) && !"updated_by".equals(key.toLowerCase())
                    && !"created_by".equals(key.toLowerCase()) && !"created_date".equals(key.toLowerCase())) {
                String javaType = getJavaType((String) result.get(key));
                String camelKey = StringUtils.capitalize(key);
                methodBuffer.append("\r\n");
                methodBuffer.append("\t").append("public ").append(javaType).append(" get").append(camelKey).append("() {").append("\r\n");
                methodBuffer.append("\t").append("\t").append("return ").append(key).append(";").append("\r\n");
                methodBuffer.append("\t").append("} ").append("\r\n");
                methodBuffer.append("\r\n");
                methodBuffer.append("\t").append("public void").append(" set").append(camelKey).append("(").append(javaType).append(" ").append(key).append(") {").append("\r\n");
                methodBuffer.append("\t").append("\t").append("this.").append(key).append(" = ").append(key).append(";").append("\r\n");
                methodBuffer.append("\t").append("} ").append("\r\n");
                methodBuffer.append("\r\n");
            }
        }

        buffer.append(methodBuffer.toString());
        buffer.append("}").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(filePath))).append(File.separator).append(className).append(".java").toString(), buffer.toString());
    }

    private static void generateDaoFile(Map resMap, String filePath, String tableName, String className) {
        boolean relatif = isRelTable;
        StringBuffer buffer = new StringBuffer();
        buffer.append("package com.dmp.web.dao;").append("\r\n").append("\r\n");
        if (relatif) {
            buffer.append("import com.dmp.modules.dao.MyBatisRepository;").append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.entity.")).append(className).append(";").toString()).append("\r\n");
            buffer.append("\r\n");
            buffer.append("@MyBatisRepository").append("\r\n");
            buffer.append("public interface ").append(className).append("Dao {").append("\r\n");
            buffer.append("\t\t").append("public void create(").append((new StringBuilder(String.valueOf(className))).append(" ").append(StringUtils.uncapitalize(className)).toString())
                    .append(");\r\n");
            buffer.append("\t\t").append("public void delete(").append((new StringBuilder(String.valueOf(className))).append(" ").append(StringUtils.uncapitalize(className)).toString())
                    .append(");\r\n");
        } else {
            buffer.append("import com.dmp.modules.dao.IParentDAO;").append("\r\n");
            buffer.append("import com.dmp.modules.dao.MyBatisRepository;").append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.entity.")).append(className).append(";").toString()).append("\r\n");
            buffer.append("\r\n");
            buffer.append("@MyBatisRepository").append("\r\n");
            buffer.append("public interface ").append(className).append("Dao extends IParentDAO<").append(className).append(", Long> {").append("\r\n");
        }
        buffer.append("\r\n");
        buffer.append("}").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(filePath))).append(File.separator).append(className).append("Dao.java").toString(), buffer.toString());
    }

    private static void generateServiceFile(Map resMap, String filePath, String tableName, String className, Map result) {
        String primaryKey = "";
        for (Iterator iterator = result.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            if ("primarykey".equals(key.toLowerCase()))
                primaryKey = (String) result.get("PrimaryKey");
        }
        boolean relatif = isRelTable;
        StringBuffer buffer = new StringBuffer();
        buffer.append("package com.dmp.web.service;").append("\r\n").append("\r\n");
        if (relatif) {
            buffer.append("import org.springframework.beans.factory.annotation.Autowired;").append("\r\n");
            buffer.append("import org.springframework.stereotype.Component;").append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.dao.")).append(className).append("Dao;").toString()).append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.entity.")).append(className).append(";").toString()).append("\r\n");
            buffer.append("\r\n");
            buffer.append("@Component").append("\r\n");
            buffer.append("public class ").append(className).append("Service {").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Autowired").append("\r\n");
            buffer.append("\t").append("private ").append(className).append("Dao ").append(StringUtils.uncapitalize(className)).append("Dao;").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Transactional(rollbackFor = { java.lang.RuntimeException.class, java.lang.Exception.class })").append("\r\n");
            buffer.append("\t").append("public void create(").append((new StringBuilder(String.valueOf(className))).append(" ").append(StringUtils.uncapitalize(className)).toString())
                    .append("){\r\n");
            buffer.append("\t").append("\t").append(StringUtils.uncapitalize(className)).append((new StringBuilder("Dao.create(")).append(StringUtils.uncapitalize(className)).append(");").toString())
                    .append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\t").append("@Transactional(rollbackFor = { java.lang.RuntimeException.class, java.lang.Exception.class })").append("\r\n");
            buffer.append("\t").append("public void delete(").append((new StringBuilder(String.valueOf(className))).append(" ").append(StringUtils.uncapitalize(className)).toString())
                    .append("){\r\n");
            buffer.append("\t").append("\t").append(StringUtils.uncapitalize(className)).append((new StringBuilder("Dao.delete(")).append(StringUtils.uncapitalize(className)).append(");").toString())
                    .append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\t").append("@Transactional(rollbackFor = { java.lang.RuntimeException.class, java.lang.Exception.class })").append("\r\n");
            buffer.append("\t").append("public void update(").append((new StringBuilder(String.valueOf(className))).append(" newObj, ").append(className).append(" oldObj").toString())
                    .append("){\r\n");
            buffer.append("\t").append("\t").append(StringUtils.uncapitalize(className)).append("Dao.delete(oldObj);").append("\r\n");
            buffer.append("\t").append("\t").append(StringUtils.uncapitalize(className)).append("Dao.create(newObj);").append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
        } else {
            buffer.append("import org.springframework.beans.factory.annotation.Autowired;").append("\r\n");
            buffer.append("import org.springframework.stereotype.Component;").append("\r\n");
            buffer.append("import com.dmp.modules.service.AbstractService;").append("\r\n");
            buffer.append("import com.dmp.web.global.service.SequenceService;").append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.dao.")).append(className).append("Dao;").toString()).append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.entity.")).append(className).append(";").toString()).append("\r\n");
            buffer.append("\r\n");
            buffer.append("@Component").append("\r\n");
            buffer.append("public class ").append(className).append("Service extends AbstractService<").append(className).append(", Long, ").append(className).append("Dao> {").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Autowired").append("\r\n");
            buffer.append("\t").append("private ").append(className).append("Dao ").append(StringUtils.uncapitalize(className)).append("Dao;").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Autowired").append("\r\n");
            buffer.append("\t").append("private SequenceService sequenceService;").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Override").append("\r\n");
            buffer.append("\t").append("protected ").append(className).append("Dao ").append("getDao() {").append("\r\n");
            buffer.append("\t").append("\t").append("return ").append(StringUtils.uncapitalize(className)).append("Dao;").append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\t").append("@Override").append("\r\n");
            buffer.append("\t").append("@Transactional(rollbackFor = { java.lang.RuntimeException.class, java.lang.Exception.class })").append("\r\n");
            buffer.append("\t").append((new StringBuilder("public void create(")).append(className).append(" ").append(className.toLowerCase()).append(") {").toString()).append("\r\n");
            buffer.append("\t")
                    .append("\t")
                    .append((new StringBuilder(String.valueOf(className.toLowerCase()))).append(".set").append(StringUtils.capitalize(primaryKey)).append("(sequenceService.getNextSeqLongValue(\"")
                            .append(tableName).append("\"));").toString()).append("\r\n");
            buffer.append("\t").append("\t").append((new StringBuilder(String.valueOf(className.toLowerCase()))).append("Dao.create(").append(className.toLowerCase()).append(");").toString())
                    .append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
        }
        buffer.append("\r\n");
        buffer.append("}").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(filePath))).append(File.separator).append(className).append("Service.java").toString(), buffer.toString());
    }

    private static void generateControllerFile(Map resMap, String filePath, String tableName, String className) {
        boolean relatif = isRelTable;
        if (relatif) {
            return;
        } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append("package com.dmp.web.controller;").append("\r\n").append("\r\n");
            buffer.append("import java.lang.reflect.Type;").append("\r\n");
            buffer.append("import java.util.List;").append("\r\n");
            buffer.append("import com.google.gson.reflect.TypeToken;").append("\r\n");
            buffer.append("\r\n");
            buffer.append("import org.slf4j.Logger;").append("\r\n");
            buffer.append("import org.slf4j.LoggerFactory;").append("\r\n");
            buffer.append("import org.springframework.beans.factory.annotation.Autowired;").append("\r\n");
            buffer.append("import org.springframework.stereotype.Controller;").append("\r\n");
            buffer.append("import org.springframework.web.bind.annotation.RequestMapping;").append("\r\n");
            buffer.append("\r\n");
            buffer.append("import com.dmp.web.controller.AbstractController;").append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.entity.")).append(className).append(";").toString()).append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.dao.")).append(className).append("Dao;").toString()).append("\r\n");
            buffer.append((new StringBuilder("import com.dmp.web.service.")).append(className).append("Service;").toString()).append("\r\n");
            buffer.append("\r\n");
            buffer.append("@Controller").append("\r\n");
            buffer.append((new StringBuilder("@RequestMapping(value = \"/")).append(className.toLowerCase()).append("\")").toString()).append("\r\n");
            buffer.append("public class ").append(className).append("Controller extends AbstractController<").append(className).append(",").append(className).append("Dao").append(",")
                    .append(className).append("Service> {").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("private static final Logger log = LoggerFactory.getLogger(").append(className).append("Controller.class);").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Autowired").append("\r\n");
            buffer.append("\t").append("private ").append(className).append("Service ").append(StringUtils.uncapitalize(className)).append("Service;").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Override").append("\r\n");
            buffer.append("\t").append("protected ").append(className).append("Service ").append("getService() {").append("\r\n");
            buffer.append("\t").append("\t").append("return ").append(StringUtils.uncapitalize(className)).append("Service;").append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Override").append("\r\n");
            buffer.append("\t").append("protected Class<").append(className).append("> getEntityClass() {").append("\r\n");
            buffer.append("\t").append("\t").append("return ").append(className).append(".class;").append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Override").append("\r\n");
            buffer.append("\t").append("protected Type getEntityType() {").append("\r\n");
            buffer.append("\t").append("\t").append("return new TypeToken<").append(className).append(">(){}.getType();").append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\r\n");
            buffer.append("\t").append("@Override").append("\r\n");
            buffer.append("\t").append("protected Type getEntityListType() {").append("\r\n");
            buffer.append("\t").append("\t").append("return new TypeToken<List<").append(className).append(">>(){}.getType();").append("\r\n");
            buffer.append("\t").append("} ").append("\r\n");
            buffer.append("\r\n");
            buffer.append("}").append("\r\n");
            resMap.put((new StringBuilder(String.valueOf(filePath))).append(File.separator).append(className).append("Controller.java").toString(), buffer.toString());
            return;
        }
    }

    private static void generateMfhPHPFile(Map resMap, DbUtils dbUtils, String filePath, String className, Map result) {
        String conPath = (new StringBuilder(String.valueOf(filePath))).append(File.separator).append("controllers").toString();
        String viewPath = (new StringBuilder(String.valueOf(filePath))).append(File.separator).append("views").append(File.separator).append(className).toString();
        List dateFields = new ArrayList();
        String primaryKey = (String) result.get("PrimaryKey");
        for (Iterator iterator = result.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            if (!"primarykey".equals(key.toLowerCase()) && !"updated_date".equals(key.toLowerCase()) && !"updated_by".equals(key.toLowerCase()) && !"created_by".equals(key.toLowerCase())
                    && !"created_date".equals(key.toLowerCase()) && "Date".equals(getJavaType((String) result.get(key))))
                dateFields.add(key);
        }

        StringBuffer bufCont = new StringBuffer();
        bufCont.append("<?php").append("\r\n").append("\r\n");
        bufCont.append("class ").append(className).append("Controller extends BaseController {").append("\r\n");
        bufCont.append("\t").append("public $layout = '/layouts/iframecolumn1';").append("\r\n").append("\r\n");
        bufCont.append("\t").append("public function actionList").append(className).append("() {").append("\r\n");
        bufCont.append("\t\t").append((new StringBuilder("$this -> render(\"list")).append(className).append("\");").toString()).append("\r\n");
        bufCont.append("\t").append("}").append("\r\n").append("\r\n");
        bufCont.append("\t").append("public function actionAdd").append(className).append("() {").append("\r\n");
        bufCont.append("\t\t").append((new StringBuilder("$this -> render(\"add")).append(className).append("\");").toString()).append("\r\n");
        bufCont.append("\t").append("}").append("\r\n").append("\r\n");
        bufCont.append("\t").append("public function actionUpdate").append(className).append("() {").append("\r\n");
        bufCont.append("\t\t").append((new StringBuilder("$this -> render(\"update")).append(className).append("\");").toString()).append("\r\n");
        bufCont.append("\t").append("}").append("\r\n").append("\r\n");
        bufCont.append("\t").append("public function actionDetail").append(className).append("() {").append("\r\n");
        bufCont.append("\t\t").append((new StringBuilder("$this -> render(\"detail")).append(className).append("\");").toString()).append("\r\n");
        bufCont.append("\t").append("}").append("\r\n").append("\r\n");
        bufCont.append("}").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(conPath))).append(File.separator).append(className).append("Controller.php").toString(), bufCont.toString());
        StringBuffer bufList = new StringBuffer();
        bufList.append((new StringBuilder("<table id=\"")).append(className).append("Table\" style=\"width:100%;height:100%;\"></table>").toString()).append("\r\n");
        bufList.append((new StringBuilder("<div id=\"")).append(className).append("Toolbar\">").toString()).append("\r\n");
        bufList.append("\t").append("<table>").append("\r\n");
        bufList.append("\t").append("\t").append("<tbody>").append("\r\n");
        bufList.append("\t").append("\t").append("\t").append("<tr>").append("\r\n");
        bufList.append("\t").append("\t").append("\t").append("\t");
        bufList.append((new StringBuilder("<a href=\"#\" onclick=\"javascript:add")).append(className).append("();\" class=\"easyui-linkbutton easy-hide\" iconCls=\"icon-add\" plain=\"true\">新增</a>")
                .toString());
        bufList.append("\r\n");
        bufList.append("\t").append("\t").append("\t").append("\t");
        bufList.append((new StringBuilder("<a href=\"#\" onclick=\"javascript:update")).append(className)
                .append("();\" class=\"easyui-linkbutton easy-hide\" iconCls=\"icon-edit\" plain=\"true\">修改</a>").toString());
        bufList.append("\r\n");
        bufList.append("\t").append("\t").append("\t").append("\t");
        bufList.append((new StringBuilder("<a href=\"#\" onclick=\"javascript:del")).append(className)
                .append("();\" class=\"easyui-linkbutton easy-hide\" iconCls=\"icon-remove\" plain=\"true\">删除</a>").toString());
        bufList.append("\r\n");
        bufList.append("\t").append("\t").append("\t").append("</tr>").append("\r\n");
        bufList.append("\t").append("\t").append("</tbody>").append("\r\n");
        bufList.append("\t").append("</table>").append("\r\n");
        bufList.append("\t").append("<div style=\"margin-left:10px;margin-top: 5px\">").append("\r\n");
        bufList.append("\t\t").append("<form id=\"searchinfo-form\">").append("\r\n");
        bufList.append("\t\t").append("</form>").append("\r\n");
        bufList.append("\t").append("</div>").append("\r\n");
        bufList.append("</div>").append("\r\n").append("\r\n");
        bufList.append("<script type=\"text/javascript\">").append("\r\n");
        bufList.append("$(function () {").append("\r\n");
        bufList.append("\t").append("$('.easyui-linkbutton').linkbutton();").append("\r\n");
        bufList.append("\t").append((new StringBuilder("$(\"#")).append(className).append("Table\").datagrid({").toString()).append("\r\n");
        bufList.append("\t\t").append("width: \"100%\",").append("\r\n");
        bufList.append("\t\t").append("height: \"100%\",").append("\r\n");
        bufList.append("\t\t").append("striped: true,").append("\r\n");
        bufList.append("\t\t").append("pageSize: 15,").append("\r\n");
        bufList.append("\t\t").append("pageList: [10,15,20],").append("\r\n");
        bufList.append("\t\t").append("singleSelect : false,").append("\r\n");
        bufList.append("\t\t").append("url:'<?php echo Yii::app()->BaseUrl ?>/index.php/Dynamic/DCWithPageDirectly',").append("\r\n");
        bufList.append("\t\t").append("loadMsg:'数据加载中请稍后……',").append("\r\n");
        bufList.append("\t\t").append((new StringBuilder("queryParams:toJsonStrParam(\"")).append(className.toLowerCase()).append("/list\",{}),").toString()).append("\r\n");
        bufList.append("\t\t").append("pagination: true,").append("\r\n");
        bufList.append("\t\t").append("rownumbers: true,").append("\r\n");
        bufList.append("\t\t").append((new StringBuilder("toolbar: '#")).append(className).append("Toolbar',").toString()).append("\r\n");
        bufList.append("\t\t").append("frozenColumns: [[").append("\r\n");
        bufList.append("\t\t\t").append("{ field: 'ck', checkbox: true }").append("\r\n");
        bufList.append("\t\t").append("]],").append("\r\n");
        bufList.append("\t\t").append("columns:[[").append("\r\n");
        List keys = transToList(result);
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if (!"del_flag".equals(key.toLowerCase()) && !"id".equals(key.toLowerCase()) && !"primarykey".equals(key.toLowerCase()) && !"updated_date".equals(key.toLowerCase())
                    && !"updated_by".equals(key.toLowerCase()) && !"created_by".equals(key.toLowerCase()) && !"created_date".equals(key.toLowerCase())) {
                bufList.append("\t\t\t").append((new StringBuilder("{field:'")).append(key).append("',title: '").append(key).append("',align: 'center',width:getWidth(0.1)").toString());
                if (primaryKey.equals(key))
                    bufList.append(",hidden:true");
                bufList.append("}");
                if (i < keys.size() - 1)
                    bufList.append(",");
                bufList.append("\r\n");
            }
        }
        bufList.append("\t\t").append("]],").append("\r\n");
        bufList.append("\t\t").append("onLoadSuccess:function(){").append("\r\n");
        bufList.append("\t\t\t").append("parent.$.messager.progress('close');").append("\r\n");
        bufList.append("\t\t").append("}").append("\r\n");
        bufList.append("\t").append("});").append("\r\n");
        bufList.append("});").append("\r\n").append("\r\n");
        bufList.append("function search(){}").append("\r\n").append("\r\n");
        bufList.append((new StringBuilder("function add")).append(className).append("(){").toString()).append("\r\n");
        bufList.append("\t").append("parent.$.modalDialog({").append("\r\n");
        bufList.append("\t\t").append("title:\"新增\",").append("\r\n");
        bufList.append("\t\t").append((new StringBuilder("href:\"<?php echo Yii::app()->baseUrl ?>/index.php/system/")).append(className).append("/Add").append(className).append("\",").toString())
                .append("\r\n");
        bufList.append("\t\t").append("height:400,").append("\r\n");
        bufList.append("\t\t").append("width:600,").append("\r\n");
        bufList.append("\t\t").append("buttons : [{").append("\r\n");
        bufList.append("\t\t\t").append("text : '提交',").append("\r\n");
        bufList.append("\t\t\t").append("handler : function() {").append("\r\n");
        bufList.append("\t\t\t\t").append((new StringBuilder("parent.$.modalDialog.dataGrid = $(\"#")).append(className).append("Table\");").toString()).append("\r\n");
        bufList.append("\t\t\t\t").append((new StringBuilder("parent.$.modalDialog.handler.unique.find('#add")).append(className).append("Form').submit();").toString()).append("\r\n");
        bufList.append("\t\t\t").append("}").append("\r\n");
        bufList.append("\t\t").append("}]").append("\r\n");
        bufList.append("\t").append("});").append("\r\n");
        bufList.append("}").append("\r\n").append("\r\n");
        bufList.append((new StringBuilder("function update")).append(className).append("(){").toString()).append("\r\n");
        bufList.append("\t").append((new StringBuilder("var rows = $('#")).append(className).append("Table').datagrid('getSelections');").toString()).append("\r\n");
        bufList.append("\t").append("if(rows.length == 0){").append("\r\n");
        bufList.append("\t\t").append("alertFn(\"操作提示\",\"请先选择一条信息！\",\"info\");").append("\r\n");
        bufList.append("\t\t").append("return;").append("\r\n");
        bufList.append("\t").append("}").append("\r\n");
        bufList.append("\t").append("if(rows.length > 1){").append("\r\n");
        bufList.append("\t\t").append("alertFn(\"操作提示\",\"每次最多只能选择一条信息！\",\"info\");").append("\r\n");
        bufList.append("\t\t").append("return;").append("\r\n");
        bufList.append("\t").append("}").append("\r\n");
        bufList.append("\t").append((new StringBuilder("var id = rows[0].")).append(primaryKey).append(";").toString()).append("\r\n");
        bufList.append("\t").append("parent.$.modalDialog({").append("\r\n");
        bufList.append("\t\t").append("title:\"修改\",").append("\r\n");
        bufList.append("\t\t").append((new StringBuilder("href:'<?php echo Yii::app()->BaseUrl ?>/index.php/system/")).append(className).append("/Update").append(className).append("',").toString())
                .append("\r\n");
        bufList.append("\t\t").append("height:400,").append("\r\n");
        bufList.append("\t\t").append("width:600,").append("\r\n");
        bufList.append("\t\t").append("onOpen:function(){").append("\r\n");
        bufList.append("\t\t\t").append("parent.$.modalDialog.recId = id;").append("\r\n");
        bufList.append("\t\t").append("},").append("\r\n");
        bufList.append("\t\t").append("buttons : [{").append("\r\n");
        bufList.append("\t\t\t").append("text : '修改',").append("\r\n");
        bufList.append("\t\t\t").append("handler : function() {").append("\r\n");
        bufList.append("\t\t\t\t").append((new StringBuilder("parent.$.modalDialog.dataGrid = $(\"#")).append(className).append("Table\");").toString()).append("\r\n");
        bufList.append("\t\t\t\t").append((new StringBuilder("parent.$.modalDialog.handler.unique.find('#update")).append(className).append("Form').submit();").toString()).append("\r\n");
        bufList.append("\t\t\t").append("}").append("\r\n");
        bufList.append("\t\t").append("}]").append("\r\n");
        bufList.append("\t").append("});").append("\r\n");
        bufList.append("}").append("\r\n").append("\r\n");
        bufList.append((new StringBuilder("function del")).append(className).append("(){").toString()).append("\r\n");
        bufList.append("\t").append((new StringBuilder("var rows = $(\"#")).append(className).append("Table\").datagrid('getSelections');").toString()).append("\r\n");
        bufList.append("\t").append("if(rows.length == 0){").append("\r\n");
        bufList.append("\t\t").append("alertFn(\"操作提示\",\"请先选择信息！\",\"info\");").append("\r\n");
        bufList.append("\t\t").append("return;").append("\r\n");
        bufList.append("\t").append("}").append("\r\n");
        bufList.append("\t").append("$.messager.confirm('提示信息', '您确认要删除吗?', function (d) {").append("\r\n");
        bufList.append("\t\t").append("if(d){").append("\r\n");
        bufList.append("\t\t\t").append("$.messager.progress({title : '提示',text : '数据处理中，请稍后....'});").append("\r\n");
        bufList.append("\t\t\t").append("var ids = \"\";").append("\r\n");
        bufList.append("\t\t\t").append("$.each(rows, function(i, row){").append("\r\n");
        bufList.append("\t\t\t\t").append((new StringBuilder("ids += row.")).append(primaryKey).append("+\",\";").toString()).append("\r\n");
        bufList.append("\t\t\t").append("});").append("\r\n");
        bufList.append("\t\t\t").append("ids = ids.substring(0,(ids.length-1));").append("\r\n");
        bufList.append("\t\t\t").append("var json = {};").append("\r\n");
        bufList.append("\t\t\t").append("json.ids = ids;").append("\r\n");
        bufList.append("\t\t\t").append("$.ajax({").append("\r\n");
        bufList.append("\t\t\t\t").append("type : 'POST',").append("\r\n");
        bufList.append("\t\t\t\t").append("cache : false,").append("\r\n");
        bufList.append("\t\t\t\t").append((new StringBuilder("data :toJsonStrParam('")).append(className.toLowerCase()).append("/multiDelete',json),").toString()).append("\r\n");
        bufList.append("\t\t\t\t").append("url:'<?php echo Yii::app()->BaseUrl ?>/index.php/Dynamic/DCDirectly',").append("\r\n");
        bufList.append("\t\t\t\t").append("dataType : 'json',").append("\r\n");
        bufList.append("\t\t\t\t").append("error: function () {").append("\r\n");
        bufList.append("\t\t\t\t\t").append("$.messager.alert('错误', '删除失败!', 'error');").append("\r\n");
        bufList.append("\t\t\t\t").append("},").append("\r\n");
        bufList.append("\t\t\t\t").append("success: function (ret) {").append("\r\n");
        bufList.append("\t\t\t\t\t").append("if (ret.code=='0') {").append("\r\n");
        bufList.append("\t\t\t\t\t\t").append((new StringBuilder("$(\"#")).append(className).append("Table\").datagrid('reload');").toString()).append("\r\n");
        bufList.append("\t\t\t\t\t\t").append((new StringBuilder("$(\"#")).append(className).append("Table\").datagrid('clearSelections');").toString()).append("\r\n");
        bufList.append("\t\t\t\t\t").append("} else {").append("\r\n");
        bufList.append("\t\t\t\t\t\t").append("$.messager.alert('错误', ret.msg, 'error');").append("\r\n");
        bufList.append("\t\t\t\t\t").append("}").append("\r\n");
        bufList.append("\t\t\t\t\t").append("$.messager.progress('close');").append("\r\n");
        bufList.append("\t\t\t\t").append("}").append("\r\n");
        bufList.append("\t\t\t").append("});").append("\r\n");
        bufList.append("\t\t").append("}").append("\r\n");
        bufList.append("\t").append("});").append("\r\n");
        bufList.append("\t").append("}").append("\r\n");
        bufList.append("function showDetail(){}").append("\r\n").append("\r\n");
        bufList.append("</script>").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(viewPath))).append(File.separator).append("list").append(className).append(".php").toString(), bufList.toString());
        StringBuffer bufAdd = new StringBuffer();
        bufAdd.append((new StringBuilder("<form id=\"add")).append(className).append("Form\" style=\"padding:10px;5px,10px,10px\" method=\"post\">").toString()).append("\r\n");
        bufAdd.append("\t").append("<table style=\"font-size: 12px;\" width=\"100%\">").append("\r\n");
        bufAdd.append("\t").append("<tbody>").append("\r\n");
        int k = 0;
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if (!primaryKey.equals(key.toLowerCase()) && !"del_flag".equals(key.toLowerCase()) && !"id".equals(key.toLowerCase()) && !"primarykey".equals(key.toLowerCase())
                    && !"updated_date".equals(key.toLowerCase()) && !"updated_by".equals(key.toLowerCase()) && !"created_by".equals(key.toLowerCase()) && !"created_date".equals(key.toLowerCase())) {
                if (k == 2)
                    k = 0;
                if (++k == 1)
                    bufAdd.append("\t\t").append("<tr>").append("\r\n");
                bufAdd.append("\t\t\t").append((new StringBuilder("<td><label for=\"")).append(key).append("\">").append(key).append("</label></td>").toString()).append("\r\n");
                String type = getJavaType((String) result.get(key));
                if ("Date".equals(type))
                    bufAdd.append("\t\t\t")
                            .append((new StringBuilder("<td><input id=\"")).append(key).append("\" name=\"").append(key).append("\" class=\"easyui-datebox\" type=\"text\" /></td>").toString())
                            .append("\r\n");
                else
                    bufAdd.append("\t\t\t")
                            .append((new StringBuilder("<td><input id=\"")).append(key).append("\" name=\"").append(key).append("\" class=\"easyui-validatebox\" type=\"text\" /></td>").toString())
                            .append("\r\n");
                if (k == 2)
                    bufAdd.append("\t\t").append("</tr>").append("\r\n");
            }
        }
        if (k == 1)
            bufAdd.append("\t\t").append("</tr>").append("\r\n");
        bufAdd.append("\t").append("</tbody>").append("\r\n");
        bufAdd.append("\t").append("</table>").append("\r\n");
        bufAdd.append("</form>").append("\r\n");
        bufAdd.append("<script type=\"text/javascript\">").append("\r\n");
        bufAdd.append("$(function () {").append("\r\n");
        bufAdd.append("\t").append("$('.easyui-validatebox').validatebox(); ").append("\r\n");
        bufAdd.append("\t").append("$('.easyui-datebox').datebox(); ").append("\r\n");
        bufAdd.append("\t").append("$.messager.progress('close');").append("\r\n");
        bufAdd.append("\t").append((new StringBuilder("$('#add")).append(className).append("Form').form({").toString()).append("\r\n");
        bufAdd.append("\t\t").append("url : '<?php echo Yii::app()->baseUrl ?>/index.php/Dynamic/DCDirectly',").append("\r\n");
        bufAdd.append("\t\t").append("onSubmit : function(param) {").append("\r\n");
        bufAdd.append("\t\t\t").append("$.messager.progress({title : '提示',text : '数据处理中，请稍后....'});").append("\r\n");
        bufAdd.append("\t\t\t").append((new StringBuilder("param.serviceUrl = '")).append(className.toLowerCase()).append("/create';").toString()).append("\r\n");
        if (dateFields.size() > 0) {
            bufAdd.append("\t\t\t").append((new StringBuilder("var json = getFormJsonData(\"add")).append(className).append("Form\");").toString()).append("\r\n");
            String df;
            for (Iterator iterator1 = dateFields.iterator(); iterator1.hasNext(); bufAdd.append("\t\t\t")
                    .append((new StringBuilder("json.")).append(df).append(" = $('#").append(df).append("').datebox('getValue')+' 00:00:00';").toString()).append("\r\n"))
                df = (String) iterator1.next();
            bufAdd.append("\t\t\t").append("param.jsonStr = JSON.stringify(json);").append("\r\n");
        } else {
            bufAdd.append("\t\t\t").append((new StringBuilder("param.jsonStr = getFormData(\"add")).append(className).append("Form\");").toString()).append("\r\n");
        }
        bufAdd.append("\t\t\t").append("var isValid = $(this).form('validate');").append("\r\n");
        bufAdd.append("\t\t\t").append("if (!isValid) {").append("\r\n");
        bufAdd.append("\t\t\t\t").append("$.messager.progress('close');").append("\r\n");
        bufAdd.append("\t\t\t").append("}").append("\r\n");
        bufAdd.append("\t\t\t").append("return isValid;").append("\r\n");
        bufAdd.append("\t\t").append("},").append("\r\n");
        bufAdd.append("\t\t").append("success : function(result) {").append("\r\n");
        bufAdd.append("\t\t\t").append("$.messager.progress('close');").append("\r\n");
        bufAdd.append("\t\t\t").append("var res = $.parseJSON(result);").append("\r\n");
        bufAdd.append("\t\t\t").append("if (res.code == 0) {").append("\r\n");
        bufAdd.append("\t\t\t\t").append("$.modalDialog.dataGrid.datagrid('reload');").append("\r\n");
        bufAdd.append("\t\t\t\t").append("$.modalDialog.dataGrid.datagrid('clearSelections');").append("\r\n");
        bufAdd.append("\t\t\t\t").append("$.modalDialog.handler.unique.dialog('close');").append("\r\n");
        bufAdd.append("\t\t\t").append("}else{").append("\r\n");
        bufAdd.append("\t\t\t\t").append("$.messager.alert('错误', res.msg, 'error');").append("\r\n");
        bufAdd.append("\t\t\t").append("}").append("\r\n");
        bufAdd.append("\t\t").append("}").append("\r\n");
        bufAdd.append("\t").append("});").append("\r\n");
        bufAdd.append("});").append("\r\n").append("\r\n");
        bufAdd.append("</script>").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(viewPath))).append(File.separator).append("add").append(className).append(".php").toString(), bufAdd.toString());
        StringBuffer bufUpd = new StringBuffer();
        bufUpd.append((new StringBuilder("<form id=\"update")).append(className).append("Form\" style=\"padding:10px;5px,10px,10px\" method=\"post\">").toString()).append("\r\n");
        bufUpd.append((new StringBuilder("<input id=\"")).append(primaryKey).append("\" name=\"").append(primaryKey).append("\" type=\"hidden\" />").toString()).append("\r\n");
        bufUpd.append("\t").append("<table style=\"font-size: 12px;\" width=\"100%\">").append("\r\n");
        bufUpd.append("\t").append("<tbody>").append("\r\n");
        int n = 0;
        for (int i = 0; i < keys.size(); i++) {
            String key = (String) keys.get(i);
            if (!primaryKey.equals(key.toLowerCase()) && !"del_flag".equals(key.toLowerCase()) && !"id".equals(key.toLowerCase()) && !"primarykey".equals(key.toLowerCase())
                    && !"updated_date".equals(key.toLowerCase()) && !"updated_by".equals(key.toLowerCase()) && !"created_by".equals(key.toLowerCase()) && !"created_date".equals(key.toLowerCase())) {
                if (n == 2)
                    n = 0;
                if (++n == 1)
                    bufUpd.append("\t\t").append("<tr>").append("\r\n");
                bufUpd.append("\t\t\t").append((new StringBuilder("<td><label for=\"")).append(key).append("\">").append(key).append("</label></td>").toString()).append("\r\n");
                String type = getJavaType((String) result.get(key));
                if ("Date".equals(type))
                    bufUpd.append("\t\t\t")
                            .append((new StringBuilder("<td><input id=\"")).append(key).append("\" name=\"").append(key).append("\" class=\"easyui-datebox\" type=\"text\" /></td>").toString())
                            .append("\r\n");
                else
                    bufUpd.append("\t\t\t")
                            .append((new StringBuilder("<td><input id=\"")).append(key).append("\" name=\"").append(key).append("\" class=\"easyui-validatebox\" type=\"text\" /></td>").toString())
                            .append("\r\n");
                if (n == 2)
                    bufUpd.append("\t\t").append("</tr>").append("\r\n");
            }
        }
        if (n == 1)
            bufUpd.append("\t\t").append("</tr>").append("\r\n");
        bufUpd.append("\t").append("</tbody>").append("\r\n");
        bufUpd.append("\t").append("</table>").append("\r\n");
        bufUpd.append("</form>").append("\r\n");
        bufUpd.append("<script type=\"text/javascript\">").append("\r\n");
        bufUpd.append("$(function () {").append("\r\n");
        bufUpd.append("\t").append("$('.easyui-validatebox').validatebox(); ").append("\r\n");
        bufUpd.append("\t").append("$('.easyui-datebox').datebox(); ").append("\r\n");
        bufUpd.append("\t").append("$.messager.progress({title : '提示',text : '数据加载中，请稍后....'});").append("\r\n");
        bufUpd.append("\t").append("var json = {};").append("\r\n");
        bufUpd.append("\t").append("json.id = $.modalDialog.recId;").append("\r\n");
        bufUpd.append("\t").append("$.ajax({").append("\r\n");
        bufUpd.append("\t\t").append("type: 'POST',").append("\r\n");
        bufUpd.append("\t\t").append((new StringBuilder("data :toJsonStrParam('")).append(className.toLowerCase()).append("/getById',json),").toString()).append("\r\n");
        bufUpd.append("\t\t").append("url: '<?php echo Yii::app()->baseUrl ?>/index.php/Dynamic/DCDirectly',").append("\r\n");
        bufUpd.append("\t\t").append("dataType:'json',").append("\r\n");
        bufUpd.append("\t\t").append("success: function (d) {").append("\r\n");
        bufUpd.append("\t\t\t").append("if (d.code == 0) {").append("\r\n");
        bufUpd.append("\t\t\t\t").append((new StringBuilder("$(\"#update")).append(className).append("Form\").form('load',d.data);").toString()).append("\r\n");
        if (dateFields.size() > 0) {
            String df;
            for (Iterator iterator2 = dateFields.iterator(); iterator2.hasNext(); bufUpd.append("\t\t\t\t")
                    .append((new StringBuilder("$('#")).append(df).append("').datebox('setValue',d.data.").append(df).append(");").toString()).append("\r\n"))
                df = (String) iterator2.next();
        }
        bufUpd.append("\t\t\t").append("} else {").append("\r\n");
        bufUpd.append("\t\t\t\t").append("$.messager.alert('错误', '数据加载失败！', 'error');").append("\r\n");
        bufUpd.append("\t\t\t").append("}").append("\r\n");
        bufUpd.append("\t\t\t").append("$.messager.progress('close');").append("\r\n");
        bufUpd.append("\t\t").append("}").append("\r\n");
        bufUpd.append("\t").append("});").append("\r\n");
        bufUpd.append("\t").append((new StringBuilder("$('#update")).append(className).append("Form').form({").toString()).append("\r\n");
        bufUpd.append("\t\t").append("url : '<?php echo Yii::app()->baseUrl ?>/index.php/Dynamic/DCDirectly',").append("\r\n");
        bufUpd.append("\t\t").append("onSubmit : function(param) {").append("\r\n");
        bufUpd.append("\t\t\t").append("$.messager.progress({title : '提示',text : '数据处理中，请稍后....'});").append("\r\n");
        bufUpd.append("\t\t\t").append((new StringBuilder("param.serviceUrl = '")).append(className.toLowerCase()).append("/update';").toString()).append("\r\n");
        if (dateFields.size() > 0) {
            bufUpd.append("\t\t\t").append((new StringBuilder("var json = getFormJsonData(\"update")).append(className).append("Form\");").toString()).append("\r\n");
            String df;
            for (Iterator iterator3 = dateFields.iterator(); iterator3.hasNext(); bufUpd.append("\t\t\t")
                    .append((new StringBuilder("json.")).append(df).append(" = $('#").append(df).append("').datebox('getValue')+' 00:00:00';").toString()).append("\r\n"))
                df = (String) iterator3.next();
            bufUpd.append("\t\t\t").append("param.jsonStr = JSON.stringify(json);;").append("\r\n");
        } else {
            bufUpd.append("\t\t\t").append((new StringBuilder("param.jsonStr = getFormData(\"update")).append(className).append("Form\");").toString()).append("\r\n");
        }
        bufUpd.append("\t\t\t").append("var isValid = $(this).form('validate');").append("\r\n");
        bufUpd.append("\t\t\t").append("if (!isValid) {").append("\r\n");
        bufUpd.append("\t\t\t\t").append("$.messager.progress('close');").append("\r\n");
        bufUpd.append("\t\t\t").append("}").append("\r\n");
        bufUpd.append("\t\t\t").append("return isValid;").append("\r\n");
        bufUpd.append("\t\t").append("},").append("\r\n");
        bufUpd.append("\t\t").append("success : function(result) {").append("\r\n");
        bufUpd.append("\t\t\t").append("$.messager.progress('close');").append("\r\n");
        bufUpd.append("\t\t\t").append("var res = $.parseJSON(result);").append("\r\n");
        bufUpd.append("\t\t\t").append("if (res.code == 0) {").append("\r\n");
        bufUpd.append("\t\t\t\t").append("$.modalDialog.dataGrid.datagrid('reload');").append("\r\n");
        bufUpd.append("\t\t\t\t").append("$.modalDialog.dataGrid.datagrid('clearSelections');").append("\r\n");
        bufUpd.append("\t\t\t\t").append("$.modalDialog.handler.unique.dialog('close');").append("\r\n");
        bufUpd.append("\t\t\t").append("}else{").append("\r\n");
        bufUpd.append("\t\t\t\t").append("$.messager.alert('错误', res.msg, 'error');").append("\r\n");
        bufUpd.append("\t\t\t").append("}").append("\r\n");
        bufUpd.append("\t\t").append("}").append("\r\n");
        bufUpd.append("\t").append("});").append("\r\n");
        bufUpd.append("});").append("\r\n").append("\r\n");
        bufUpd.append("</script>").append("\r\n");
        resMap.put((new StringBuilder(String.valueOf(viewPath))).append(File.separator).append("update").append(className).append(".php").toString(), bufUpd.toString());
    }

    private static List transToList(Map result) {
        List list = new ArrayList();
        list.addAll(result.keySet());
        return list;
    }
}