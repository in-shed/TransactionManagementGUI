package adasch8.GUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import adasch8.logic.BankLogic;
import adasch8.logic.LoadSave;

/**
 * GUI för bankapplikationen.
 *
 * @author Adasch-8, Adam Schedin
 */
public class GUI {

    private BankLogic bankLogic;
    private final JFrame frame;
    private final JPanel cardPanel;
    private final JList<String> customerList;
    private final JList<String> accountList;
    private boolean isCustomerMenuActive;
    private JPanel customerScenePanel;
    private final AccountScene accountScene;

    private final String CUSTOMER_SCENE = "customerScene";
    private final String DEFAULT_DIRECTORY = "src/adasch8_files";

    /** Konstruktor för GUI. */
    public GUI() {
        this.bankLogic = new BankLogic();
        this.frame = new JFrame("Bank Management Program");
        this.cardPanel = new JPanel(new CardLayout());
        this.customerList = new JList<>();
        this.accountList = new JList<>();
        this.isCustomerMenuActive = true;
        CustomerScene customerScene = new CustomerScene();
        this.customerScenePanel = customerScene.create(bankLogic, customerList);
        this.accountScene = new AccountScene();
    }

    /** Startar upp GUI. */
    public void initGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(800, 600);

        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cardPanel.add(customerScenePanel, CUSTOMER_SCENE);
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, CUSTOMER_SCENE);

        frame.add(cardPanel, BorderLayout.CENTER);

        JMenuBar customerMenuBar = new JMenuBar();
        JMenu loadSaveBank = new JMenu("Ladda/Spara Bank");
        JMenu accountMenu = new JMenu("Växla Meny");
        JMenu transactionMenu = new JMenu("Transaktioner");
        transactionMenu.setVisible(false);

        JMenuItem loadBankMenuItem = new JMenuItem("Ladda bank");
        JMenuItem saveBankMenuItem = new JMenuItem("Spara bank");
        JMenuItem viewAccountsMenuItem = new JMenuItem("Visa konton");
        JMenuItem openTransactionsMenuItem = new JMenuItem("Öppna tidigare sparade transaktioner");

        loadSaveBank.add(loadBankMenuItem);
        loadSaveBank.add(saveBankMenuItem);
        accountMenu.add(viewAccountsMenuItem);
        transactionMenu.add(openTransactionsMenuItem);

        ActionListener menuActionListener =
                e -> {
                    if (e.getSource() == viewAccountsMenuItem) {
                        if (isCustomerMenuActive) {
                            if (handleViewAccounts()) {
                                viewAccountsMenuItem.setText("Tillbaka till kunder");
                                loadSaveBank.setVisible(false);
                                transactionMenu.setVisible(true);
                            }
                        } else {
                            handleViewCustomers();
                            viewAccountsMenuItem.setText("Visa konton");
                            loadSaveBank.setVisible(true);
                            transactionMenu.setVisible(false);
                        }
                        isCustomerMenuActive = !isCustomerMenuActive;
                    }
                    if (e.getSource() == loadBankMenuItem) {
                        handleLoadBank();
                    }
                    if (e.getSource() == saveBankMenuItem) {
                        handleSaveBank();
                    }
                    if (e.getSource() == openTransactionsMenuItem) {
                        handleReadTransactions();
                    }
                };

        viewAccountsMenuItem.addActionListener(menuActionListener);
        loadBankMenuItem.addActionListener(menuActionListener);
        saveBankMenuItem.addActionListener(menuActionListener);
        openTransactionsMenuItem.addActionListener(menuActionListener);

        customerMenuBar.add(loadSaveBank);
        customerMenuBar.add(accountMenu);
        customerMenuBar.add(transactionMenu);
        frame.setJMenuBar(customerMenuBar);

        frame.setVisible(true);
    }

    /** Hanterar inläsning av bankdata. */
    private void handleLoadBank() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Ladda bank");
        fileChooser.setCurrentDirectory(new File(DEFAULT_DIRECTORY));
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            BankLogic loadedBankLogic = LoadSave.loadBankFromFile(filePath);
            if (loadedBankLogic == null || loadedBankLogic.getAllCustomers().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Misslyckades med att ladda bank.");
                return;
            }
            bankLogic = loadedBankLogic;
            customerList.setListData(bankLogic.getCustomerInfoArray());
            cardPanel.remove(customerScenePanel);
            CustomerScene customerScene = new CustomerScene();
            customerScenePanel = customerScene.create(bankLogic, customerList);
            cardPanel.add(customerScenePanel, CUSTOMER_SCENE);
            cardPanel.revalidate();
            cardPanel.repaint();
        }
    }

    /** Hanterar sparande av bankdata. */
    private void handleSaveBank() {
        if (bankLogic.getAllCustomers().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Inga kunder att spara.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Spara bank");
        fileChooser.setSelectedFile(new File("bankdata.dat"));
        fileChooser.setCurrentDirectory(new File(DEFAULT_DIRECTORY));
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
            LoadSave.saveBankToFile(bankLogic, filePath);
        }
    }

    /** Hanterar visning av kundscenen. */
    private void handleViewCustomers() {
        cardPanel.add(customerScenePanel, CUSTOMER_SCENE);
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, CUSTOMER_SCENE);
        accountList.clearSelection();
    }

    /** Hanterar visning av kontoscenen. AccountList uppdateras dynamiskt. */
    private boolean handleViewAccounts() {
        int customerSelectedIndex = customerList.getSelectedIndex();
        final int SELECTED_INDEX_NOT_FOUND = -1;
        if (customerSelectedIndex == SELECTED_INDEX_NOT_FOUND) {
            String pNo =
                    JOptionPane.showInputDialog("Ingen kund vald.\nAnge personnummer manuellt:");
            if (pNo == null || pNo.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Åtgärden avbröts.");
                return false;
            }
            if (bankLogic.getCustomer(pNo) == null) {
                JOptionPane.showMessageDialog(
                        null, "Kunde inte hitta kund med detta personnummer.");
                return false;
            }
            customerSelectedIndex = bankLogic.findCustomerIndexBypNo(pNo);
        }

        JPanel accountScenePanel =
                accountScene.create(bankLogic, accountList, customerSelectedIndex);
        String ACCOUNT_SCENE = "accountScene";
        cardPanel.add(accountScenePanel, ACCOUNT_SCENE);
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, ACCOUNT_SCENE);
        return true;
    }

    /** Läser en textfil innehållande tidigare transaktioner i programmet. */
    private void handleReadTransactions() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Läs transaktioner");
        fileChooser.setCurrentDirectory(new File(DEFAULT_DIRECTORY));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            TransactionFileViewer.showTransactionsInDialog(frame, filePath);
        }
    }
}
