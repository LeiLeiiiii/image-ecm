/*
package com.sunyard.edm;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableFill;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * @author zhouleibin
 * @Type
 * @Desc
 * @date 2020-08-27 20:03
 *//*

public class CodeGenerator {

    private static final String author = "pjw";//作者


    private static final String url = "jdbc:mysql://172.1.3.165:3306/sunam_dev?useUnicode=true&characterEncoding=UTF-8";//数据库
    private static final String driverName = "com.mysql.cj.jdbc.Driver";//驱动
    private static final DbType dbType = DbType.MYSQL;//驱动
    //private static final String url = "jdbc:oracle:thin:@172.1.1.44:1521:ecm";//数据库
    //private static final String driverName = "oracle.jdbc.driver.OracleDriver";//驱动
    private static final String userName = "root";
    private static final String password = "123456";
    private static final String projectName = "/SunFile-service";//项目名称
    private static final String packageName = "com.sunyard.sunam";//包名
    private static final String[] tableName = new String[]{
            "doc_bs_document",
    };

    public static void main(String[] args) {
        CodeGenerator g = new CodeGenerator();
        g.generateByTables(projectName, packageName, tableName);
    }

    */
/**
     * 根据表自动生成
     *
     * @param packageName 包名
     * @param tableNames  表名
     *//*

    private void generateByTables(String projectName, String packageName, String... tableNames) {
        //配置数据源
        DataSourceConfig dataSourceConfig = getDataSourceConfig();
        // 策略配置
        StrategyConfig strategyConfig = getStrategyConfig(tableNames);
        //全局变量配置
        GlobalConfig globalConfig = getGlobalConfig(projectName);
        //包名配置
        PackageConfig packageConfig = getPackageConfig(packageName);
        //自动生成
        atuoGenerator(dataSourceConfig, strategyConfig, globalConfig, packageConfig);
    }

    */
/**
     * 集成
     *
     * @param dataSourceConfig 配置数据源
     * @param strategyConfig   策略配置
     * @param config           全局变量配置
     * @param packageConfig    包名配置
     *//*

    private void atuoGenerator(DataSourceConfig dataSourceConfig, StrategyConfig strategyConfig,
                               GlobalConfig config, PackageConfig packageConfig) {
        AutoGenerator autoGenerator = new AutoGenerator();
        autoGenerator.setGlobalConfig(config);
        autoGenerator.setDataSource(dataSourceConfig);
        autoGenerator.setStrategy(strategyConfig);
        autoGenerator.setPackageInfo(packageConfig);
        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        //不自动生成的代码设置null
        templateConfig.setXml(null);
        templateConfig.setController(null);
        templateConfig.setMapper(null);
        templateConfig.setService(null);
        templateConfig.setServiceImpl(null);
        autoGenerator.setTemplate(templateConfig);
        autoGenerator.setTemplateEngine(new FreemarkerTemplateEngine());
        autoGenerator.execute();
    }

    */
/**
     * 全局配置
     *//*

    private GlobalConfig getGlobalConfig(String projectName) {
        String projectPath = System.getProperty("user.dir");
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(projectPath + "/" + projectName + "/src/main/java/");//输出目录
        globalConfig.setAuthor(author);
        globalConfig.setFileOverride(true);// 是否覆盖文件
        //globalConfig.setEnableCache(false)// XML 二级缓存
        globalConfig.setBaseColumnList(true);// XML columList
        globalConfig.setBaseResultMap(true);// XML ResultMap
        globalConfig.setActiveRecord(false);// 不需要ActiveRecord特性的请改为false
        globalConfig.setOpen(false);//生成后打开文件夹
        globalConfig.setIdType(IdType.ASSIGN_ID);
        globalConfig.setSwagger2(true);
        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        //设置service名
        */
/*globalConfig.setServiceName("%sService");
           globalConfig.setXmlName("%sMapper");
        globalConfig.setMapperName("%sMapper");
        globalConfig.setControllerName("%sController");
        globalConfig.setServiceImplName("%sServiceImpl");*//*

        return globalConfig;
    }

    */
/**
     * 配置数据源
     *//*

    private DataSourceConfig getDataSourceConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setDbType(dbType);
        dataSourceConfig.setUrl(url);
        dataSourceConfig.setDriverName(driverName);
        dataSourceConfig.setUsername(userName);
        dataSourceConfig.setPassword(password);
        dataSourceConfig.setTypeConvert(new MySqlTypeConvert() {
            @Override
            public DbColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                //tinyint转换成Boolean
                if (fieldType.toLowerCase().contains("number")) {
                    return DbColumnType.INTEGER;
                }
                //将数据库中datetime转换成date
                if (fieldType.toLowerCase().contains("timestamp")) {
                    return DbColumnType.DATE;
                }
                //将数据库中datetime转换成date
                if (fieldType.toLowerCase().contains("datetime")) {
                    return DbColumnType.DATE;
                }
                //将数据库中datetime转换成date
                if (fieldType.toLowerCase().contains("date")) {
                    return DbColumnType.DATE;
                }
                //将数据库中datetime转换成date
                if (fieldType.toLowerCase().contains("blob")) {
                    return DbColumnType.BYTE_ARRAY;
                }
                return (DbColumnType) super.processTypeConvert(globalConfig, fieldType);
            }
        });
        return dataSourceConfig;
    }

    */
/**
     * 设置包名
     *//*

    private PackageConfig getPackageConfig(String packageName) {
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent(packageName);
        packageConfig.setEntity("entity");
        packageConfig.setMapper("com/sunyard/am/mapper");
        */
/*        packageConfig.setController("controller");
        packageConfig.setService("service");
        packageConfig.setServiceImpl("impl");
        packageConfig.setXml("xml");*//*


        return packageConfig;
    }

    */
/**
     * 策略配置
     *
     * @param tableNames 表名
     *//*

    private StrategyConfig getStrategyConfig(String... tableNames) {
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setCapitalMode(true);// 全局大写命名 ORACLE 注意
        //strategyConfig.setDbColumnUnderline(true)//全局下划线命名
        strategyConfig.setEntityLombokModel(true);
        strategyConfig.setRestControllerStyle(false);
        // 排除表的前缀
        // strategyConfig.setTablePrefix("T_","BS_","BP_");
        strategyConfig.setNaming(NamingStrategy.underline_to_camel);//从数据库表到文件的命名策略
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel);
        strategyConfig.setInclude(tableNames);//需要生成的的表名，多个表名传数组
        strategyConfig.setLogicDeleteFieldName("is_deleted");
        List<TableFill> tableFills = new ArrayList<>();
        TableFill createTime = new TableFill("create_time", FieldFill.INSERT);
        TableFill updateTime = new TableFill("update_time", FieldFill.INSERT_UPDATE);
        tableFills.add(createTime);
        tableFills.add(updateTime);
        strategyConfig.setTableFillList(tableFills);
        // strategyConfig.isEntityLombokModel();
        return strategyConfig;
    }

}
*/
/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2020-08-27 sunshaohong creat
 */

