package adasch8.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Klass som innefattar ett sparkonto.
 *
 * @author Adasch-8, Adam Schedin
 */
public class SavingsAccount extends Account {

    private boolean hasWithdrawn = false;
    private final BigDecimal withdrawalInterest = new BigDecimal("0.02");

    /**
     * Konstruktor för att skapa ett sparkonto.
     *
     * @param accountType typ av konto
     * @param balance saldo när kontot skapas
     * @param interest ränta
     */
    SavingsAccount(String accountType, BigDecimal balance, BigDecimal interest) {
        super(accountType, balance, interest);
    }

    /**
     * Hämtar kontots uttagsränta.
     *
     * @return räntan
     */
    public BigDecimal getWithdrawalInterest() {
        return withdrawalInterest;
    }

    /**
     * Hämtar ett värde som kontrollerar om det skett uttag från kontot innan.
     *
     * @return true om det har skett uttag, annars false.
     */
    public boolean isHasWithdrawn() {
        return hasWithdrawn;
    }

    /**
     * Returnerar en sträng som innehåller kontots information.
     *
     * @return en sträng som innehåller kontots information.
     */
    @Override
    public String toString() {
        return String.format(
                "%s %s %s %s",
                getAccountId(),
                formatNumber(getBalance()),
                getAccountType(),
                getFormattedInterest(getInterest()));
    }

    /**
     * Ändrar kontots saldo.
     *
     * @param balanceChange ändrat saldo.
     */
    @Override
    public void changeBalance(BigDecimal balanceChange) {

        if (hasWithdrawn && balanceChange.compareTo(BigDecimal.ZERO) < 0) {
            balanceChange = balanceChange.add(getWithdrawalInterest().multiply(balanceChange));
        }
        super.changeBalance(balanceChange);
        if (balanceChange.compareTo(BigDecimal.ZERO) < 0) {
            hasWithdrawn = true;
        }
    }

    /**
     * Beräknar och formaterar räntan. Saldot multipliceras med räntesatsen.
     *
     * @return interest den beräknade räntan.
     */
    @Override
    public String calculateAndFormatInterest() {
        BigDecimal totalInterest =
                getBalance()
                        .multiply(
                                getInterest()
                                        .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("sv-SE"))
                .format(totalInterest);
    }

    /**
     * Kontrollerar om det går att ta ut ett belopp från kontot.
     *
     * @param amount belopp att ta ut
     * @return true om det går att ta ut beloppet, annars false.
     */
    @Override
    public Boolean canWithdrawAmount(BigDecimal amount) {
        if (isHasWithdrawn()) {
            BigDecimal amountWithInterest = amount.add(getWithdrawalInterest().multiply(amount));

            return getBalance().compareTo(amountWithInterest) >= 0;
        }
        return getBalance().compareTo(amount) >= 0;
    }
}
