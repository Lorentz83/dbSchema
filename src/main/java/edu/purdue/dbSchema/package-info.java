/**
 * Provides a simple SQL parser for extracting a set of features from queries.
 * Known bugs:
 * <ul>
 * <li>parse error without any message in case of unknown (not allowed?) data
 * type (i.e. double for postgres);</li>
 * <li>only read and write grants, currently it is impossible to distinguish
 * between update, insert and delete;</li>
 * </ul>
 */
package edu.purdue.dbSchema;
