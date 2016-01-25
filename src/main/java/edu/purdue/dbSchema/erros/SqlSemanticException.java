package edu.purdue.dbSchema.erros;

/**
 * Thrown when there is an error in the SQL semantic. For example it is thrown
 * if trying to add a table when another with the same name already exists; if
 * there is an ambiguous name reference or a referenced object does not exist.
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
