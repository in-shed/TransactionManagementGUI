package logic;

import database.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.TransactionType;
import model.AccountType;


/**
 * Klass som innefattar bankens logik.
 *
 */
public class BankLogic implements Serializable, AutoCloseable {

    /** Konstruktor för bankens logik */
    private static final Logger LOGGER =
            Logger.getLogger(BankLogic.class.getName());

    private final Connection connection;
    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public BankLogic() throws SQLException {
        this.connection = Database.connect();
        this.customerDAO    = new CustomerDAO(connection);
        this.accountDAO     = new AccountDAO(connection);
        this.transactionDAO = new TransactionDAO(connection);
    }

    /**
     * Hämtar info om alla kunder.
     *
     * @return customerInfo lista med info om alla kunder.
     */
    public List<String> getAllCustomers() {
        List<String> info = new ArrayList<>();
        for (Customer c : customerDAO.getAllCustomers()) {
            info.add(c.toString());
        }
        return info;
    }

    /**
     * Skapar en ny kund.
     *
     * @param name kundens förnamn
     * @param surname kundens efternamn
     * @param pNo kundens personnummer
     * @return true om kunden skapades, annars false. Kunder kan inte skapas med samma personnummer.
     */
        public boolean createCustomer(String name, String surname, String pNo) {

            if (customerDAO.exists(pNo)) {
            return false;
            }

        return customerDAO.save(new Customer(name, surname, pNo));
    }

    /**
     * Hämtar en kund baserat på personnummer.
     *
     * @param pNo kundens personnummer
     * @return customerInfo lista med info om kunden och dess konton, null om kunden inte finns.
     */
    public List<String> getCustomer(String pNo) {
        Customer customer = customerDAO.findByPNo(pNo);
        if (customer == null) {
            return null;
        }

        List<String> info = new ArrayList<>();
        info.add(customer.toString());

        for (AccountDAO.AccountRecord acc : accountDAO.findByPNo(pNo)) {
            info.add(acc.toString());
        }
        return info;
    }

    /**
     * Ändrar en kunds namn.
     *
     * @param name kundens nya förnamn
     * @param surname kundens nya efternamn
     * @param pNo kundens personnummer
     * @return true om antingen förnamn eller efternamn ändrades, annars false.
     */
    public boolean changeCustomerName(String name, String surname, String pNo) {
        Customer customer = customerDAO.findByPNo(pNo);
        if (customer == null) {
            return false;
        }

        String newFirst = name.isEmpty()    ? customer.getName()    : name;
        String newLast  = surname.isEmpty() ? customer.getSurname() : surname;

        if (name.isEmpty() && surname.isEmpty()) {
            return false;
        }

        return customerDAO.updateName(pNo, newFirst, newLast);
    }

    /**
     * Tar bort en kund och dess konton.
     *
     * @param pNo kundens personnummer
     * @return customerInfo lista med info om kunden och dess konton, null om kunden inte finns.
     */
    public List<String> deleteCustomer(String pNo) {
        Customer customer = customerDAO.findByPNo(pNo);
        if (customer == null) {
            return null;
        }

        // Gather info before deletion (cascade removes accounts + transactions)
        List<String> info = new ArrayList<>();
        info.add(customer.toString());

        for (AccountDAO.AccountRecord acc : accountDAO.findByPNo(pNo)) {
            info.add(acc.toString());
        }

        customerDAO.delete(pNo);   // cascade handles the rest
        return info;
    }

    /**
     * Skapar ett sparkonto åt en kund.
     *
     * @param pNo kundens personnummer
     * @return accountId det nya kontots id, -1 om kunden inte finns.
     */
    public int createSavingsAccount(String pNo) {
        if (customerDAO.findByPNo(pNo) == null) {
            return -1;
        }
        return accountDAO.save(pNo, AccountType.SAVINGS, BigDecimal.ZERO);
    }

