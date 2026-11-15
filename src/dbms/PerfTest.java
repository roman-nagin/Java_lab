package dbms;

import dbms.engine.*;
import dbms.parser.QueryParser;

public class PerfTest {
    public static void main(String[] args) throws Exception {
        Database db = new Database("data_perf");
        Table t;

        try {
            t = db.getTable("bench");
            System.out.println("Таблица уже есть. Строк: " + t.rowCount());
        } catch (Exception e) {
            System.out.println("Создаем таблицу...");
            new QueryParser("CREATE TABLE bench (id INT PRIMARY KEY, brand STRING, price DOUBLE);").parse().execute(db);
            t = db.getTable("bench");

            System.out.println("Генерируем 10 000 строк напрямую (без парсера)...");
            long startIns = System.currentTimeMillis();

            for (int i = 0; i < 10_000; i++) {
                Row r = new Row();
                r.add(new Value(DataType.INT, i));
                r.add(new Value(DataType.STRING, "brand_" + (i % 100)));
                r.add(new Value(DataType.DOUBLE, i * 1.5));
                t.insert(r); // Напрямую в память
            }
            db.persist(t); // Сохраняем 1 раз в конце
            System.out.println("Вставка 10k строк заняла: " + (System.currentTimeMillis() - startIns) + " мс");
        }

        System.out.println("\n--- Тест БЕЗ индекса (1000 запросов) ---");
        long startNoIdx = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            new QueryParser("SELECT * FROM bench WHERE brand = 'brand_50';").parse().execute(db);
        }
        long timeNoIdx = System.currentTimeMillis() - startNoIdx;
        System.out.println("Время без индекса: " + timeNoIdx + " мс");

        System.out.println("\n--- Создаем индекс ---");
        new QueryParser("CREATE INDEX idx_bench_brand ON bench (brand);").parse().execute(db);

        System.out.println("\n--- Тест С индексом (1000 запросов) ---");
        long startIdx = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            new QueryParser("SELECT * FROM bench WHERE brand = 'brand_50';").parse().execute(db);
        }
        long timeIdx = System.currentTimeMillis() - startIdx;
        System.out.println("Время с индексом: " + timeIdx + " мс");

        System.out.println("\n=========================================");
        System.out.println("УСКОРЕНИЕ: " + ((double) timeNoIdx / timeIdx) + "x");
        System.out.println("=========================================");
    }
}