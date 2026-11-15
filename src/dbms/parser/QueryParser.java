package dbms.parser;

import dbms.engine.*;
import dbms.query.*;
import java.util.*;

public class QueryParser {
    private final List<Token> tokens;
    private int pos = 0;

    public QueryParser(String sql) {
        this.tokens = new Lexer(sql).tokenize();
    }

    private Token peek() { return tokens.get(pos); }
    private Token next() { return tokens.get(pos++); }

    private boolean match(TokenType t) {
        if (peek().type == t) { pos++; return true; }
        return false;
    }

    private boolean matchKeyword(String kw) {
        if (peek().type == TokenType.KEYWORD && peek().text.equals(kw)) {
            pos++; return true;
        }
        return false;
    }

    private Token expect(TokenType t) {
        if (peek().type != t)
            throw new RuntimeException("Ожидалось " + t + ", получено " + peek());
        return next();
    }

    private String expectIdentOrKeyword() {
        if (peek().type == TokenType.IDENT || peek().type == TokenType.KEYWORD)
            return next().text;
        throw new RuntimeException("Ожидался идентификатор, получено " + peek());
    }

    public Query parse() {
        Query q = parseStatement();
        match(TokenType.SEMI);
        return q;
    }

    private Query parseStatement() {
        if (matchKeyword("INSERT")) return parseInsert();
        if (matchKeyword("SELECT")) return parseSelect();
        if (matchKeyword("UPDATE")) return parseUpdate();
        if (matchKeyword("DELETE")) return parseDelete();
        if (matchKeyword("CREATE")) {
            if (peek().type == TokenType.KEYWORD && peek().text.equals("INDEX"))
                return parseCreateIndex();
            return parseCreateTable();
        }
        if (matchKeyword("DROP")) return parseDropTable();
        throw new RuntimeException("Неизвестный запрос: " + peek());
    }

    private Query parseInsert() {
        if (!matchKeyword("INTO")) throw new RuntimeException("Ожидалось INTO");
        String table = expectIdentOrKeyword();
        List<String> cols = null;
        if (match(TokenType.LPAREN)) {
            cols = new ArrayList<>();
            cols.add(expectIdentOrKeyword());
            while (match(TokenType.COMMA)) cols.add(expectIdentOrKeyword());
            expect(TokenType.RPAREN);
        }
        if (!matchKeyword("VALUES")) throw new RuntimeException("Ожидалось VALUES");
        List<List<String>> rows = new ArrayList<>();
        do {
            expect(TokenType.LPAREN);
            List<String> vals = new ArrayList<>();
            vals.add(readLiteral());
            while (match(TokenType.COMMA)) vals.add(readLiteral());
            expect(TokenType.RPAREN);
            rows.add(vals);
        } while (match(TokenType.COMMA));
        return new InsertQuery(table, cols, rows);
    }

    private String readLiteral() {
        Token t = next();
        if (t.type == TokenType.NUMBER || t.type == TokenType.STRING) return t.text;
        if (t.type == TokenType.KEYWORD &&
                (t.text.equals("TRUE") || t.text.equals("FALSE") || t.text.equals("NULL")))
            return t.text;
        if (t.type == TokenType.IDENT) return t.text;
        throw new RuntimeException("Ожидался литерал, получено " + t);
    }

    private Query parseSelect() {
        List<String> cols = null;
        if (peek().type == TokenType.STAR) {
            next();
        } else {
            cols = new ArrayList<>();
            cols.add(expectIdentOrKeyword());
            while (match(TokenType.COMMA)) cols.add(expectIdentOrKeyword());
        }
        if (!matchKeyword("FROM")) throw new RuntimeException("Ожидалось FROM");
        String table = expectIdentOrKeyword();
        Condition where = null;
        if (matchKeyword("WHERE")) where = parseCondition();
        return new SelectQuery(table, cols, where);
    }

    private Condition parseCondition() {
        String col = expectIdentOrKeyword();
        Token opTok = expect(TokenType.OP);
        Condition.Op op;
        switch (opTok.text) {
            case "=":  op = Condition.Op.EQ; break;
            case "!=": op = Condition.Op.NEQ; break;
            case "<":  op = Condition.Op.LT; break;
            case ">":  op = Condition.Op.GT; break;
            case "<=": op = Condition.Op.LE; break;
            case ">=": op = Condition.Op.GE; break;
            default: throw new RuntimeException("Неизвестный оператор: " + opTok.text);
        }
        String raw = readLiteral();
        return new LazyCondition(col, op, raw);
    }

    public static class LazyCondition extends Condition {
        public final String rawValue;
        public LazyCondition(String column, Op op, String rawValue) {
            super(column, op, new Value(DataType.STRING, rawValue));
            this.rawValue = rawValue;
        }
    }

    private Query parseUpdate() {
        String table = expectIdentOrKeyword();
        if (!matchKeyword("SET")) throw new RuntimeException("Ожидалось SET");
        Map<String, String> set = new LinkedHashMap<>();
        do {
            String c = expectIdentOrKeyword();
            expect(TokenType.OP);
            set.put(c, readLiteral());
        } while (match(TokenType.COMMA));
        Condition where = null;
        if (matchKeyword("WHERE")) where = parseCondition();
        return new UpdateQuery(table, set, where);
    }

    private Query parseDelete() {
        if (!matchKeyword("FROM")) throw new RuntimeException("Ожидалось FROM");
        String table = expectIdentOrKeyword();
        Condition where = null;
        if (matchKeyword("WHERE")) where = parseCondition();
        return new DeleteQuery(table, where);
    }

    private Query parseCreateTable() {
        if (!matchKeyword("TABLE")) throw new RuntimeException("Ожидалось TABLE");
        String name = expectIdentOrKeyword();
        expect(TokenType.LPAREN);
        Table schema = new Table(name);
        do {
            String colName = expectIdentOrKeyword();
            String typeName = expectIdentOrKeyword();
            boolean pk = false;
            if (matchKeyword("PRIMARY")) { matchKeyword("KEY"); pk = true; }
            schema.addColumn(new Column(colName, DataType.fromString(typeName), pk));
        } while (match(TokenType.COMMA));
        expect(TokenType.RPAREN);
        return new CreateTableQuery(name, schema);
    }

    private Query parseDropTable() {
        if (!matchKeyword("TABLE")) throw new RuntimeException("Ожидалось TABLE");
        return new DropTableQuery(expectIdentOrKeyword());
    }

    private Query parseCreateIndex() {
        next();
        String idxName = expectIdentOrKeyword();
        if (!matchKeyword("ON")) throw new RuntimeException("Ожидалось ON");
        String table = expectIdentOrKeyword();
        expect(TokenType.LPAREN);
        String col = expectIdentOrKeyword();
        expect(TokenType.RPAREN);
        return new CreateIndexQuery(idxName, table, col);
    }
}