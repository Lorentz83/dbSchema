package edu.purdue.dbSchema.schema;

import java.io.Serializable;

/**
 * Represents an object name in the database. It contains code to normalize the
 * names, therefore equality is case insensitive and ignores quotes.
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class Name implements Comparable<Name>, Serializable {

    private final String _name;
    private final String _normalizedName;

    /**
     * Creates a name.
     *
     * @param name the name.
     * @throws NullPointerException if name is null.
     * @throws IllegalArgumentException if the normalized name is empty.
     */
    public Name(String name) throws NullPointerException, IllegalArgumentException {
        if (name == null) {
            throw new NullPointerException("Missing name");
        }
        _name = name;
        _normalizedName = normalize(name);

        if (_normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Empty name");
        }
    }

    @Override
    public int compareTo(Name o) {
        return _normalizedName.compareTo(o._normalizedName);
    }

    @Override
    public int hashCode() {
        return _normalizedName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return _normalizedName.equals(((Name) obj)._normalizedName);
    }

    private String normalize(String name) {
        name = name.toLowerCase();
        if (name.startsWith("'") && name.endsWith("'")) {
            name = name.substring(1, name.length() - 1);
        } else if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        return name;
    }

    @Override
    public String toString() {
        return _name;
    }

    public String normalize() {
        return _normalizedName;
    }

}
