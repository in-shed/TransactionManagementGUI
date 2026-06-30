package database;

/**
 * Unchecked exception for all database-related failures.
 * Wraps SQLException with meaningful messages so callers
 * don't need try/catch boilerplate but can still catch
 * this if they want to handle data-access errors.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}