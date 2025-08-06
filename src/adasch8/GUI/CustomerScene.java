package adasch8.GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import adasch8.logic.BankLogic;

/**
 * Klass som hanterar kundscenen i det grafiska användargränssnittet.
 *
 * @author Adasch-8, Adam Schedin
 */
public class CustomerScene {
    /**
     * Skapar kundscenen.
     *
     * @return panelen för kundscenen.
     */
    public JPanel create(
            BankLogic bankLogic, JList<String> customerList
    ) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Kunder"), BorderLayout.NORTH);

        JPanel customerPanel = new JPanel(new BorderLayout());
        panel.add(customerPanel, BorderLayout.CENTER);

        JScrollPane customerScrollPane = new JScrollPane(customerList);
        customerPanel.add(customerScrollPane, BorderLayout.CENTER);

        ImageIcon bankIcon = new ImageIcon("src/adasch8/assets/Customer.png");
        JLabel bankIconLabel = new JLabel(bankIcon);
        panel.add(bankIconLabel, BorderLayout.NORTH);

        JPanel customerButtonPanel = new JPanel(new FlowLayout());
        customerPanel.add(customerButtonPanel, BorderLayout.NORTH);


        JTextArea outputTextArea = new JTextArea(5, 20);
        outputTextArea.setEditable(false);
        JScrollPane customerOutputScrollPane = new JScrollPane(outputTextArea);
        panel.add(customerOutputScrollPane, BorderLayout.SOUTH);

        CustomerActions customerActions =
                new CustomerActions(bankLogic, customerList, outputTextArea);

        outputTextArea.setText("Vänligen välj en kund.");

        customerList.setListData(bankLogic.getCustomerInfoArray());

        JButton addCustomerButton = new JButton("Lägg till kund");
        JButton getCustomerButton = new JButton("Hämta kund");
        JButton changeCustomerNameButton = new JButton("Ändra namnet på kund");
        JButton deleteCustomerButton = new JButton("Ta bort kund");

        ActionListener customerActionListener =
                e -> {
                    if (e.getSource() == addCustomerButton) {
                        customerActions.handleAddCustomer();
                    } else if (e.getSource() == deleteCustomerButton) {
                        customerActions.handleDeleteCustomer();
                    } else if (e.getSource() == changeCustomerNameButton) {
                        customerActions.handleChangeCustomerName();
                    } else if (e.getSource() == getCustomerButton) {
                        customerActions.handleGetCustomer();
                    }
                };
        addCustomerButton.addActionListener(customerActionListener);
        deleteCustomerButton.addActionListener(customerActionListener);
        changeCustomerNameButton.addActionListener(customerActionListener);
        getCustomerButton.addActionListener(customerActionListener);

        customerButtonPanel.add(addCustomerButton);
        customerButtonPanel.add(getCustomerButton);
        customerButtonPanel.add(changeCustomerNameButton);
        customerButtonPanel.add(deleteCustomerButton);

        return panel;

}
}
