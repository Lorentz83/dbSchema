package edu.purdue.dbSchema.erros;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlParseException extends Exception {

    public SqlParseException(String msg) {
        super(msg);
    }

    public SqlParseException(String format, String... args) {
        super(String.format(format, (Object[]) args));
    }
}
