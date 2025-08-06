package adasch8.logic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Klass som innefattar en kund. @Author Adasch-8, Adam Schedin */
public class Customer implements Serializable {
    private String name;
    private String surname;
    private final String pNo;
    private final List<Account> accountList;

    /**
     * Konstruktor för att skapa en kund.
     *
     * @param name kundens förnamn
     * @param surname kundens efternamn
     * @param pNo kundens personnummer
     */
    Customer(String name, String surname, String pNo) {
        this.name = name;
        this.surname = surname;
        this.pNo = pNo;
        this.accountList = new ArrayList<>();
    }


    /** Sätter kundens förnamn.
     *
     * @param name kundens förnamn
     * */
    public void setName(String name) {
        this.name = name;
    }

    /** Sätter kundens efternamn.
     *
     * @param surname kundens efternamn
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Hämtar kundens personnummer.
     *
     * @return pNo kundens personnummer.
     */
    public String getpNo() {
        return pNo;
    }

    /**
     * Hämtar info för kunden och dess konton.
     *
     * @return customerInfo info om kunden och dess konton.
     */
    public List<String> getCustomerInfo() {
        List<String> customerInfo = new ArrayList<>();
        customerInfo.add(toString());
        for (Account account : accountList) {
            customerInfo.add(account.toString());
        }
        return customerInfo;
    }

    /**
     * Hämtar information om kunden och dess konton för att kunna radera kunden.
     *
     * @return customerDeletionInfo information om kunden och dess konton.
     */
    public List<String> getCustomerDeletionInfo() {
        List<String> customerDeletionInfo = new ArrayList<>();
        customerDeletionInfo.add(toString());
        for (Account account : accountList) {
            customerDeletionInfo.add(account.getAccountClosingInfo());
        }
        return customerDeletionInfo;
    }

    /** Stänger kundens konton. */
    public void closeCustomerAccounts() {
        accountList.clear();
    }

    /**
     * Skapar ett konto åt kunden.
     *
     * @return newAccount det skapade kontot.
     */
    public SavingsAccount createSavingsAccount() {
        SavingsAccount newAccount =
                new SavingsAccount("Sparkonto", BigDecimal.ZERO, new BigDecimal("2.4"));
        accountList.add(newAccount);
        return newAccount;
    }

    /**
     * Skapar ett kreditkonto åt kunden.
     *
     * @return newAccount det skapade kontot.
     */
    public CreditAccount createCreditAccount() {
        CreditAccount newAccount =
                new CreditAccount(
                        "Kreditkonto",
                        BigDecimal.ZERO,
                        new BigDecimal("1.1"),
                        new BigDecimal("5"),
                        new BigDecimal("-5000"));
        accountList.add(newAccount);
        return newAccount;
    }

    /**
     * Hämtar ett konto bland kundens konto baserat på kontots ID.
     * @param accountId kontots ID.
     * @return account kontot som hittats, annars null om inget konto hittats.
     */
    public Account findAccount(int accountId) {
        for (Account account : accountList) {
            if (account.getAccountId().equals(String.valueOf(accountId))) {
                return account;
            }
        }

        return null;
    }

    /**
     * Tar bort ett konto från listan med kundens konton.
     *
     * @param account kontot som ska tas bort.
     * @return accountInfo information om kontot som tagits bort.
     */
    public String removeAccount(Account account) {
        String accountInfo = account.getAccountClosingInfo();
        accountList.remove(account);
        return accountInfo;
    }

    /**
     * Hämtar information om kunden, personnummer, namn och efternamn.
     *
     * @return information om kunden.
     */
    @Override
    public String toString() {
        return String.format("%s %s %s", pNo, name, surname);
    }

    /**
     * Hämtar en array med information om kundens konton
     *
     * @return array med information om kundens konton
     */
    public String[] getCustomerAccountInfoArray() {
        List<String> accountInfoList = new ArrayList<>();
        for (Account account: accountList) {
            accountInfoList.add(account.toString());
        }
        return accountInfoList.toArray(new String[0]);
    }

    /**
     * Hämtar ett kontos id baserat på dess index i kundens kontolista
     *
     * @param index kontots index
     * @return kontots id
     */
    public String getAccountIdByIndex(int index) {
        return accountList.get(index).getAccountId();
    }
}
