package database;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.TransactionType;

/**
 * Data Access Object for the transactions table.
 */
public class TransactionDAO {

    private static final Logger LOGGER =
            Logger.getLogger(TransactionDAO.class.getName());

    private final Connection connection;

    public TransactionDAO(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }


    /**
     * Inserts a new transaction and returns the generated id.
     *
     * @throws DatabaseException on failure
     */
    public int save(int accountId, BigDecimal amount,
                    TransactionType transactionType, String description) {

        String sql =
                "INSERT INTO transactions " +
                "(account_id, amount, transaction_type, description) " +
                "VALUES (?, ?, ?::transaction_type, ?)";

        try (PreparedStatement stmt =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, accountId);
            stmt.setBigDecimal(2, amount);
            stmt.setString(3, transactionType.name());
            stmt.setString(4, description);

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new DatabaseException(
                        "Transaction insert affected 0 rows for accountId="
                        + accountId);
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new DatabaseException(
                        "Transaction insert succeeded but no key returned");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to save transaction for accountId=" + accountId, e);
            throw new DatabaseException("Failed to save transaction", e);
        }
    }


    /**
     * Returns all transactions for an account, newest first.
     *
     * @throws DatabaseException on SQL error
     */
    public List<TransactionRecord> findByAccountId(int accountId) {

        List<TransactionRecord> list = new ArrayList<>();

        String sql =
                "SELECT * FROM transactions " +
                "WHERE account_id = ? " +
                "ORDER BY transaction_time DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to find transactions for accountId=" + accountId, e);
            throw new DatabaseException(
                    "Failed to find transactions for account " + accountId, e);
        }

        return list;
    }


    private TransactionRecord mapRow(ResultSet rs) throws SQLException {

        Timestamp ts = rs.getTimestamp("transaction_time");
        LocalDateTime time = (ts != null) ? ts.toLocalDateTime() : null;

        return new TransactionRecord(
                rs.getInt("transaction_id"),
                rs.getInt("account_id"),
                rs.getBigDecimal("amount"),
                rs.getString("transaction_type"),
                rs.getString("description"),
                time
        );
    }

    /**
         * Immutable data-transfer object representing one row in
         * the <strong>transactions</strong> table.
         */
        public record TransactionRecord(int transactionId, int accountId, BigDecimal amount, String transactionType,
                                        String description, LocalDateTime transactionTime) {

        @Override
            public String toString() {
                return String.format(
                        "%s | %s | %s kr | %s",
                        transactionTime != null ? transactionTime : "N/A",
                        transactionType,
                        amount != null ? amount.toPlainString() : "0",
                        description != null ? description : "");
            }
        }
}