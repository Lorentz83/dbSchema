package edu.purdue.dbSchema.erros;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class UnauthorizedSqlException extends Exception {

    public UnauthorizedSqlException(String msg) {
        super(msg);
    }

    public UnauthorizedSqlException(String format, String... args) {
        super(String.format(format, (Object[]) args));
    }
}
