package adasch8.logic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Klass som innefattar bankens logik.
 *
 * @author Adasch-8, Adam Schedin
 */
public class BankLogic implements Serializable {
    private final List<Customer> customerList;

    /** Konstruktor för bankens logik, skapar en ny lista med kunder. */
    public BankLogic() {
        customerList = new ArrayList<>();
    }

    /**
     * Hämtar info om alla kunder.
     *
     * @return customerInfo lista med info om alla kunder.
     */
    public List<String> getAllCustomers() {
        ArrayList<String> customerInfo = new ArrayList<>();
        for (Customer customer : customerList) {
            customerInfo.add(customer.toString());
        }
        return customerInfo;
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
        if (findCustomerBypNo(pNo) != null) {
            return false;
        }
        customerList.add(new Customer(name, surname, pNo));
        return true;
    }

    /**
     * Hämtar en kund baserat på personnummer.
     *
     * @param pNo kundens personnummer
     * @return customerInfo lista med info om kunden och dess konton, null om kunden inte finns.
     */
    public List<String> getCustomer(String pNo) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        return customer.getCustomerInfo();
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
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return false;
        }
        boolean nameUpdated = false;
        if (!name.isEmpty()) {
            customer.setName(name);
            nameUpdated = true;
        }
        if (!surname.isEmpty()) {
            customer.setSurname(surname);
            nameUpdated = true;
        }
        return nameUpdated;
    }

    /**
     * Skapar ett sparkonto åt en kund.
     *
     * @param pNo kundens personnummer
     * @return accountId det nya kontots id, -1 om kunden inte finns.
     */
    public int createSavingsAccount(String pNo) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return -1;
        }
        SavingsAccount newAccount = customer.createSavingsAccount();
        return Integer.parseInt(newAccount.getAccountId());
    }

    /**
     * Skapar ett kreditkonto åt en kund.
     *
     * @param pNo kundens personnummer
     * @return accountId det nya kontots id, -1 om kunden inte finns.
     */
    public int createCreditAccount(String pNo) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return -1;
        }
        CreditAccount newAccount = customer.createCreditAccount();
        return Integer.parseInt(newAccount.getAccountId());
    }

    /**
     * Hämtar en förteckning över transaktionerna för ett konto
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @return arraylist med kontohistoriken, null om kontot inte finns.
     */
    public List<String> getTransactions(String pNo, int accountId) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        Account account = customer.findAccount(accountId);
        if (isAccountInvalid(account)) {
            return null;
        }
        return account.getTransactionHistory();
    }

    /**
     * Hämtar ett kontos info.
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @return account kontots info, null om kunden eller kontot inte finns.
     */
    public String getAccount(String pNo, int accountId) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        Account account = customer.findAccount(accountId);
        if (isAccountInvalid(account)) {
            return null;
        }
        return account.toString();
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
        if (isTransactionAmountInvalid(amount)) {
            return false;
        }
        Account account = getValidAccount(pNo, accountId);
        if (account == null) {
            return false;
        }
        account.changeBalance(BigDecimal.valueOf(amount));
        return true;
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
        BigDecimal amountToWithdraw = BigDecimal.valueOf(amount);
        if (isTransactionAmountInvalid(amount)) {
            return false;
        }
        Account account = getValidAccount(pNo, accountId);
        if (account == null) {
            return false;
        }
        if (account.canWithdrawAmount(amountToWithdraw)) {
            account.changeBalance(amountToWithdraw.negate());
            return true;
        }
        return false;
    }

    /**
     * Stänger ett konto.
     *
     * @param pNo kundens personnummer
     * @param accountId kontots id
     * @return info om kontot vid avslut, null om kunden eller kontot inte finns.
     */
    public String closeAccount(String pNo, int accountId) {

        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        Account account = customer.findAccount(accountId);
        if (isAccountInvalid(account)) {
            return null;
        }
        return customer.removeAccount(account);
    }

    /**
     * Tar bort en kund och dess konton.
     *
     * @param pNo kundens personnummer
     * @return customerInfo lista med info om kunden och dess konton, null om kunden inte finns.
     */
    public List<String> deleteCustomer(String pNo) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        List<String> customerInfo = customer.getCustomerDeletionInfo();
        customer.closeCustomerAccounts();
        customerList.remove(customer);
        return customerInfo;
    }

    /**
     * Hittar en kund med hjälp av dess personnummer.
     *
     * @param pNo personnummer
     * @return customer om hittad, annars null.
     */
    private Customer findCustomerBypNo(String pNo) {
        for (Customer customer : customerList) {
            if (customer.getpNo().equals(pNo)) {
                return customer;
            }
        }
        return null;
    }

    /**
     * Hjälpmetod för att hitta ett giltigt konto.
     *
     * @param pNo personnummer.
     * @param accountId kontots id.
     * @return konto om kontot är giltigt, annars null.
     */
    private Account getValidAccount(String pNo, int accountId) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        Account account = customer.findAccount(accountId);
        if (isAccountInvalid(account)) {
            return null;
        }
        return account;
    }

    /**
     * Hjälpmetod för att kontrollerar om saldoändringen är giltig.
     *
     * @param amount mängd att kontrollera.
     * @return true om ändringen är giltig, annars false.
     */
    private boolean isTransactionAmountInvalid(int amount) {
        return amount <= 0;
    }

    /**
     * Hjälpmetod för att kontrollera om ett konto är giltigt.
     *
     * @param account konto att kontrollera
     * @return true om kontot är ogiltigt, annars false.
     */
    private boolean isAccountInvalid(Account account) {
        return account == null;
    }

    /**
     * Hjälpmetod för att kontrollera om en kund är giltig.
     *
     * @param customer kund
     * @return true om kunden är ogiltig, annars false.
     */
    private boolean isCustomerInvalid(Customer customer) {
        return customer == null;
    }

    /**
     * Hämtar en lista med info om alla kunder.
     *
     * @return customerInfoArray arraylist med info om alla kunder.
     */
    public String[] getCustomerInfoArray() {
        List<String> customerInfoList = new ArrayList<>();
        for (Customer customer : customerList) {
            customerInfoList.add(customer.toString());
        }
        return customerInfoList.toArray(new String[0]);
    }

    /**
     * Hämtar en kund baserat på index.
     *
     * @param index index för kunden
     * @return customerInfo lista med info om kunden och dess konton.
     */
    public Customer getCustomerByIndex(int index) {
        return customerList.get(index);
    }

    /**
     * Hämtar kontots id baserat på personnummer och index.
     *
     * @param pNo personnummer
     * @param index index för kontot
     * @return kontots id, null om kunden inte finns.
     */
    public String getAccountIdBypNoAndIndex(String pNo, int index) {
        Customer customer = findCustomerBypNo(pNo);
        if (isCustomerInvalid(customer)) {
            return null;
        }
        return customer.getAccountIdByIndex(index);
    }

    /**
     * Hämtar kundens index i kundlistan baserat på kundens personnummer.
     * @param pNo personnummer.
     * @return index för kunden, -1 om kunden inte finns.
     */
    public int findCustomerIndexBypNo(String pNo) {
        for (int i = 0; i < customerList.size(); i++) {
            if (customerList.get(i).getpNo().equals(pNo)) {
                return i;
            }
        }
        return -1;
    }
    }


