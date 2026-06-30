package logic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import model.AccountType;

/** Klass som innefattar en kund. */
public class Customer implements Serializable {
    private String name;
    private String surname;
    private final String pNo;

    /**
     * Konstruktor för att skapa en kund.
     *
     * @param name kundens förnamn
     * @param surname kundens efternamn
     * @param pNo kundens personnummer
     */
    public Customer(String name, String surname, String pNo) {
        this.name = name;
        this.surname = surname;
        this.pNo = pNo;
    }

    /** Hämtar kundens förnamn.* */
    public String getName() {
       return this.name;
    }

    /** Hämtar kundens efternamn.* */
    public String getSurname() {
        return this.surname;
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
     * Hämtar information om kunden, personnummer, namn och efternamn.
     *
     * @return information om kunden.
     */
    @Override
    public String toString() {
        return String.format("%s %s %s", pNo, name, surname);
    }
}



