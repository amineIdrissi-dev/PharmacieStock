package pharmacie;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import pharmacie.util.Config;
import pharmacie.view.LoginFrame;

/** Point d'entrée : ouvre la fenêtre de connexion. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // apparence par défaut de Java
            }
            try {
                Config.get("db.url");
            } catch (Throwable e) {
                JOptionPane.showMessageDialog(null,
                        "Impossible de lire config.properties.\n"
                        + "Copiez config.exemple.properties en config.properties,\n"
                        + "et lancez l'application depuis le dossier du projet.",
                        "Erreur de configuration", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new LoginFrame().setVisible(true);
        });
    }
}
