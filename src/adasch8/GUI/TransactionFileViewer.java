package adasch8.GUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * Klass som hanterar läsning och visning av transaktioner i en dialogruta.
 *
 * Author: Adasch-8, Adam Schedin
 */
public class TransactionFileViewer {
    public static void showTransactionsInDialog(JFrame frame, String filePath) {
        JDialog dialog = new JDialog(frame, "Saved Transactions", true);
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get(filePath).toFile()))) {
            textArea.read(reader, null);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Kunde inte läsa filen.", "Fel", JOptionPane.ERROR_MESSAGE);
            return;
        }

        dialog.add(new JScrollPane(textArea));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}
