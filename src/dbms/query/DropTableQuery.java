package dbms.query;

import dbms.engine.Database;

public class DropTableQuery implements Query {
    public final String table;
    public DropTableQuery(String table) { this.table = table; }

    @Override
    public Object execute(Database db) throws Exception {
        db.dropTable(table);
        return "OK";
    }
}