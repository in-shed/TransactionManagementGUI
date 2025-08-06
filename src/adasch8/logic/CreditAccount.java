package adasch8.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Klass som innefattar ett kreditkonto.
 *
 * @author Adasch-8, Adam Schedin
 */
public class CreditAccount extends Account {

    private final BigDecimal creditLimit;
    private final BigDecimal debtInterest;

    /**
     * Konstruktor för att skapa ett kreditkonto.
     *
     * @param accountType typ av konto
     * @param balance saldo när kontot skapas
     * @param interest ränta
     */
    CreditAccount(
            String accountType,
            BigDecimal balance,
            BigDecimal interest,
            BigDecimal debtInterest,
            BigDecimal creditLimit) {

        super(accountType, balance, interest);
        this.debtInterest = debtInterest;
        this.creditLimit = creditLimit;
    }

    /**
     * Hämtar kontots ränta vid skuld
     *
     * @return debtInterest kontots ränta vid skuld
     */
    public BigDecimal getDebtInterest() {
        return debtInterest;
    }

    /**
     * Hämtar kontots kreditgräns
     *
     * @return creditLimit kontots kreditgräns
     */
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    /**
     * Skriver ut information om kontot
     *
     * @return String information om kontot
     */
    @Override
    public String toString() {
        if (getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            return String.format(
                    "%s %s %s %s",
                    getAccountId(),
                    formatNumber(getBalance()),
                    getAccountType(),
                    getFormattedInterest(getInterest()));
        } else {
            return String.format(
                    "%s %s %s %s",
                    getAccountId(),
                    formatNumber(getBalance()),
                    getAccountType(),
                    getFormattedInterest(getDebtInterest()));
        }
    }

    /**
     * Beräknar och formaterar räntan. Räntan är högre om saldot är negativt, därför kollar vi
     * saldot och räknar räntan på absolutbeloppet.
     *
     * @return interest den beräknade räntan
     */
    @Override
    public String calculateAndFormatInterest() {
        BigDecimal debtInterestAmount;
        BigDecimal normalInterestAmount;
        if (getBalance().compareTo(BigDecimal.ZERO) < 0) {
            debtInterestAmount = getBalance().abs();
            normalInterestAmount = BigDecimal.ZERO;
        } else {
            debtInterestAmount = BigDecimal.ZERO;
            normalInterestAmount = getBalance();
        }
        debtInterestAmount =
                debtInterestAmount.multiply(
                        getDebtInterest().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
        normalInterestAmount =
                normalInterestAmount.multiply(
                        getInterest().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));

        BigDecimal totalInterestAmount = normalInterestAmount.subtract(debtInterestAmount);
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("sv-SE"))
                .format(totalInterestAmount);
    }

    /**
     * Kontrollerar om det går att ta ut ett belopp från kontot.
     *
     * @param amount belopp att ta ut
     * @return true om det går att ta ut beloppet, annars false
     */
    @Override
    public Boolean canWithdrawAmount(BigDecimal amount) {
        return (getBalance().subtract(amount)).compareTo(getCreditLimit()) >= 0;
    }
}
