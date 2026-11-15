package dbms.query;

import dbms.engine.*;
import java.util.*;

public class SelectQuery implements Query {
    public final String table;
    public final List<String> columns;
    public final Condition where;

    public SelectQuery(String table, List<String> columns, Condition where) {
        this.table = table; this.columns = columns; this.where = where;
    }

    @Override
    public Object execute(Database db) throws Exception {
        Table t = db.getTable(table);
        List<Integer> rows = t.selectWithIndexOrScan(where);

        List<Integer> colIdxs = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        if (columns == null) {
            for (int i = 0; i < t.getColumns().size(); i++) {
                colIdxs.add(i);
                headers.add(t.getColumns().get(i).getName());
            }
        } else {
            for (String c : columns) {
                colIdxs.add(t.getColumnIndex(c));
                headers.add(c);
            }
        }

        List<List<String>> data = new ArrayList<>();
        for (int ri : rows) {
            Row r = t.getRow(ri);
            List<String> line = new ArrayList<>();
            for (int ci : colIdxs) line.add(r.get(ci).toString());
            data.add(line);
        }
        return new QueryResult(headers, data);
    }

    public static class QueryResult {
        public final List<String> headers;
        public final List<List<String>> rows;
        public QueryResult(List<String> headers, List<List<String>> rows) {
            this.headers = headers; this.rows = rows;
        }
    }
}