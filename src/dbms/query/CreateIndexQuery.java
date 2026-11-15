package dbms.query;

import dbms.engine.*;

public class CreateIndexQuery implements Query {
    public final String indexName;
    public final String table;
    public final String column;

    public CreateIndexQuery(String indexName, String table, String column) {
        this.indexName = indexName; this.table = table; this.column = column;
    }

    @Override
    public Object execute(Database db) throws Exception {
        Table t = db.getTable(table);
        Index idx = new Index(indexName, column);
        idx.rebuild(t);
        t.getIndexes().put(indexName, idx);
        db.persist(t);
        return "OK";
    }
}