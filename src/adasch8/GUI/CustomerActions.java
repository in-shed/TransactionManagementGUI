package adasch8.GUI;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import adasch8.logic.BankLogic;

/**
 * Klass som hanterar kundåtgärder i det grafiska användargränssnittet gentemot banklogiken.
 *
 * @author Adasch-8, Adam Schedin
 */
public class CustomerActions {
    private static final int SELECTED_INDEX_NOT_FOUND = -1;

    private final BankLogic bankLogic;
    private final JList<String> customerList;
    private final JTextArea customerOutputTextArea;

    /**
     * Konstruktor för CustomerActions.
     *
     * @param bankLogic banklogik.
     * @param customerList lista med kunder som är kopplad till GUI.
     * @param customerOutputTextArea textområde för systemmeddelanden.
     */
    public CustomerActions(
            BankLogic bankLogic, JList<String> customerList, JTextArea customerOutputTextArea) {
        this.bankLogic = bankLogic;
        this.customerList = customerList;
        this.customerOutputTextArea = customerOutputTextArea;
    }

    /** Hanterar visning av kundinformation. */
    public void handleGetCustomer() {
        String pNo = acquirepNo();
        if (pNo == null) {
            return;
        }

        String customerInfo = bankLogic.getCustomer(pNo).toString();
        if (customerInfo != null) {
            customerOutputTextArea.setText("Info för kund " + customerInfo);
        } else {
            customerOutputTextArea.setText("Kunde inte hitta kund.");
        }
    }

    /** Hanterar ändring av kundens namn. */
    public void handleChangeCustomerName() {

        String pNo = acquirepNo();
        if (pNo == null) {
            return;
        }

        String newName = JOptionPane.showInputDialog("Skriv in nytt namn:");
        if (newName == null) {
            customerOutputTextArea.setText("Namnbyte avbrutet.");
        }

        String newSurname = JOptionPane.showInputDialog("Skriv in nytt efternamn");
        if (newSurname == null) {
            customerOutputTextArea.setText("Namnbyte avbrutet.");
        }
        if (bankLogic.changeCustomerName(newName, newSurname, pNo)) {
            customerOutputTextArea.setText("Kundens namn uppdaterat.");
            updateCustomerList();
        } else {
            customerOutputTextArea.setText("Kunde inte hitta kund, vänligen försök igen!");
        }
    }

    /** Hanterar borttagning av kund. */
    public void handleDeleteCustomer() {
        String pNo = acquirepNo();
        if (pNo == null) {
            return;
        }
        int remove = JOptionPane.showConfirmDialog(
                null,
                "Är du säker på att du vill ta bort denna kund?\n"
                        + "Alla konton kopplade till denna kund kommer att tas bort.",
                "Bekräfta stängning av konto",
                JOptionPane.YES_NO_OPTION);
        if (remove != JOptionPane.YES_OPTION) {
            customerOutputTextArea.setText("Borttagning av kund avbröts.");
            return;
        }
        if (bankLogic.deleteCustomer(pNo) != null) {
            customerOutputTextArea.setText("Kund borttagen.");
            updateCustomerList();
        } else {
            customerOutputTextArea.setText("Kunde inte hitta kund.");
        }

    }

    /** Hanterar tillägg av kund. */
    public void handleAddCustomer() {
        String name = JOptionPane.showInputDialog("Skriv in förnamn");
        String surname = JOptionPane.showInputDialog("Skriv in efternamn");
        String pNo = JOptionPane.showInputDialog("Skriv in personnummer");
        if (name == null || surname == null || pNo == null || pNo.isEmpty()) {
            customerOutputTextArea.setText("Ogiltig inmatning. Kunden skapades inte.");
            return;
        }

        if (bankLogic.createCustomer(name, surname, pNo)) {
            customerOutputTextArea.setText("Ny kund skapad.");
            updateCustomerList();
        } else {
            customerOutputTextArea.setText("Kund med detta personnummer existerar redan.");
        }
    }

    /**
     * Hämtar valt personnummer från kundlistan.
     *
     * @return personnummer för den valda kunden.
     */
    private String getSelectedpNo() {
        return bankLogic.getCustomerByIndex(customerList.getSelectedIndex()).getpNo();
    }

    /** Uppdaterar listan med kunder så att den visar de uppdaterade kunderna när kunder ändras. */
    private void updateCustomerList() {
        customerList.setListData(bankLogic.getCustomerInfoArray());
    }

    /**
     * Hämtar ett personnummer för en kund om kund är vald tar den från listan, om kund ej vald kan
     * personnumret matas in manuellt.
     *
     * @return personnumret, alternativt null om personnumret var ogiltigt eller inte fanns.
     */
    private String acquirepNo() {
        if (customerList.getSelectedIndex() != SELECTED_INDEX_NOT_FOUND) {
            return getSelectedpNo();
        }

        String pNo = JOptionPane.showInputDialog("Ingen kund vald.\nAnge personnummer manuellt:");
        if (pNo == null || pNo.isEmpty()) {
            customerOutputTextArea.setText("Åtgärden avbröts.");
            return null;
        }

        if (bankLogic.getCustomer(pNo) != null) {
            return pNo;
        }

        customerOutputTextArea.setText("Kunde inte hitta kund med detta personnummer.");
        return null;
    }
}
