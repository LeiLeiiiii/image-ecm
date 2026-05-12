//package com.sunyard.sunafm.mybatis;
//
//import com.baomidou.mybatisplus.annotation.DbType;
//import com.baomidou.mybatisplus.annotation.FieldFill;
//import com.baomidou.mybatisplus.annotation.IdType;
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.baomidou.mybatisplus.generator.AutoGenerator;
//import com.baomidou.mybatisplus.generator.IFill;
//import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
//import com.baomidou.mybatisplus.generator.config.GlobalConfig;
//import com.baomidou.mybatisplus.generator.config.PackageConfig;
//import com.baomidou.mybatisplus.generator.config.StrategyConfig;
//import com.baomidou.mybatisplus.generator.config.TemplateConfig;
//import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
//import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
//import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
//import com.baomidou.mybatisplus.generator.fill.Column;
//import com.baomidou.mybatisplus.generator.query.SQLQuery;
//import org.apache.ibatis.annotations.Mapper;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author zhouleibin
// * @Type
// * @Desc
// * @date 2020-08-27 20:03
// */
//public class CodeGenerator {
//
//    private static final String author = "pjw";//作者
//
//
//    private static final String url = "jdbc:mysql://172.1.3.165:3306/dev_afm?useUnicode=true&characterEncoding=UTF-8";//数据库
//    private static final String driverName = "com.mysql.cj.jdbc.Driver";//驱动
//    private static final DbType dbType = DbType.MYSQL;//驱动
//    //private static final String url = "jdbc:oracle:thin:@172.1.1.44:1521:ecm";//数据库
//    //private static final String driverName = "oracle.jdbc.driver.OracleDriver";//驱动
//    private static final String userName = "root";
//    private static final String password = "123456";
//    private static final String projectName = "afm";//项目名称
//    private static final String packageName = "com.sunyard.sunafm";//包名
//    private static final String[] tableName = new String[]{
//            "afm_server"
//    };
//
//    public static void main(String[] args) {
//        CodeGenerator g = new CodeGenerator();
//        g.generateByTables(projectName, packageName, tableName);
//    }
//
//    /**
//     * 根据表自动生成
//     *
//     * @param packageName 包名
//     * @param tableNames  表名
//     */
//    private void generateByTables(String projectName, String packageName, String... tableNames) {
//        //配置数据源
//        DataSourceConfig dataSourceConfig = getDataSourceConfig();
//        //模板配置
//        TemplateConfig templateConfig = getTemplateConfig();
//        // 策略配置
//        StrategyConfig strategyConfig = getStrategyConfig(tableNames);
//        //全局变量配置
//        GlobalConfig globalConfig = getGlobalConfig(projectName);
//        //包名配置
//        PackageConfig packageConfig = getPackageConfig(packageName);
//        //自动生成
//        autoGenerator(dataSourceConfig, strategyConfig, globalConfig, packageConfig, templateConfig);
//    }
//
//    /**
//     * 集成
//     *
//     * @param dataSourceConfig 配置数据源
//     * @param strategyConfig   策略配置
//     * @param globalConfig     全局变量配置
//     * @param packageConfig    包名配置
//     */
//    private void autoGenerator(DataSourceConfig dataSourceConfig, StrategyConfig strategyConfig,
//                               GlobalConfig globalConfig, PackageConfig packageConfig, TemplateConfig templateConfig) {
//
//        AutoGenerator template = new AutoGenerator(dataSourceConfig)
//                .strategy(strategyConfig)
//                .global(globalConfig)
//                .packageInfo(packageConfig)
//                .template(templateConfig);
//        template.execute();
//    }
//
//    /**
//     * 模板配置
//     *
//     * @return
//     */
//    private TemplateConfig getTemplateConfig() {
//        return new TemplateConfig.Builder()
//                .xml(null)
//                .controller(null)
//                .service(null)
//                .serviceImpl(null)
//                .build();
//    }
//
//    /**
//     * 全局配置
//     */
//    private GlobalConfig getGlobalConfig(String projectName) {
//        String projectPath = System.getProperty("user.dir");
//        return new GlobalConfig.Builder()
//                .disableOpenDir()
//                .outputDir(projectPath + "/" + projectName + "/src/main/java/")
//                .author(author)
//                .build();
//    }
//
//    /**
//     * 配置数据源
//     */
//    private DataSourceConfig getDataSourceConfig() {
//        return new DataSourceConfig.Builder(url, userName, password).typeConvert(new MySqlTypeConvert() {
//                    @Override
//                    public DbColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
//                        //tinyint转换成Boolean
//                        if (fieldType.toLowerCase().contains("number")) {
//                            return DbColumnType.INTEGER;
//                        }
//                        //将数据库中datetime转换成date
//                        if (fieldType.toLowerCase().contains("timestamp")) {
//                            return DbColumnType.DATE;
//                        }
//                        //将数据库中datetime转换成date
//                        if (fieldType.toLowerCase().contains("datetime")) {
//                            return DbColumnType.DATE;
//                        }
//                        //将数据库中datetime转换成date
//                        if (fieldType.toLowerCase().contains("date")) {
//                            return DbColumnType.DATE;
//                        }
//                        //将数据库中datetime转换成date
//                        if (fieldType.toLowerCase().contains("blob")) {
//                            return DbColumnType.BYTE_ARRAY;
//                        }
//                        return (DbColumnType) super.processTypeConvert(globalConfig, fieldType);
//                    }
//                })
//                .databaseQueryClass(SQLQuery.class)
//                .build();
//    }
//
//    /**
//     * 设置包名
//     */
//    private PackageConfig getPackageConfig(String packageName) {
//        return new PackageConfig.Builder()
//                .parent(packageName)
//                .entity("po")
//                .mapper("mapper")
//                .build();
//    }
//
//    /**
//     * 策略配置
//     *
//     * @param tableNames 表名
//     */
//    private StrategyConfig getStrategyConfig(String... tableNames) {
//        List<IFill> tableFills = new ArrayList<>();
//        Column createTime = new Column("create_time", FieldFill.INSERT);
//        Column updateTime = new Column("update_time", FieldFill.INSERT_UPDATE);
//        tableFills.add(createTime);
//        tableFills.add(updateTime);
//        return new StrategyConfig.Builder()
//                .enableCapitalMode()
//                .addInclude(tableNames)
//
//                .entityBuilder()
//                .idType(IdType.ASSIGN_ID)
//                .enableFileOverride()
//                .enableLombok()
//                .naming(NamingStrategy.underline_to_camel)
//                .logicDeleteColumnName("is_deleted")
//                .addTableFills(tableFills)
//                .columnNaming(NamingStrategy.underline_to_camel)
//
//                .mapperBuilder()
//                .enableFileOverride()
//                .enableBaseColumnList()
//                .superClass(BaseMapper.class)
//                .mapperAnnotation(Mapper.class)
//                .formatMapperFileName("%sMapper")
//                .build();
//    }
//
//}