    /**
     * Skapar ett kreditkonto åt en kund.
     *
     * @param pNo kundens personnummer
     * @return accountId det nya kontots id, -1 om kunden inte finns.
     */
    public int createCreditAccount(String pNo) {
        if (customerDAO.findByPNo(pNo) == null) {
            return -1;
        }
        return accountDAO.save(pNo, AccountType.CREDIT, BigDecimal.ZERO);
    }




    /**
     * Hämtar ett kontos info.
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @return account kontots info, null om kunden eller kontot inte finns.
     */
    public String getAccount(String pNo, int accountId) {
        AccountDAO.AccountRecord acc = getOwnedAccount(pNo, accountId);
        return (acc != null) ? acc.toString() : null;
    }

    /**
     * Sätter in pengar på ett konto.
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @param amount belopp att sätta in
     * @return true om insättningen lyckades, annars false.
     */
    public boolean deposit(String pNo, int accountId, int amount) {

        if (amount <= 0) {
            return false;
        }

        AccountDAO.AccountRecord acc = getOwnedAccount(pNo, accountId);
        if (acc == null) {
            return false;
        }

        BigDecimal newBalance = acc.getBalance().add(BigDecimal.valueOf(amount));

        return executeInTransaction(() -> {
            accountDAO.updateBalance(accountId, newBalance);
            transactionDAO.save(accountId,
                    BigDecimal.valueOf(amount),
                    TransactionType.DEPOSIT,
                    "Deposit");
            return true;
        });
    }

    /**
     * Tar ut pengar från ett konto.
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @param amount belopp att ta ut
     * @return true om uttaget lyckades, annars false.
     */
    public boolean withdraw(String pNo, int accountId, int amount) {

        if (amount <= 0) {
            return false;
        }

        AccountDAO.AccountRecord acc = getOwnedAccount(pNo, accountId);
        if (acc == null) {
            return false;
        }

        BigDecimal amountBD = BigDecimal.valueOf(amount);
        BigDecimal newBalance = acc.getBalance().subtract(amountBD);

        // Credit accounts may go negative, savings may not
        if (AccountType.CREDIT == acc.getAccountType()
            && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        return executeInTransaction(() -> {
            accountDAO.updateBalance(accountId, newBalance);
            transactionDAO.save(accountId,
                    amountBD.negate(),
                    TransactionType.WITHDRAWAL,
                    "Withdrawal");
            return true;
        });
    }

    /**
     * Stänger ett konto.
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @return info om kontot vid avslut, null om kunden eller kontot inte finns.
     */
    public String closeAccount(String pNo, int accountId) {

        AccountDAO.AccountRecord acc = getOwnedAccount(pNo, accountId);
        if (acc == null) {
            return null;
        }

        BigDecimal balance = acc.getBalance();

        // Calculate interest on savings (example: 1.0 %)
        if (AccountType.SAVINGS == acc.getAccountType()
            && balance.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal interest = balance
                    .multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);

            executeInTransaction(() -> {
                transactionDAO.save(accountId, interest,
                        TransactionType.INTEREST, "Interest on closure");
                return null;
            });

            balance = balance.add(interest);
        }

        BigDecimal finalBalance = balance;
        executeInTransaction(() -> {
            transactionDAO.save(accountId, finalBalance,
                    TransactionType.ACCOUNT_CLOSED, "Account closed");
            return null;
        });

        accountDAO.delete(accountId);

        return String.format(
                "Account %d (%s) closed. Final balance: %s",
                accountId, acc.getAccountType(), balance.toPlainString());
    }

    /**
     * Hämtar en förteckning över transaktionerna för ett konto
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @return arraylist med kontohistoriken, null om kontot inte finns.
     */
    public List<String> getTransactions(String pNo, int accountId) {

        AccountDAO.AccountRecord acc = getOwnedAccount(pNo, accountId);
        if (acc == null) {
            return null;
        }

        List<String> history = new ArrayList<>();
        for (TransactionDAO.TransactionRecord t
                : transactionDAO.findByAccountId(accountId)) {
            history.add(t.toString());
        }
        return history;
    }

