package adasch8.GUI;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import adasch8.logic.BankLogic;
import adasch8.logic.Customer;

/**
 * Klass som hanterar kontoscenen i det grafiska användargränssnittet.
 *
 * @author Adasch-8, Adam Schedin
 */
public class AccountScene {
    private static final int SELECTED_INDEX_NOT_FOUND = -1;

    /**
     * Skapar kontoscenen
     *
     * @return panelen för kundscenen.
     */
    public JPanel create(
            BankLogic bankLogic,
            JList<String> accountList,
            int customerSelectedIndex) {

        JTextArea outputTextArea = new JTextArea(5, 20);
        JPanel panel = new JPanel(new BorderLayout());

        JScrollPane accountScrollPane = new JScrollPane(accountList);


        ImageIcon accountIcon = new ImageIcon("src/adasch8/assets/Account.png");
        JLabel accountIconLabel = new JLabel(accountIcon);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(accountIconLabel);
        panel.add(topPanel, BorderLayout.NORTH);


        panel.add(accountScrollPane, BorderLayout.CENTER);

        outputTextArea.setEditable(false);
        JScrollPane accountOutputScrollPane = new JScrollPane(outputTextArea);
        panel.add(accountOutputScrollPane, BorderLayout.SOUTH);

        outputTextArea.setText("Vänligen välj ett konto.");

        AccountActions accountActions =
                new AccountActions(
                        bankLogic, accountList, outputTextArea, customerSelectedIndex);

        JPanel accountButtonPanel = new JPanel();
        topPanel.add(accountButtonPanel);
        accountButtonPanel.setLayout(new GridLayout(2, 4));

        JButton createSavingsAccountButton = new JButton("Skapa sparkonto");
        JButton createCreditAccountButton = new JButton("Skapa kreditkonto");
        JButton getTransactionsButton = new JButton("Hämta transaktioner");
        JButton saveTransactionButton = new JButton("Spara transaktioner");
        JButton getAccountButton = new JButton("Hämta konto");
        JButton depositButton = new JButton("Insättning");
        JButton withdrawButton = new JButton("Uttag");
        JButton closeAccountButton = new JButton("Stäng konto");


        if (customerSelectedIndex == SELECTED_INDEX_NOT_FOUND) {
            return panel;
        }
        Customer customer = bankLogic.getCustomerByIndex(customerSelectedIndex);
        accountList.setListData(customer.getCustomerAccountInfoArray());

        ActionListener accountActionListener =
                e -> {
                    if (e.getSource() == createSavingsAccountButton) {
                        accountActions.handleCreateSavingsAccount();
                    } else if (e.getSource() == createCreditAccountButton) {
                        accountActions.handleCreateCreditAccount();
                    } else if (e.getSource() == getTransactionsButton) {
                        accountActions.handleGetTransactions();
                    } else if (e.getSource() == getAccountButton) {
                        accountActions.handleGetAccount();
                    } else if (e.getSource() == saveTransactionButton) {
                        accountActions.handleSaveTransactions();
                    } else if (e.getSource() == depositButton) {
                        accountActions.handleDeposit();
                    } else if (e.getSource() == withdrawButton) {
                        accountActions.handleWithdraw();
                    } else if (e.getSource() == closeAccountButton) {
                        accountActions.handleCloseAccount();
                    }
                };
        createSavingsAccountButton.addActionListener(accountActionListener);
        createCreditAccountButton.addActionListener(accountActionListener);
        getTransactionsButton.addActionListener(accountActionListener);
        getAccountButton.addActionListener(accountActionListener);
        saveTransactionButton.addActionListener(accountActionListener);
        depositButton.addActionListener(accountActionListener);
        withdrawButton.addActionListener(accountActionListener);
        closeAccountButton.addActionListener(accountActionListener);

        accountButtonPanel.add(createSavingsAccountButton);
        accountButtonPanel.add(createCreditAccountButton);
        accountButtonPanel.add(closeAccountButton);
        accountButtonPanel.add(getAccountButton);
        accountButtonPanel.add(getTransactionsButton);
        accountButtonPanel.add(depositButton);
        accountButtonPanel.add(withdrawButton);
        accountButtonPanel.add(saveTransactionButton);

        return panel;
    }
}
