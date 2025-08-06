package adasch8.logic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Abstrakt klass som innefattar ett konto.
 *
 * @author Adasch-8, Adam Schedin
 */
public abstract class Account implements Serializable {
    private final String accountId;
    private final String accountType;
    private BigDecimal balance;
    private final BigDecimal interest;
    private final List<Transaction> TransactionHistory = new ArrayList<>();

    private static int lastAssignedId = 1000;

    /**
     * Konstruktor för ett konto
     *
     * @param accountType kontotyp
     * @param balance saldo
     * @param interest ränta
     */
    Account(String accountType, BigDecimal balance, BigDecimal interest) {
        lastAssignedId++;
        this.accountId = String.valueOf(lastAssignedId);
        this.accountType = accountType;
        this.balance = balance;
        this.interest = interest;
    }

    /**
     * Beräknar och formaterar räntan.
     *
     * @return räntan i formaterad sträng.
     */
    public abstract String calculateAndFormatInterest();

    /**
     * Kontrollerar om ett belopp kan sättas in på kontot.
     *
     * @param amount beloppet som ska sättas in.
     * @return true om beloppet kan sättas in, annars false.
     */
    public abstract Boolean canWithdrawAmount(BigDecimal amount);

    /**
     * Hämtar kontots id.
     *
     * @return accountId kontots id.
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Hämtar senast tilldelade konto-id.
     *
     * @return det senast tilldelade konto-id.
     */
    public static int getLastAssignedId() {
        return lastAssignedId;
    }

    /** Sätter det senast tilldelade kontonumret. */
    public static void setLastAssignedID(int newLastAssignedId) {
        lastAssignedId = newLastAssignedId;
    }

    /**
     * Hämtar kontots typ.
     *
     * @return accountType kontots typ.
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Hämtar kontots saldo.
     *
     * @return balance kontots saldo.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Hämtar kontots ränta
     *
     * @return interest kontots ränta.
     */
    public BigDecimal getInterest() {
        return interest;
    }

    /**
     * Hämtar kontots transaktionshistorik
     *
     * @return TransactionHistory kontots transaktionshistorik
     */
    public List<String> getTransactionHistory() {
        List<String> history = new ArrayList<>();
        for (Transaction transaction : TransactionHistory) {
            history.add(transaction.toString());
        }
        return history;
    }

    /**
     * Hämtar info om kontot vid avslut.
     *
     * @return info om kontot vid avslut.
     */
    public String getAccountClosingInfo() {
        return String.format(
                "%s %s %s %s",
                getAccountId(),
                formatNumber(getBalance()),
                getAccountType(),
                calculateAndFormatInterest());
    }

    /**
     * Hämtar och formaterar räntan.
     *
     * @return räntan i formaterad sträng.
     */
    protected String getFormattedInterest(BigDecimal interest) {
        NumberFormat percentFormat =
                NumberFormat.getPercentInstance(Locale.forLanguageTag("sv-SE"));
        percentFormat.setMaximumFractionDigits(1);
        return percentFormat.format(interest.movePointLeft(2));
    }

    /**
     * Hämtar och formaterar ett numeriskt värde.
     *
     * @param balance det numeriska värdet som ska formateras
     * @return saldot i formaterad sträng.
     */
    protected String formatNumber(BigDecimal balance) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("sv-SE")).format(balance);
    }

    /**
     * Hämtar och formaterar den nuvarande tiden
     *
     * @return den nuvarande formaterade tiden
     */
    protected String getTimeNow() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime date = LocalDateTime.now();
        return date.format(formatter);
    }

    /**
     * Ändrar kontots saldo (och lägger till en ny transaktion).
     *
     * @param balanceChange ändrat saldo.
     */
    public void changeBalance(BigDecimal balanceChange) {
        balance = balance.add(balanceChange);
        TransactionHistory.add(new Transaction(getTimeNow(), formatNumber(balanceChange), formatNumber(balance)));

    }
}
