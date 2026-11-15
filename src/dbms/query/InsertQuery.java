package dbms.query;

import dbms.engine.*;
import java.util.*;

public class InsertQuery implements Query {
    public final String table;
    public final List<String> columns;
    public final List<List<String>> rows;

    public InsertQuery(String table, List<String> columns, List<List<String>> rows) {
        this.table = table; this.columns = columns; this.rows = rows;
    }

    @Override
    public Object execute(Database db) throws Exception {
        Table t = db.getTable(table);
        int inserted = 0;
        Column pk = t.getPrimaryKey();
        int pkIdx = pk == null ? -1 : t.getColumnIndex(pk.getName());

        for (List<String> literals : rows) {
            Row r = new Row();
            for (Column c : t.getColumns()) {
                int litIdx;
                if (columns != null) {
                    litIdx = -1;
                    for (int i = 0; i < columns.size(); i++)
                        if (columns.get(i).equalsIgnoreCase(c.getName())) { litIdx = i; break; }
                } else {
                    litIdx = t.getColumnIndex(c.getName());
                    if (litIdx >= literals.size()) litIdx = -1;
                }
                if (litIdx == -1 || litIdx >= literals.size())
                    r.add(new Value(c.getType(), null));
                else
                    r.add(Value.ofString(c.getType(), literals.get(litIdx)));
            }
            if (pkIdx >= 0 && t.pkExists(r.get(pkIdx)))
                throw new RuntimeException("Нарушение уникальности PK: " + r.get(pkIdx));
            t.insert(r);
            inserted++;
        }
        db.persist(t);
        return inserted;
    }
}