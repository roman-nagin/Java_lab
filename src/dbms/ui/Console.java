package dbms.ui;

import dbms.engine.*;
import dbms.parser.QueryParser;
import dbms.query.*;
import java.util.*;

public class Console {
    private final Database db;

    public Console(Database db) { this.db = db; }

    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Мини-СУБД. HELP — команды, EXIT — выход ===");
        while (true) {
            System.out.print("sql> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("QUIT")) break;
            if (line.equalsIgnoreCase("HELP")) { printHelp(); continue; }
            try {
                execute(line);
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }

    public void execute(String sql) throws Exception {
        Query q = new QueryParser(sql).parse();

        if (q instanceof SelectQuery) {
            SelectQuery s = (SelectQuery) q;
            fixCondition(s.where, s.table);
        } else if (q instanceof UpdateQuery) {
            UpdateQuery u = (UpdateQuery) q;
            fixCondition(u.where, u.table);
        } else if (q instanceof DeleteQuery) {
            DeleteQuery d = (DeleteQuery) q;
            fixCondition(d.where, d.table);
        }

        Object res = q.execute(db);
        if (res instanceof SelectQuery.QueryResult) {
            printTable((SelectQuery.QueryResult) res);
        } else if (res instanceof Integer) {
            System.out.println("Затронуто строк: " + res);
        } else {
            System.out.println(res);
        }
    }

    private void fixCondition(Condition cond, String table) {
        if (!(cond instanceof QueryParser.LazyCondition)) return;
        QueryParser.LazyCondition lc = (QueryParser.LazyCondition) cond;
        try {
            Table t = db.getTable(table);
            int colIdx = t.getColumnIndex(lc.column);
            DataType type = t.getColumns().get(colIdx).getType();
            cond.value = Value.ofString(type, lc.rawValue);
        } catch (Exception e) {
            System.err.println("Не удалось определить тип условия: " + e.getMessage());
        }
    }

    private void printTable(SelectQuery.QueryResult r) {
        int n = r.headers.size();
        int[] w = new int[n];
        for (int i = 0; i < n; i++) w[i] = r.headers.get(i).length();
        for (List<String> row : r.rows)
            for (int i = 0; i < n; i++) w[i] = Math.max(w[i], row.get(i).length());

        StringBuilder sep = new StringBuilder("+");
        for (int x : w) {
            for (int j = 0; j < x + 2; j++) sep.append('-');
            sep.append('+');
        }
        String sepLine = sep.toString();

        System.out.println(sepLine);
        System.out.print("|");
        for (int i = 0; i < n; i++)
            System.out.printf(" %-" + w[i] + "s |", r.headers.get(i));
        System.out.println();
        System.out.println(sepLine);
        for (List<String> row : r.rows) {
            System.out.print("|");
            for (int i = 0; i < n; i++)
                System.out.printf(" %-" + w[i] + "s |", row.get(i));
            System.out.println();
        }
        System.out.println(sepLine);
        System.out.println("Строк: " + r.rows.size());
    }

    private void printHelp() {
        System.out.println(
                "CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE);\n" +
                        "INSERT INTO cars VALUES (1, 'Toyota', 25000.0);\n" +
                        "INSERT INTO cars (id, brand) VALUES (2, 'BMW');\n" +
                        "SELECT * FROM cars;\n" +
                        "SELECT brand, price FROM cars WHERE price > 20000;\n" +
                        "UPDATE cars SET price = 30000 WHERE id = 2;\n" +
                        "DELETE FROM cars WHERE brand = 'Toyota';\n" +
                        "CREATE INDEX idx_cars_brand ON cars (brand);\n" +
                        "DROP TABLE cars;\n" +
                        "EXIT"
        );
    }
}