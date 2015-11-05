package edu.purdue.dbSchema.erros;

/**
 * Indicates a parser error. Typically thrown when a string cannot be parsed as
 * valid SQL for the specified dialect.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class SqlParseException extends Exception {

    public SqlParseException(String msg) {
        super(msg);
    }

    public SqlParseException(String format, String... args) {
        super(String.format(format, (Object[]) args));
    }
}
