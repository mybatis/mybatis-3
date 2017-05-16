package org.apache.ibatis.mapping.Dialect;

public enum Dialect {
    mysql, mariadb, sqlite, oracle, hsqldb, postgresql, sqlserver, db2, informix, h2, sqlserver2012;

    public static Dialect of(String dialect) {
        try {
        	if(dialect==null || "".equals(dialect)) return null;
            Dialect d = Dialect.valueOf(dialect.toLowerCase());
            return d;
        } catch (IllegalArgumentException e) {
            String dialects = null;
            for (Dialect d : Dialect.values()) {
                if (dialects == null) {
                    dialects = d.toString();
                } else {
                    dialects += "," + d;
                }
            }
            throw new IllegalArgumentException("Mybatis do not support [" + dialects + "]");
        }
    }
}