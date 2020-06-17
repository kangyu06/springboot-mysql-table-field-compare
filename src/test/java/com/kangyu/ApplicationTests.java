package com.kangyu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTests {

    @Autowired
    @Qualifier("primaryJdbcTemplate")
    protected JdbcTemplate jdbcTemplate1;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    protected JdbcTemplate jdbcTemplate2;

    @Before
    public void setUp() {
    }

    /**
     * 预发布环境库
     */
    public static String database01 = "ess_news_db";
    public static String database01_desc = "预发布";

    /**
     * 测试环境
     */
    public static String database02 = "ess_news_db";
    public static String database02_desc = "测试";

    /**
     * 查询表字段
     *
     * @param
     * @return void
     * @author kangyu
     * @date 15:09 2020/6/16
     */
    @Test
    public void demo01() throws Exception {
        String output = String.format("%s = %s", "joe", 35);
        System.out.println(output);
        String sql = "SELECT t1.COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS as t1 WHERE TABLE_SCHEMA = 'test' AND TABLE_NAME ='user'";
        List<String> stringList = jdbcTemplate1.queryForList(sql, String.class);
        System.out.println(stringList);
    }

    /**
     * 逻辑：
     * 查询两边数据库所有的表，先进行表对比
     * 表与表之间进行字段对比
     *
     * @return
     * @author kangyu
     * @date 15:19 2020/6/16
     */
    @Test
    public void demo02() throws Exception {
        System.out.println("hello dounion....");

        // release_ess_news_db库下的所有表
        String sql = "select table_name from information_schema.tables where table_schema= '" + database01 + "'";
        List<String> a = jdbcTemplate1.queryForList(sql, String.class);
        System.out.println(a);

        System.out.println("-----------------------------------");
        // ess_news_db库下的所有表
        sql = "select table_name from information_schema.tables where table_schema = '" + database02 + "'";
        List<String> b = jdbcTemplate2.queryForList(sql, String.class);
        System.out.println(b);

        System.out.println("-----------------------------------");
        // 表差异对比
        // Compare two arraylists – find missing elements remove all elements from second list
        List<String> copyA01 = new ArrayList<>(a);
        List<String> copyB01 = new ArrayList<>(b);
        copyA01.removeAll(b);
        copyB01.removeAll(a);
        String formatA01 = String.format(" %s 相对于 %s 数据库,多了以下几张表: %s ", database01_desc, database02_desc, copyA01);
        String formatB01 = String.format(" %s 相对于 %s 数据库,多了以下几张表: %s ", database02_desc, database01_desc, copyB01);
        System.out.println(formatA01);
        System.out.println(formatB01);

        System.out.println("-----------------------------------");
        // 查询相同的表
        // Compare two arraylists – find common elements
        List<String> copyA02 = new ArrayList<>(a);
        List<String> copyB02 = new ArrayList<>(b);
        copyA02.retainAll(copyB02);
        String commonStr = String.format(" %s , %s 数据库公共的数据表如下： %s ", database01_desc, database02_desc, copyA02);
        System.out.println(commonStr);

        System.out.println("-----------------------------------");
        System.out.println("表字段差异：");
        // 两个库 相同的表字段差异
        List<String> commonTableList = new ArrayList<String>(copyA02);
        if (commonTableList != null && commonTableList.size() > 0) {
            for (int i = 0; i < commonTableList.size(); i++) {
                String tableName = commonTableList.get(i);
                tableFiledCompare(tableName);
            }
        }
    }

    /**
     * 表字段对比
     *
     * @param tableName
     * @return void
     * @author kangyu
     * @date 17:07 2020/6/16
     */
    public void tableFiledCompare(String tableName) {
        String sql01 = "SELECT t1.COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS as t1 WHERE TABLE_SCHEMA = '" + database01 + "' AND TABLE_NAME = '" + tableName + "'";
        List<String> filedList01 = jdbcTemplate1.queryForList(sql01, String.class);
        String sql02 = "SELECT t1.COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS as t1 WHERE TABLE_SCHEMA = '" + database02 + "' AND TABLE_NAME = '" + tableName + "'";
        List<String> filedList02 = jdbcTemplate2.queryForList(sql02, String.class);
        List<String> copy01 = new ArrayList<>(filedList01);
        List<String> copy02 = new ArrayList<>(filedList02);
        copy01.removeAll(filedList02);
        copy02.removeAll(filedList01);
        if (!copy01.isEmpty()) {
            String format01 = String.format("%s 相对于 %s 数据库下的 %s 表，多了以下几个字段: %s ", database01_desc, database02_desc, tableName, copy01);
            System.out.println(format01);
        }
        if (!copy02.isEmpty()) {
            String format02 = String.format("%s 相对于 %s 数据库下的 %s 表，多了以下几个字段: %s ", database02_desc, database01_desc, tableName, copy02);
            System.out.println(format02);
        }
    }
}
