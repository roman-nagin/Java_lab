# Лабораторная работа №2. Разработка упрощённой СУБД на Java

**Выполнил:** Нагин Роман Андреевич  
**Группа:** 25КНТ-6  
**Дата выполнения:** 15.11.2026

---

## 1. Архитектура системы

Проект реализован на Java 8+ и использует объектную модель с разделением на слои. Код соответствует принципам ООП и использует паттерн «Команда» (Command) для обработки запросов и паттерн «Репозиторий» (Repository) для работы с данными.

**Основные компоненты:**
- **`dbms.engine`** — ядро СУБД. Классы `Database`, `Table`, `Row`, `Value`, `Column`, `Index`. Отвечает за хранение данных, строгую типизацию, проверку первичных ключей и индексацию.
- **`dbms.parser`** — лексический и синтаксический анализатор (`Lexer`, `QueryParser`). Превращает входную строку с SQL-запросом в объект запроса.
- **`dbms.query`** — паттерн «Команда». Классы `InsertQuery`, `SelectQuery`, `UpdateQuery`, `DeleteQuery` и др. реализуют интерфейс `Query` и выполняют конкретные действия над базой.
- **`dbms.ui`** — консольный интерфейс (`Console`).
- **`dbms.Main`** — точка входа.

**Диаграмма классов (упрощенно):**

    Database 1──* Table 1──* Row
                      1──* Column
                      1──* Index

    Query (interface)
     ├── InsertQuery
     ├── SelectQuery
     ├── UpdateQuery
     ├── DeleteQuery
     ├── CreateTableQuery
     ├── DropTableQuery
     └── CreateIndexQuery

*Хранение данных реализовано через нативную сериализацию Java (`ObjectOutputStream`). Каждая таблица сохраняется в отдельный файл `data/{tableName}.tbl` и восстанавливается при старте программы.*

**Диаграмма последовательности (на примере INSERT):**

    User → Console: "INSERT INTO ..."
    Console → QueryParser: parse(sql)
    QueryParser → InsertQuery
    Console → InsertQuery: execute(db)
    InsertQuery → Database: getTable
    InsertQuery → Table: pkExists / insert
    InsertQuery → Table: save(dir)
    Table → File: ObjectOutputStream

---

## 2. Результаты тестирования производительности

Тест проводился на таблице из **10 000 строк** (столбцы: `id`, `brand`, `price`). Выполнялось **1000 запросов** вида `SELECT * FROM bench WHERE brand = 'brand_50';`.

| Тип выполнения | Время выполнения (1000 запросов) |
| :--- | :--- |
| Без индекса (полный скан) | **132 мс** |
| С индексом (TreeMap) | **16 мс** |
| **Ускорение** | **8.25x** |

*Вывод: реализованный индекс на основе `TreeMap` позволяет существенно сократить время поиска за счет исключения полного перебора строк таблицы. При увеличении объема данных ускорение будет расти.*

---

## 3. Инструкция пользователя

**Как запустить:**
1. Откройте проект в IntelliJ IDEA (CE или Ultimate).
2. Убедитесь, что в качестве Project SDK выбран JDK 11 или выше.
3. Запустите класс `dbms.Main` (нажмите зелёный треугольник `Run`).
4. Взаимодействие происходит в консоли (вкладка Run внизу). Введите `HELP` для просмотра списка команд, `EXIT` для выхода из программы.

**Примеры команд (минимальное подмножество SQL):**

    -- Создание таблиц
    CREATE TABLE cars (id INT PRIMARY KEY, brand STRING, price DOUBLE, in_stock BOOLEAN);
    CREATE TABLE owners (id INT PRIMARY KEY, name STRING, car_id INT, birth DATE);

    -- Вставка данных
    INSERT INTO cars VALUES (1, 'Toyota', 25000.0, TRUE);
    INSERT INTO cars (id, brand) VALUES (2, 'BMW'); -- не указанные поля будут NULL

    -- Выборка
    SELECT * FROM cars;
    SELECT brand, price FROM cars WHERE price > 20000;
    SELECT * FROM cars WHERE brand = 'Toyota';

    -- Обновление и удаление
    UPDATE cars SET price = 30000 WHERE id = 2;
    DELETE FROM cars WHERE brand = 'Toyota';

    -- Работа с индексами и таблицами
    CREATE INDEX idx_cars_brand ON cars (brand);
    DROP TABLE cars;

---

## 4. Выводы

В ходе лабораторной работы была разработана упрощённая система управления базами данных на языке Java.

- Реализован собственный парсер SQL-подобного языка (поддерживаются `INSERT`, `SELECT`, `UPDATE`, `DELETE`, `CREATE TABLE`, `DROP TABLE`, `CREATE INDEX`).
- Обеспечена поддержка всех требуемых типов данных: целое число, число с плавающей точкой, строка, дата, логическое значение.
- Реализована проверка уникальности первичного ключа с выбросом исключения при попытке вставки дубликата.
- Данные сериализуются на диск (`data/*.tbl`) и автоматически загружаются при старте программы, что обеспечивает персистентность.
- Для повышения производительности реализован механизм индексов (`TreeMap`), который ускорил точечную выборку в 8.25 раз по сравнению с полным сканированием.
- Архитектура кода структурирована с использованием принципов ООП и паттернов проектирования (Command, Repository, Iterator), что упрощает расширение функционала.

---

## 5. Список использованных источников

1. Oracle Java Documentation. URL: https://docs.oracle.com/javase/
2. Gamma E., Helm R., Johnson R., Vlissides J. Design Patterns: Elements of Reusable Object-Oriented Software. Addison-Wesley, 1994.
3. Date C.J. An Introduction to Database Systems. 8th ed. Addison-Wesley, 2003.
4. Документация IntelliJ IDEA CE. URL: https://www.jetbrains.com/help/idea/