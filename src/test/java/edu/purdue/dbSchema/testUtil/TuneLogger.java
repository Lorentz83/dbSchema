package edu.purdue.dbSchema.testUtil;

import edu.purdue.dbSchema.parser.SqlParserTest;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 *
 * @author Lorenzo Bossi <lbossi@purdue.edu>
 */
public class TuneLogger {

    public static void init() throws IOException {
        try (InputStream inputStream = SqlParserTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
    }
}
