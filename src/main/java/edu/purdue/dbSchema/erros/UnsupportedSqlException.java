package edu.purdue.dbSchema.erros;

/**
 * Represents a condition when a SQL statement is valid but not understood by
 * this project.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class UnsupportedSqlException extends Exception {

    public UnsupportedSqlException(String msg) {
        super(msg);
    }

    public UnsupportedSqlException(String format, Object... args) {
        super(String.format(format, (Object[]) args));
    }
}
