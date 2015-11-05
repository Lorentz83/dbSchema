package edu.purdue.dbSchema.parser;

import java.util.Objects;

/**
 * Is an immutable object containing a pair of strings.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class StringPair {

    public StringPair(String first, String second) {
        this.first = first;
        this.second = second;
    }
    public final String first;
    public final String second;

    @Override
    public String toString() {
        return String.format("%s.%s", first, second);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.first);
        hash = 53 * hash + Objects.hashCode(this.second);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StringPair other = (StringPair) obj;
        if (!Objects.equals(this.first, other.first)) {
            return false;
        }
        if (!Objects.equals(this.second, other.second)) {
            return false;
        }
        return true;
    }

}
