package edu.purdue.dbSchema.erros;

/**
 * There is an error in the sql semantic. Like trying to add a table that
 * already exists.
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class SqlSemanticException extends Exception {

    public SqlSemanticException(String msg) {
        super(msg);
    }

    public SqlSemanticException(String format, String... args) {
        super(String.format(format, (Object[]) args));
    }
}
