package pharmacie.service;

import java.sql.Connection;
import java.sql.SQLException;

import pharmacie.dao.ApprovisionnementDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.model.Approvisionnement;
import pharmacie.util.ConnexionBD;

public class StockService {
    private final ApprovisionnementDAO approDAO = new ApprovisionnementDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();

    /** Enregistre l'entrée de stock et augmente la quantité du médicament, dans une transaction. */
    public void reapprovisionner(Approvisionnement appro) throws SQLException {
        if (appro.getQuantite() <= 0) {
            throw new SQLException("La quantité doit être supérieure à 0.");
        }
        Connection cn = ConnexionBD.getConnexion();
        try {
            cn.setAutoCommit(false);
            appro.setUtilisateur(AuthService.getUtilisateurConnecte());
            approDAO.ajouter(appro);
            medicamentDAO.modifierQuantite(appro.getMedicament().getId(), appro.getQuantite());
            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }
}
