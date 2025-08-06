package adasch8;

import javax.swing.SwingUtilities;

import adasch8.GUI.GUI;

/**
 * Bootstrap för bankapplikationen.
 *
 * @author Adasch-8, Adam Schedin
 */
class Main {
    /**
     * Huvudmetoden för att starta programmet. <a
     * href="https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingUtilities.html#invokeLater-java.lang.Runnable-">...</a>
     *
     * @param args kommandoradsargument
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    GUI gui = new GUI();
                    gui.initGUI();
                });
    }
}
