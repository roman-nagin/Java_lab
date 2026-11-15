package dbms.engine;

import java.io.Serializable;
import java.util.*;

public class Index implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String column;
    private final TreeMap<Value, List<Integer>> map = new TreeMap<>();

    public Index(String name, String column) {
        this.name = name;
        this.column = column;
    }

    public String getName() { return name; }
    public String getColumn() { return column; }

    public void add(Value value, int rowIdx) {
        if (value.isNull()) return;
        map.computeIfAbsent(value, k -> new ArrayList<>()).add(rowIdx);
    }

    public List<Integer> lookupEq(Value value) {
        return map.getOrDefault(value, Collections.emptyList());
    }

    public List<Integer> lookupRange(Value from, boolean fromInclusive,
                                     Value to, boolean toInclusive) {
        NavigableMap<Value, List<Integer>> sub;
        if (from == null && to == null) sub = map;
        else if (from == null) sub = map.headMap(to, toInclusive);
        else if (to == null) sub = map.tailMap(from, fromInclusive);
        else sub = map.subMap(from, fromInclusive, to, toInclusive);

        List<Integer> result = new ArrayList<>();
        for (List<Integer> lst : sub.values()) result.addAll(lst);
        return result;
    }

    public void rebuild(Table table) {
        map.clear();
        int colIdx = table.getColumnIndex(column);
        for (int i = 0; i < table.rowCount(); i++) {
            Value v = table.getRow(i).get(colIdx);
            if (!v.isNull()) map.computeIfAbsent(v, k -> new ArrayList<>()).add(i);
        }
    }
}