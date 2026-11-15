package dbms.engine;

import java.io.*;
import java.util.*;

public class Database {
    private final File dir;
    private final Map<String, Table> tables = new LinkedHashMap<>();

    public Database(String dirPath) {
        this.dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        loadAll();
    }

    public Collection<Table> getTables() { return tables.values(); }

    public Table getTable(String name) {
        Table t = tables.get(name.toLowerCase());
        if (t == null) throw new IllegalArgumentException("Нет таблицы: " + name);
        return t;
    }

    public boolean exists(String name) { return tables.containsKey(name.toLowerCase()); }

    public void createTable(Table t) throws IOException {
        tables.put(t.getName().toLowerCase(), t);
        t.save(dir);
    }

    public void dropTable(String name) throws IOException {
        Table t = tables.remove(name.toLowerCase());
        if (t == null) return;
        File f = new File(dir, t.getName() + ".tbl");
        if (f.exists()) f.delete();
    }

    public void persist(Table t) throws IOException { t.save(dir); }

    private void loadAll() {
        File[] files = dir.listFiles((d, n) -> n.endsWith(".tbl"));
        if (files == null) return;
        for (File f : files) {
            try {
                Table t = Table.load(f);
                tables.put(t.getName().toLowerCase(), t);
            } catch (Exception e) {
                System.err.println("Не удалось загрузить " + f + ": " + e.getMessage());
            }
        }
    }
}