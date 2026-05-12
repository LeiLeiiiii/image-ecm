package com.sunyard.ecm.config;

import lombok.extern.slf4j.Slf4j;
import tech.spiro.addrparser.common.RegionDTO;
import tech.spiro.addrparser.common.RegionLevel;
import tech.spiro.addrparser.io.RegionDataInput;
import tech.spiro.addrparser.io.RegionDataReport;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Oracle 兼容的关系型数据库区域数据输入实现。
 * <p>
 * 由于第三方库 RdbmsRegionDataInput 内部生成的 SQL 中使用了 level 字段名，
 * 而 level 是 Oracle 的保留关键字（用于层次查询伪列），在没有 CONNECT BY 子句时
 * 会导致 ORA-01788 错误。本类使用双引号对 level 进行转义，使其在 Oracle 中
 * 被正确识别为列名。
 * </p>
 */
@Slf4j
public class OracleRdbmsRegionDataInput implements RegionDataInput {

    private static final String SELECT_SQL_FORMAT =
            "SELECT code, parent_code, name, \"LEVEL\", center, polyline FROM %s";

    private final DataSource ds;

    private final String sql;

    private Connection conn;

    private PreparedStatement stmt;

    private ResultSet rs;

    private final RegionDataReport report;

    private boolean initialized;

    public OracleRdbmsRegionDataInput(DataSource ds, String tableName) {
        this.report = new RegionDataReport();
        this.initialized = false;
        if (ds == null) {
            throw new IllegalArgumentException("DataSource:<ds> is null.");
        }
        if (tableName == null) {
            throw new IllegalArgumentException("<tableName> is null.");
        }
        this.ds = ds;
        this.sql = String.format(SELECT_SQL_FORMAT, tableName);
    }

    @Override
    public void init() throws IOException {
        if (initialized) {
            return;
        }
        log.debug("Initializing...sql: {}", sql);
        try {
            conn = ds.getConnection();
            log.debug("Initializing: Get connection completely.");
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            initialized = true;
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public RegionDTO read() throws IOException {
        if (!initialized) {
            throw new IllegalStateException("Have not initialized already.");
        }
        try {
            if (rs.next()) {
                RegionDTO dto = new RegionDTO();
                dto.setCode(rs.getInt(1));
                dto.setParentCode(rs.getInt(2));
                dto.setName(rs.getString(3));
                dto.setLevel(RegionLevel.values()[rs.getInt(4)]);
                dto.setCenter(rs.getString(5));
                dto.setPolyline(rs.getString(6));
                report.record(dto);
                return dto;
            }
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (!initialized) {
            throw new IllegalStateException("Have not initialized already.");
        }
        log.info(report.report());
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException ignored) {
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
