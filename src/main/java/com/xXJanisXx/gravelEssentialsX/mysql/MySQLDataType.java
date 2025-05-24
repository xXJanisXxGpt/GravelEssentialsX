package com.xXJanisXx.gravelEssentialsX.mysql;

public enum MySQLDataType {
    CHAR(255),
    VARCHAR(255),
    TEXT,
    BOOLEAN,
    TINYINT(1),
    SMALLINT(6),
    INT(11),
    BIGINT(20),
    FLOAT(10, 2),
    DOUBLE(10, 2),
    DECIMAL(10, 2),
    DATE,
    TIME,
    DATETIME,
    TIMESTAMP;

    private final long size;
    private final Integer precision;

    MySQLDataType() {
        this.size = -1;
        this.precision = null;
    }

    MySQLDataType(int size) {
        this.size = size;
        this.precision = null;
    }

    MySQLDataType(int size, int precision) {
        this.size = size;
        this.precision = precision;
    }

    public long getSize() {
        return size;
    }

    public Integer getPrecision() {
        return precision;
    }

    public String toSQL() {
        String typeName = this.name().toUpperCase();

        if (this == BOOLEAN) {
            return "TINYINT(1)";
        }

        if (size < 0) {
            return typeName;
        }

        if (precision != null) {
            return typeName + "(" + this.size + "," + this.precision + ")";
        }

        return typeName + "(" + this.size + ")";
    }
}
