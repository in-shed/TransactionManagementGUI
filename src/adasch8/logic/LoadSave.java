package adasch8.logic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Klass som hanterar sparande och laddande av data.
 *
 * @author Adasch-8, Adam Schedin
 */
public class LoadSave {
    /**
     * Sparar bankens data till en fil.
     *
     * @param bankLogic banklogik.
     * @param filePath sökvägen till filen där data ska sparas.
     */
    public static void saveBankToFile(BankLogic bankLogic, String filePath) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(bankLogic);
            out.writeInt(Account.getLastAssignedId());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    /**
     * Laddar bankens data från en fil. Intellij rekommenderade pattern variable här.
     *
     * @param filePath sökvägen till filen där data ska laddas ifrån.
     * @return banklogik med uppdaterad data, alternativt null om det inte gick att ladda.
     */
    public static BankLogic loadBankFromFile(String filePath) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
            Object object = in.readObject();
            if (!(object instanceof BankLogic bankLogic)) {
                JOptionPane.showMessageDialog(null, "Felaktigt filformat");
                return null;
            }
            int lastAssignedId = in.readInt();
            Account.setLastAssignedID(lastAssignedId);
            return bankLogic;
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            return null;
        }
    }

    /**
     * Sparar en transaktion till en fil.
     *
     * @param filePath sökvägen till filen där transaktionen ska sparas.
     * @param transaction transaktionen som ska sparas.
     * @return transaktionen som sparades, eller null om det inte gick att spara.
     */
    public static List<String> saveTransactionToFile(String filePath, List<String> transaction) {
        try {
            Files.write(
                    Paths.get(filePath),
                    transaction,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
            return transaction;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving transaction: " + e.getMessage());
            return null;
        }
    }
}
