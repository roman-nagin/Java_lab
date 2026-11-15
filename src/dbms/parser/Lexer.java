package dbms.parser;

import java.util.*;

public class Lexer {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "INSERT", "INTO", "VALUES", "SELECT", "FROM", "WHERE", "UPDATE", "SET",
            "DELETE", "CREATE", "TABLE", "DROP", "INDEX", "ON", "PRIMARY", "KEY",
            "TRUE", "FALSE", "NULL", "INT", "DOUBLE", "STRING", "DATE", "BOOLEAN"
    ));

    private final String src;
    private int pos = 0;

    public Lexer(String src) { this.src = src; }

    public List<Token> tokenize() {
        List<Token> out = new ArrayList<>();
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isWhitespace(c)) { pos++; continue; }
            if (c == '\'') { out.add(readString()); continue; }
            if (Character.isDigit(c) || (c == '-' && pos + 1 < src.length()
                    && Character.isDigit(src.charAt(pos + 1)))) {
                out.add(readNumber()); continue;
            }
            if (Character.isLetter(c) || c == '_') { out.add(readWord()); continue; }
            switch (c) {
                case '(': out.add(new Token(TokenType.LPAREN, "(")); pos++; break;
                case ')': out.add(new Token(TokenType.RPAREN, ")")); pos++; break;
                case ',': out.add(new Token(TokenType.COMMA, ",")); pos++; break;
                case '*': out.add(new Token(TokenType.STAR, "*")); pos++; break;
                case ';': out.add(new Token(TokenType.SEMI, ";")); pos++; break;
                case '=': out.add(new Token(TokenType.OP, "=")); pos++; break;
                case '>':
                    if (pos + 1 < src.length() && src.charAt(pos + 1) == '=') {
                        out.add(new Token(TokenType.OP, ">=")); pos += 2;
                    } else { out.add(new Token(TokenType.OP, ">")); pos++; }
                    break;
                case '<':
                    if (pos + 1 < src.length() && src.charAt(pos + 1) == '=') {
                        out.add(new Token(TokenType.OP, "<=")); pos += 2;
                    } else { out.add(new Token(TokenType.OP, "<")); pos++; }
                    break;
                case '!':
                    if (pos + 1 < src.length() && src.charAt(pos + 1) == '=') {
                        out.add(new Token(TokenType.OP, "!=")); pos += 2;
                    } else throw new RuntimeException("Ожидалось !=");
                    break;
                default:
                    throw new RuntimeException("Неизвестный символ: " + c);
            }
        }
        out.add(new Token(TokenType.EOF, ""));
        return out;
    }

    private Token readString() {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (pos < src.length() && src.charAt(pos) != '\'') sb.append(src.charAt(pos++));
        if (pos < src.length()) pos++;
        return new Token(TokenType.STRING, sb.toString());
    }

    private Token readNumber() {
        int start = pos;
        if (src.charAt(pos) == '-') pos++;
        boolean dot = false;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isDigit(c)) pos++;
            else if (c == '.' && !dot) { dot = true; pos++; }
            else break;
        }
        return new Token(TokenType.NUMBER, src.substring(start, pos));
    }

    private Token readWord() {
        int start = pos;
        while (pos < src.length() &&
                (Character.isLetterOrDigit(src.charAt(pos)) || src.charAt(pos) == '_')) pos++;
        String word = src.substring(start, pos);
        if (KEYWORDS.contains(word.toUpperCase()))
            return new Token(TokenType.KEYWORD, word.toUpperCase());
        return new Token(TokenType.IDENT, word);
    }
}