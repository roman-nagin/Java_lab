package dbms;

import dbms.engine.Database;
import dbms.ui.Console;

public class Main {
    public static void main(String[] args) {
        Database db = new Database("data");
        new Console(db).run();
    }
}