    /**
     * Verifierar att ett konto existerar OCH tillhör den givna kunden.
     */
    private AccountDAO.AccountRecord getOwnedAccount(String pNo, int accountId) {

        AccountDAO.AccountRecord acc = accountDAO.findById(accountId);
        if (acc == null) {
            return null;
        }
        if (!acc.getpNo().equals(pNo)) {
            LOGGER.warning(
                    "Account " + accountId + " does not belong to pNo=" + pNo);
            return null;
        }
        return acc;
    }

    /**
     * Hämtar en lista med info om alla kunder.
     * (Används av GUI för att populera JList)
     *
     * @return customerInfoArray array med info om alla kunder.
     */
    public String[] getCustomerInfoArray() {
        List<String> customerInfoList = new ArrayList<>();
        for (Customer customer : customerDAO.getAllCustomers()) {
            customerInfoList.add(customer.toString());
        }
        return customerInfoList.toArray(new String[0]);
    }

    /**
     * Hämtar en kund baserat på index i den sorterade databaslistan.
     *
     * @param index index för kunden
     * @return customer om hittad, annars null.
     */
    public Customer getCustomerByIndex(int index) {
        List<Customer> customers = customerDAO.getAllCustomers();
        if (index >= 0 && index < customers.size()) {
            return customers.get(index);
        }
        return null;
    }

    /**
     * Hämtar kontots id baserat på personnummer och index.
     * (Kontona sorteras av databasen baserat på account_id)
     *
     * @param pNo personnummer
     * @param index index för kontot
     * @return kontots id som sträng, null om kunden eller indexet inte finns.
     */
    public String getAccountIdBypNoAndIndex(String pNo, int index) {
        List<AccountDAO.AccountRecord> accounts = accountDAO.findByPNo(pNo);
        if (accounts.isEmpty()) {
            return null;
        }
        if (index >= 0 && index < accounts.size()) {
            return String.valueOf(accounts.get(index).getAccountId());
        }
        return null;
    }

    /**
     * Hämtar kundens index i den sorterade kundlistan baserat på personnummer.
     *
     * @param pNo personnummer.
     * @return index för kunden, -1 om kunden inte finns.
     */
    public int findCustomerIndexBypNo(String pNo) {
        List<Customer> customers = customerDAO.getAllCustomers();
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getpNo().equals(pNo)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Hämtar en array med info om alla konton för en specifik kund.
     * (Används av GUI för att populera JList)
     *
     * @param customerIndex kundens index.
     * @return array med info om kundens konton.
     */
    public String[] getAccountInfoArray(int customerIndex) {
        Customer customer = getCustomerByIndex(customerIndex);
        if (customer == null) {
            return new String[0];
        }

        String pNo = customer.getpNo();
        List<AccountDAO.AccountRecord> accounts = accountDAO.findByPNo(pNo);
        List<String> accountStrings = new ArrayList<>();

        for (AccountDAO.AccountRecord acc : accounts) {
            accountStrings.add(acc.toString());
        }
        return accountStrings.toArray(new String[0]);
    }

    /**
     * Runs a unit of work inside a database transaction with
     * commit / rollback semantics.
     */
    private <T> T executeInTransaction(TransactionCallback<T> callback) {

        try {
            connection.setAutoCommit(false);

            T result = callback.execute();

            connection.commit();
            connection.setAutoCommit(true);
            return result;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                LOGGER.log(Level.SEVERE,
                        "Rollback failed", rollbackEx);
            }
            try {
                connection.setAutoCommit(true);
            } catch (SQLException autoCommitEx) {
                LOGGER.log(Level.SEVERE,
                        "Failed to reset auto-commit", autoCommitEx);
            }

            LOGGER.log(Level.SEVERE, "Transaction failed, rolled back", e);
            throw new DatabaseException("Transaction failed", e);
        }
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
    }




    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to close connection", e);
        }
    }
}



