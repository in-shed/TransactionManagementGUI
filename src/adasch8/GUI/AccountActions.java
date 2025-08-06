package adasch8.GUI;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import adasch8.logic.BankLogic;
import adasch8.logic.Customer;
import adasch8.logic.LoadSave;

/**
 * Klass som hanterar kontoåtgärder i det grafiska användargränssnittet gentemot banklogiken.
 *
 * @author Adasch-8, Adam Schedin
 */
public class AccountActions {

    private final int SELECTED_INDEX_NOT_FOUND = -1;
    private final int INVALID_ACCOUNT_ID = -1;
    private final int INVALID_AMOUNT = -1;
    private final int customerSelectedIndex;
    private final JList<String> accountList;
    private final BankLogic bankLogic;
    private final JTextArea accountOutputTextArea;

    /**
     * Konstruktor för AccountActions.
     *
     * @param bankLogic banklogik.
     * @param accountList lista med konton som är kopplad till GUI.
     * @param accountOutputTextArea textområde för systemmeddelanden.
     * @param customerSelectedIndex index för vald kund.
     */
    public AccountActions(
            BankLogic bankLogic,
            JList<String> accountList,
            JTextArea accountOutputTextArea,
            int customerSelectedIndex) {
        this.accountList = accountList;
        this.bankLogic = bankLogic;
        this.accountOutputTextArea = accountOutputTextArea;
        this.customerSelectedIndex = customerSelectedIndex;
    }

    /** Hanterar tillägg av sparkonto. */
    public void handleCreateSavingsAccount() {
        String pNo = getSelectedCustomerpNo();
        int accountId = bankLogic.createSavingsAccount(pNo);
        if (accountId != INVALID_ACCOUNT_ID) {
            accountOutputTextArea.setText("Sparkonto skapat med id: " + accountId);
            updateAccountList(customerSelectedIndex);
        } else {
            accountOutputTextArea.setText("Kunde inte skapa sparkonto.");
        }
    }

    /** Hanterar tillägg av kreditkonto. */
    public void handleCreateCreditAccount() {
        String pNo = getSelectedCustomerpNo();
        int accountId = bankLogic.createCreditAccount(pNo);
        if (accountId != INVALID_ACCOUNT_ID) {
            accountOutputTextArea.setText("Kreditkonto skapat med id: " + accountId);
            updateAccountList(customerSelectedIndex);
        } else {
            accountOutputTextArea.setText("Kunde inte skapa kreditkonto.");
        }
    }

    /** Hanterar stängning av konto. */
    public void handleCloseAccount() {
        String pNo = getSelectedCustomerpNo();
        int accountId = acquireAccountId();
        if (accountId == SELECTED_INDEX_NOT_FOUND) {
            return;
        }
        int remove = JOptionPane.showConfirmDialog(
                null,
                "Är du säker på att du vill stänga kontot med id: " + accountId + "?",
                "Bekräfta stängning av konto",
                JOptionPane.YES_NO_OPTION);
        if (remove != JOptionPane.YES_OPTION) {
            accountOutputTextArea.setText("Stängning av konto avbröts.");
            return;
        }

        String accountInfo = bankLogic.closeAccount(pNo, accountId);
        if (accountInfo != null) {
            accountOutputTextArea.setText("Kontot avstängt: " + accountInfo);

            updateAccountList(customerSelectedIndex);
        } else {
            accountOutputTextArea.setText("Kunde inte hitta konto.");
        }
    }

    /** Hanterar hämtning av transaktioner. */
    public void handleGetTransactions() {
        String pNo = getSelectedCustomerpNo();

        int accountId = acquireAccountId();
        if (accountId == SELECTED_INDEX_NOT_FOUND) {
            return;
        }
        String transactions = bankLogic.getTransactions(pNo, accountId).toString();
        accountOutputTextArea.setText(transactions);
    }

