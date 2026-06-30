package database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.AccountType;

/**
 * Data Access Object for the accounts table.
 */
public class AccountDAO {

    private static final Logger LOGGER =
            Logger.getLogger(AccountDAO.class.getName());

    private final Connection connection;

    public AccountDAO(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }


    /**
     * Inserts a new account and returns the generated {@code account_id}.
     *
     * @throws DatabaseException if the insert fails or no key is generated
     */
    public int save(String pNo, AccountType accountType, BigDecimal balance) {

        String sql =
                "INSERT INTO accounts(pno, account_type, balance) " +
                "VALUES (?, ?::account_type, ?)";

        try (PreparedStatement stmt =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pNo);
            stmt.setString(2, accountType.name());
            stmt.setBigDecimal(3, balance);

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                throw new DatabaseException(
                        "Account insert affected 0 rows for pNo=" + pNo);
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    LOGGER.info("Created account id=" + id + " for pNo=" + pNo);
                    return id;
                }
                throw new DatabaseException(
                        "Account insert succeeded but no generated key returned");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to save account for pNo=" + pNo, e);
            throw new DatabaseException("Failed to save account", e);
        }
    }


    /**
     * Finds a single account by its primary key.
     *
     * @return the {@link AccountRecord}, or {@code null} if not found
     * @throws DatabaseException on SQL error
     */
    public AccountRecord findById(int accountId) {

        String sql = "SELECT * FROM accounts WHERE account_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to find account id=" + accountId, e);
            throw new DatabaseException(
                    "Failed to find account " + accountId, e);
        }

        return null;
    }

    /**
     * Returns every account belonging to a customer, ordered by id.
     *
     * @throws DatabaseException on SQL error
     */
    public List<AccountRecord> findByPNo(String pNo) {

        List<AccountRecord> accounts = new ArrayList<>();

        String sql =
                "SELECT * FROM accounts WHERE pno = ? ORDER BY account_id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, pNo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to find accounts for pNo=" + pNo, e);
            throw new DatabaseException(
                    "Failed to find accounts for customer " + pNo, e);
        }

        return accounts;
    }


    /**
     * Updates the balance of an account.
     *
     * @return {@code true} if exactly one row was updated
     * @throws DatabaseException on SQL error
     */
    public boolean updateBalance(int accountId, BigDecimal balance) {

        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setBigDecimal(1, balance);
            stmt.setInt(2, accountId);

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                LOGGER.warning(
                        "Balance update affected 0 rows for accountId="
                        + accountId);
                return false;
            }

            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to update balance for accountId=" + accountId, e);
            throw new DatabaseException(
                    "Failed to update balance for account " + accountId, e);
        }
    }


    /**
     * Deletes an account. Associated transactions are cascade-deleted.
     *
     * @return {@code true} if exactly one row was deleted
     * @throws DatabaseException on SQL error
     */
    public boolean delete(int accountId) {

        String sql = "DELETE FROM accounts WHERE account_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            int affected = stmt.executeUpdate();

            if (affected == 0) {
                LOGGER.warning(
                        "Delete affected 0 rows for accountId=" + accountId);
                return false;
            }

            LOGGER.info("Deleted account id=" + accountId);
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed to delete account id=" + accountId, e);
            throw new DatabaseException(
                    "Failed to delete account " + accountId, e);
        }
    }


    public boolean exists(int accountId) {

        String sql = "SELECT 1 FROM accounts WHERE account_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,
                    "Failed existence check for accountId=" + accountId, e);
            throw new DatabaseException(
                    "Failed to check account existence " + accountId, e);
        }
    }

    /**
     * Helper function to map a row.
     * @param rs ResultSet
     * @return AccountRecord
     * @throws SQLException
     */
    private AccountRecord mapRow(ResultSet rs) throws SQLException {
        return new AccountRecord(
                rs.getInt("account_id"),
                rs.getString("pno"),
                AccountType.valueOf(rs.getString("account_type")),
                rs.getBigDecimal("balance")
        );
    }

    /**
     * Immutable data-transfer object representing one row in
     * the accounts table.
     */
    public static final class AccountRecord {

        private final int     accountId;
        private final String  pNo;
        private final AccountType  accountType;
        private final BigDecimal balance;

        public AccountRecord(int accountId, String pNo,
                             AccountType accountType, BigDecimal balance) {
            this.accountId = accountId;
            this.pNo = pNo;
            this.accountType = accountType;
            this.balance = balance;
        }

        public int getAccountId() { return accountId;   }
        public String getpNo() { return pNo;         }
        public AccountType getAccountType() { return accountType; }
        public BigDecimal getBalance() { return balance;     }

        @Override
        public String toString() {
            return String.format(
                    "Account{id=%d, pNo=%s, type=%s, balance=%s}",
                    accountId, pNo, accountType, balance);
        }
    }
}
