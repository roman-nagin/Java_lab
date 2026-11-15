package dbms.query;

import dbms.engine.*;
import java.util.*;

public class UpdateQuery implements Query {
    public final String table;
    public final Map<String, String> setLiterals;
    public final Condition where;

    public UpdateQuery(String table, Map<String, String> setLiterals, Condition where) {
        this.table = table; this.setLiterals = setLiterals; this.where = where;
    }

    @Override
    public Object execute(Database db) throws Exception {
        Table t = db.getTable(table);
        Map<String, Value> updates = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : setLiterals.entrySet()) {
            Column c = null;
            for (Column col : t.getColumns())
                if (col.getName().equalsIgnoreCase(e.getKey())) { c = col; break; }
            if (c == null) throw new RuntimeException("Нет столбца: " + e.getKey());
            updates.put(c.getName(), Value.ofString(c.getType(), e.getValue()));
        }
        int n = t.updateWhere(where, updates);
        db.persist(t);
        return n;
    }
}