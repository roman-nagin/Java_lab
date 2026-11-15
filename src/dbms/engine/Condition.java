package dbms.engine;

public class Condition {
    public enum Op { EQ, NEQ, LT, GT, LE, GE }

    public final String column;
    public final Op op;
    public Value value;

    public Condition(String column, Op op, Value value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    public boolean matches(Value v) {
        int cmp = v.compareTo(value);
        switch (op) {
            case EQ:  return v.equals(value);
            case NEQ: return !v.equals(value);
            case LT:  return cmp < 0;
            case GT:  return cmp > 0;
            case LE:  return cmp <= 0;
            case GE:  return cmp >= 0;
            default: return false;
        }
    }
}