package dbms.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Row implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Value> values = new ArrayList<>();

    public List<Value> getValues() { return values; }
    public void add(Value v) { values.add(v); }
    public Value get(int i) { return values.get(i); }
    public int size() { return values.size(); }
}