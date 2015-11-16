/**
 * Provides a simple SQL parser for extracting a set of features from queries.
 * Known bugs:
 * <ul>
 * <li>parse error without any message in case of unknown (not allowed?) data
 * type (i.e. double for postgres)</li>
 * </ul>
 */
package edu.purdue.dbSchema;
