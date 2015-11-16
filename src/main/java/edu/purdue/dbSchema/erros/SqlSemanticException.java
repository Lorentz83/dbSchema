package edu.purdue.dbSchema.erros;

/**
 * Indicates an error in the SQL semantic. Like trying to add a table that
 * already exists.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class SqlSemanticException extends Exception {

    public SqlSemanticException(String msg) {
        super(msg);
    }

    public SqlSemanticException(String format, Object... args) {
        super(String.format(format, (Object[]) args));
    }
}
