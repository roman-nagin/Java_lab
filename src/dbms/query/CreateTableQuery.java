package dbms.query;

import dbms.engine.*;

public class CreateTableQuery implements Query {
    public final String table;
    public final Table schema;

    public CreateTableQuery(String table, Table schema) {
        this.table = table; this.schema = schema;
    }

    @Override
    public Object execute(Database db) throws Exception {
        if (db.exists(table)) throw new RuntimeException("Таблица существует: " + table);
        db.createTable(schema);
        return "OK";
    }
}