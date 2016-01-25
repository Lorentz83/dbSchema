package edu.purdue.dbSchema.erros;

/**
 * Thrown when an authorization error occurs. For example when a user is trying
 * to update a column without having the permission.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class UnauthorizedSqlException extends Exception {

    public UnauthorizedSqlException(String msg) {
        super(msg);
    }

    public UnauthorizedSqlException(String format, Object... args) {
        super(String.format(format, (Object[]) args));
    }
}
