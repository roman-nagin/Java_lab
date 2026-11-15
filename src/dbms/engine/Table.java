package dbms.engine;

import java.io.*;
import java.util.*;

public class Table implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final List<Column> columns = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private final Map<String, Index> indexes = new LinkedHashMap<>();

    public Table(String name) { this.name = name; }

    public String getName() { return name; }
    public List<Column> getColumns() { return columns; }
    public List<Row> getRows() { return rows; }
    public Map<String, Index> getIndexes() { return indexes; }

    public int rowCount() { return rows.size(); }
    public Row getRow(int i) { return rows.get(i); }

    public void addColumn(Column c) { columns.add(c); }

    public int getColumnIndex(String colName) {
        for (int i = 0; i < columns.size(); i++)
            if (columns.get(i).getName().equalsIgnoreCase(colName)) return i;
        throw new IllegalArgumentException("Нет столбца: " + colName);
    }

    public Column getPrimaryKey() {
        for (Column c : columns) if (c.isPrimaryKey()) return c;
        return null;
    }

    public boolean pkExists(Value pkValue) {
        Column pk = getPrimaryKey();
        if (pk == null) return false;
        int idx = getColumnIndex(pk.getName());
        for (Row r : rows) if (r.get(idx).equals(pkValue)) return true;
        return false;
    }

    public void insert(Row row) {
        int idx = rows.size();
        rows.add(row);
        for (Index index : indexes.values()) {
            int colIdx = getColumnIndex(index.getColumn());
            index.add(row.get(colIdx), idx);
        }
    }

    public int deleteWhere(Condition cond) {
        if (cond == null) {
            int n = rows.size();
            rows.clear();
            for (Index index : indexes.values()) index.rebuild(this);
            return n;
        }
        int colIdx = getColumnIndex(cond.column);
        int removed = 0;
        Iterator<Row> it = rows.iterator();
        while (it.hasNext()) {
            if (cond.matches(it.next().get(colIdx))) { it.remove(); removed++; }
        }
        for (Index index : indexes.values()) index.rebuild(this);
        return removed;
    }

    public int updateWhere(Condition cond, Map<String, Value> updates) {
        List<Integer> targetIdxs = selectWithIndexOrScan(cond);
        for (int i : targetIdxs) {
            Row r = rows.get(i);
            for (Map.Entry<String, Value> e : updates.entrySet()) {
                int col = getColumnIndex(e.getKey());
                r.getValues().set(col, e.getValue());
            }
        }
        for (Index index : indexes.values()) index.rebuild(this);
        return targetIdxs.size();
    }

    public List<Integer> selectWithIndexOrScan(Condition cond) {
        if (cond == null) {
            List<Integer> all = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) all.add(i);
            return all;
        }
        for (Index idx : indexes.values()) {
            if (!idx.getColumn().equalsIgnoreCase(cond.column)) continue;
            switch (cond.op) {
                case EQ: return new ArrayList<>(idx.lookupEq(cond.value));
                case GT:
                case GE: return new ArrayList<>(idx.lookupRange(
                        cond.value, cond.op == Condition.Op.GE, null, true));
                case LT:
                case LE: return new ArrayList<>(idx.lookupRange(
                        null, true, cond.value, cond.op == Condition.Op.LE));
                default: break;
            }
        }
        int colIdx = getColumnIndex(cond.column);
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++)
            if (cond.matches(rows.get(i).get(colIdx))) out.add(i);
        return out;
    }

    public void save(File dir) throws IOException {
        File f = new File(dir, name + ".tbl");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(f)))) {
            oos.writeObject(this);
        }
    }

    public static Table load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (Table) ois.readObject();
        }
    }
}