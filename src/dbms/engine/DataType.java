package dbms.engine;

public enum DataType {
    INT, DOUBLE, STRING, DATE, BOOLEAN;

    public static DataType fromString(String s) {
        switch (s.toUpperCase()) {
            case "INT": case "INTEGER": return INT;
            case "DOUBLE": case "FLOAT": case "REAL": return DOUBLE;
            case "STRING": case "TEXT": case "VARCHAR": return STRING;
            case "DATE": return DATE;
            case "BOOL": case "BOOLEAN": return BOOLEAN;
            default: throw new IllegalArgumentException("Неизвестный тип: " + s);
        }
    }
}