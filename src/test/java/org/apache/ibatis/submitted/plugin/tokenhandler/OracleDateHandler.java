/*
 * Copyright 2017 Focus Technology, Co., Ltd. All rights reserved.
 */
/**
 * 
 */
package org.apache.ibatis.submitted.plugin.tokenhandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.ibatis.type.DateTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * OracleDateHandler.java
 *
 * @author chuyifan
 */
/**
 * @author chuyifan
 */
public class OracleDateHandler extends DateTypeHandler {
    private static final String FORMAT = "yyyy-MM-dd hh:mm:ss";
    private static ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat(FORMAT);
        }
    };

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Date parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, format.get().format(parameter));
        // ps.setTimestamp(i, new Timestamp((parameter).getTime()));
    }
}