    /** Hanterar sparandet av transaktioner. */
    public void handleSaveTransactions() {
        String pNo = getSelectedCustomerpNo();
        int accountId = acquireAccountId();
        if (accountId == SELECTED_INDEX_NOT_FOUND) {
            return;
        }

        List<String> transactions = bankLogic.getTransactions(pNo, accountId);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("src/adasch8_files"));
        fileChooser.setSelectedFile(new File("transaktioner.txt"));
        fileChooser.setDialogTitle("Spara transaktioner");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile().exists()) {
                int overwrite =
                        JOptionPane.showConfirmDialog(
                                null,
                                "Filen finns redan. Vill du skriva över den?",
                                "Överskrivning",
                                JOptionPane.YES_NO_OPTION);
                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".txt")) {
                filePath += ".txt";
            }
            transactions = LoadSave.saveTransactionToFile(filePath, transactions);
            if (transactions != null) {
                accountOutputTextArea.setText("Transaktioner sparade.");
            } else {
                accountOutputTextArea.setText("Kunde inte spara transaktioner.");
            }
        }
    }

    /** Hanterar hämtning av konto. */
    public void handleGetAccount() {
        String pNo = getSelectedCustomerpNo();
        int accountId = acquireAccountId();
        if (accountId == SELECTED_INDEX_NOT_FOUND) {
            return;
        }
        String accountInfo = bankLogic.getAccount(pNo, accountId);
        accountOutputTextArea.setText(accountInfo);
    }

    /** Hanterar insättning på konto. */
    public void handleDeposit() {
        String pNo = getSelectedCustomerpNo();
        int accountId = acquireAccountId();
        if (accountId == SELECTED_INDEX_NOT_FOUND) {
            return;
        }
        int amount = promptForAmount("Ange belopp att sätta in:");
        if (amount == INVALID_AMOUNT) {
            return;
        }

        if (bankLogic.deposit(pNo, accountId, amount)) {
            accountOutputTextArea.setText("Insättningen lyckades");
            updateAccountList(customerSelectedIndex);
        } else {
            accountOutputTextArea.setText("Insättningen misslyckades");
        }
    }

    /** Hanterar uttag från konto. */
    public void handleWithdraw() {
        String pNo = getSelectedCustomerpNo();
        int accountId = acquireAccountId();
        if (accountId == SELECTED_INDEX_NOT_FOUND) {
            return;
        }
        int amount = promptForAmount("Ange belopp att ta ut:");
        if (amount == INVALID_AMOUNT) {
            return;
        }

        if (bankLogic.withdraw(pNo, accountId, amount)) {
            accountOutputTextArea.setText("Uttaget lyckades.");
            updateAccountList(customerSelectedIndex);
        } else {
            accountOutputTextArea.setText("Uttaget misslyckades.");
        }
    }

    /**
     * Uppdaterar listan med konto så att den visar de uppdaterade kontona när konton ändras.
     *
     * @param customerIndex index för vald kund.
     */
    private void updateAccountList(int customerIndex) {
        if (customerIndex == SELECTED_INDEX_NOT_FOUND) {
            return;
        }
        Customer customer = bankLogic.getCustomerByIndex(customerIndex);
        accountList.setListData(customer.getCustomerAccountInfoArray());
    }

    /**
     * Kontrollerar om ett konto är valt av användaren (den har tryckt på det).
     *
     * @return true om ett konto är valt, annars false.
     */
    private boolean isAccountSelected() {
        return accountList.getSelectedIndex() != SELECTED_INDEX_NOT_FOUND;
    }

    /**
     * Hämtar konto-id, kontrollerar först om konto är valt, om inget konto är valt kan konto-id
     * anges manuellt. Innefattar även validering.
     *
     * @return konto-id om kontot finns, alternativt -1.
     */
    private int acquireAccountId() {
        String pNo = getSelectedCustomerpNo();
        if (isAccountSelected()) {
            return Integer.parseInt(
                    bankLogic.getAccountIdBypNoAndIndex(pNo, accountList.getSelectedIndex()));
        }

        String input =
                JOptionPane.showInputDialog(
                        null, "Inget konto valt. \nAnge konto-id " + "manuellt:");
        if (input == null) {
            accountOutputTextArea.setText("Val av konto avbrutet.");
            return SELECTED_INDEX_NOT_FOUND;
        }
        int accountId;
        try {
            accountId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            accountOutputTextArea.setText("Ogiltigt konto-id, vänligen försök igen!");
            return SELECTED_INDEX_NOT_FOUND;
        }

        if (bankLogic.getAccount(pNo, accountId) != null) {
            return accountId;
        }
        accountOutputTextArea.setText(
                "Kunde inte hitta konto med detta konto-id, vänligen försök igen!");
        return SELECTED_INDEX_NOT_FOUND;
    }

    /**
     * Hämtar personnumret för vald kund.
     *
     * @return personnumret.
     */
    private String getSelectedCustomerpNo() {
        return bankLogic.getCustomerByIndex(customerSelectedIndex).getpNo();
    }

    /**
     * Hämtar mängden pengar som ska matas in.
     *
     * @param message meddelandet som visas i inmatningsdialogen.
     * @return mängden pengar, alternativt -1 om mängden är ogiltig.
     */
    private int promptForAmount(String message) {
        String input = JOptionPane.showInputDialog(message);
        if (input == null) {
            accountOutputTextArea.setText("Åtgärden avbruten");
            return INVALID_AMOUNT;
        }
        int amount;
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            accountOutputTextArea.setText("Ogiltig mängd pengar, vänligen försök igen!");
            return INVALID_AMOUNT;
        }

        if (amount <= 0) {
            accountOutputTextArea.setText("Beloppet måste vara större än noll.");
            return INVALID_AMOUNT;
        }

        return amount;
    }
}
