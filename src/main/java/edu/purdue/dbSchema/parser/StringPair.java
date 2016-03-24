package edu.purdue.dbSchema.parser;

import edu.purdue.dbSchema.utils.Pair;

/**
 * Is an immutable object containing a pair of strings.
 *
 * @author Lorenzo Bossi [lbossi@purdue.edu]
 */
public class StringPair extends Pair<String, String> {

    public StringPair(String first, String second) {
        super(first, second);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", getFirst(), getSecond());
    }

}
