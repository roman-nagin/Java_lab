package dbms.query;

import dbms.engine.*;

public class DeleteQuery implements Query {
    public final String table;
    public final Condition where;

    public DeleteQuery(String table, Condition where) {
        this.table = table; this.where = where;
    }

    @Override
    public Object execute(Database db) throws Exception {
        Table t = db.getTable(table);
        int n = t.deleteWhere(where);
        db.persist(t);
        return n;
    }
}