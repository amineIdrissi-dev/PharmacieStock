package pharmacie.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import pharmacie.dao.LigneVenteDAO;
import pharmacie.dao.MedicamentDAO;
import pharmacie.dao.VenteDAO;
import pharmacie.model.Facture;
import pharmacie.model.LigneVente;
import pharmacie.model.Medicament;
import pharmacie.model.Vente;
import pharmacie.util.ConnexionBD;

public class VenteService {
    private final VenteDAO venteDAO = new VenteDAO();
    private final LigneVenteDAO ligneVenteDAO = new LigneVenteDAO();
    private final MedicamentDAO medicamentDAO = new MedicamentDAO();
    private final FactureService factureService = new FactureService();

    /**
     * Vérifie le stock, enregistre la vente et ses lignes, diminue les quantités
     * et crée la facture, le tout dans une seule transaction.
     */
    public Facture enregistrerVente(Vente vente, String modePaiement) throws SQLException {
        if (vente.getLignes().isEmpty()) {
            throw new SQLException("La vente ne contient aucun médicament.");
        }
        Connection cn = ConnexionBD.getConnexion();
        try {
            cn.setAutoCommit(false);

            // Quantité demandée par médicament (un même produit peut être sur plusieurs lignes)
            Map<Integer, Integer> demandes = new LinkedHashMap<>();
            for (LigneVente l : vente.getLignes()) {
                demandes.merge(l.getMedicament().getId(), l.getQuantite(), Integer::sum);
            }
            for (Map.Entry<Integer, Integer> d : demandes.entrySet()) {
                Medicament m = medicamentDAO.trouverParId(d.getKey());
                if (m == null || m.getQuantite() < d.getValue()) {
                    String nom = m == null ? "un médicament supprimé" : m.getNom();
                    int stock = m == null ? 0 : m.getQuantite();
                    throw new SQLException("Stock insuffisant pour " + nom + " (disponible : " + stock + ").");
                }
            }

            vente.setUtilisateur(AuthService.getUtilisateurConnecte());
            vente.calculerTotal();
            venteDAO.ajouter(vente);                       // remplit vente.id
            for (LigneVente l : vente.getLignes()) {
                l.setIdVente(vente.getId());
                ligneVenteDAO.ajouter(l);
                medicamentDAO.modifierQuantite(l.getMedicament().getId(), -l.getQuantite());
            }
            Facture facture = factureService.genererFacture(vente, modePaiement);   // même transaction
            cn.commit();
            return facture;
        } catch (SQLException e) {
            cn.rollback();                                 // rien n'est enregistré en cas d'erreur
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }
}
