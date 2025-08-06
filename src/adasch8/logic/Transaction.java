package adasch8.logic;

import java.io.Serializable;

/**
 * Klass för att representera en transaktion.
 *
 * @author Adasch-8, Adam Schedin
 */
public class Transaction implements Serializable {
    private final String date;
    private final String amount;
    private final String balance;

    /**
     * Konstruktor för transaktionen.
     *
     * @param date datum för transaktionen.
     * @param amount mängd pengar som transaktionen gäller.
     * @param balance saldo efter transaktionen.
     */
    public Transaction(String date, String amount, String balance) {
        this.date = date;
        this.amount = amount;
        this.balance = amount;
    }

    /**
     * Hämtar information för transaktionen i strängformat.
     *
     * @return information om transaktionen.
     */
    @Override
    public String toString() {
        return date + " " + amount + " Saldo: " + balance;
    }
}
