package pharmacie.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Singleton : une seule connexion JDBC partagée par toute l'application. */
public final class ConnexionBD {
    private static Connection connexion;

    private ConnexionBD() {}

    public static synchronized Connection getConnexion() throws SQLException {
        if (connexion == null || connexion.isClosed()) {
            connexion = DriverManager.getConnection(
                    Config.get("db.url"), Config.get("db.utilisateur"), Config.get("db.motdepasse"));
        }
        return connexion;
    }

    public static synchronized void fermer() {
        try {
            if (connexion != null && !connexion.isClosed()) {
                connexion.close();
            }
        } catch (SQLException ignored) {
            // fermeture en fin d'application : rien à faire
        }
    }
}
