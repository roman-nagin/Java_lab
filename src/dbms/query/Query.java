package dbms.query;

import dbms.engine.Database;

public interface Query {
    Object execute(Database db) throws Exception;
}