package pharmacie.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import pharmacie.dao.FactureDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.dao.VenteDAO;
import pharmacie.model.Facture;
import pharmacie.model.LigneVente;
import pharmacie.model.Vente;
import pharmacie.util.Config;
import pharmacie.util.ConnexionBD;

public class FactureService {
    private static final BigDecimal CENT = BigDecimal.valueOf(100);

    private final FactureDAO factureDAO = new FactureDAO();
    private final VenteDAO venteDAO = new VenteDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();

    /**
     * Calcule les totaux, attribue le numéro suivant et enregistre la facture.
     * Appelée par VenteService dans la transaction de la vente.
     */
    public Facture genererFacture(Vente vente, String modePaiement) throws SQLException {
        BigDecimal taux = Config.getDecimal("tva.taux");
        BigDecimal ht = vente.getMontantTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal ttc = ht.add(ht.multiply(taux).divide(CENT, 2, RoundingMode.HALF_UP));

        Facture f = new Facture();
        f.setNumero(factureDAO.genererNumero(LocalDateTime.now().getYear()));
        f.setDateFacture(LocalDateTime.now());
        f.setMontantHt(ht);
        f.setTauxTva(taux);
        f.setMontantTtc(ttc);
        f.setModePaiement(modePaiement);
        f.setVente(vente);
        factureDAO.ajouter(f);
        return f;
    }

    /** Charge la facture d'une vente avec la vente complète (lignes, client, vendeur) pour l'affichage. */
    public Facture chargerParVente(int idVente) throws SQLException {
        Facture f = factureDAO.trouverParVente(idVente);
        if (f != null) {
            f.setVente(venteDAO.trouverParId(idVente));
        }
        return f;
    }

    /** Annule la facture et remet en stock les quantités vendues (réservé à l'administrateur). */
    public void annulerFacture(Facture facture) throws SQLException {
        if (!AuthService.estAdmin()) {
            throw new SQLException("Seul l'administrateur peut annuler une facture.");
        }
        if (facture.estAnnulee()) {
            return;
        }
        Connection cn = ConnexionBD.getConnexion();
        try {
            cn.setAutoCommit(false);
            factureDAO.annuler(facture.getId());
            for (LigneVente l : facture.getVente().getLignes()) {
                medicamentDAO.modifierQuantite(l.getMedicament().getId(), l.getQuantite());
            }
            cn.commit();
            facture.setStatut(Facture.ANNULEE);
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }
}
