package dbms.engine;

import java.io.Serializable;
import java.time.LocalDate;

public class Value implements Serializable, Comparable<Value> {
    private static final long serialVersionUID = 1L;

    private final DataType type;
    private final Object value;

    public Value(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public DataType getType() { return type; }
    public Object getRaw() { return value; }
    public boolean isNull() { return value == null; }

    public int asInt() { return ((Number) value).intValue(); }
    public double asDouble() { return ((Number) value).doubleValue(); }
    public String asString() { return value.toString(); }
    public boolean asBoolean() { return (Boolean) value; }
    public LocalDate asDate() { return (LocalDate) value; }

    public static Value ofString(DataType type, String s) {
        if (s == null || s.equalsIgnoreCase("NULL")) return new Value(type, null);
        switch (type) {
            case INT:     return new Value(type, Integer.parseInt(s.trim()));
            case DOUBLE:  return new Value(type, Double.parseDouble(s.trim()));
            case STRING:  return new Value(type, s);
            case DATE:    return new Value(type, LocalDate.parse(s.trim()));
            case BOOLEAN: return new Value(type, Boolean.parseBoolean(s.trim()));
            default: throw new IllegalArgumentException("bad type");
        }
    }

    @Override
    public int compareTo(Value o) {
        if (this.isNull() && o.isNull()) return 0;
        if (this.isNull()) return -1;
        if (o.isNull()) return 1;
        switch (type) {
            case INT:     return Integer.compare(asInt(), o.asInt());
            case DOUBLE:  return Double.compare(asDouble(), o.asDouble());
            case STRING:  return asString().compareTo(o.asString());
            case DATE:    return asDate().compareTo(o.asDate());
            case BOOLEAN: return Boolean.compare(asBoolean(), o.asBoolean());
            default: return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Value)) return false;
        Value v = (Value) o;
        if (this.isNull() && v.isNull()) return true;
        if (this.isNull() || v.isNull()) return false;
        return this.value.equals(v.value);
    }

    @Override
    public int hashCode() { return value == null ? 0 : value.hashCode(); }

    @Override
    public String toString() { return value == null ? "NULL" : value.toString(); }
}