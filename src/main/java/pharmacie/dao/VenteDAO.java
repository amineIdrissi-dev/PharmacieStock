package pharmacie.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Acheteur;
import pharmacie.model.Utilisateur;
import pharmacie.model.Vente;

public class VenteDAO implements CrudDAO<Vente> {

    private static final String SELECT =
            "SELECT v.*, a.nom AS a_nom, a.prenom AS a_prenom, a.telephone AS a_tel, "
          + "u.nom AS u_nom, u.prenom AS u_prenom, "
          + "(SELECT ISNULL(SUM(l.quantite), 0) FROM LigneVente l WHERE l.id_vente = v.id_vente) AS nb_articles "
          + "FROM Vente v "
          + "LEFT JOIN Acheteur a ON a.id_acheteur = v.id_acheteur "
          + "JOIN Utilisateur u ON u.id_utilisateur = v.id_utilisateur ";

    private final LigneVenteDAO ligneVenteDAO = new LigneVenteDAO();

    /** Enregistre l'en-tête de la vente et remplit son id généré. */
    @Override
    public boolean ajouter(Vente v) throws SQLException {
        String sql = "INSERT INTO Vente (montant_total, id_acheteur, id_utilisateur) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            ps.setBigDecimal(1, v.getMontantTotal());
            DaoUtil.setEntierOuNull(ps, 2, v.getAcheteur() == null ? null : v.getAcheteur().getId());
            ps.setInt(3, v.getUtilisateur().getId());
            boolean ok = ps.executeUpdate() == 1;
            v.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    /** Retourne la vente complète, avec ses lignes. */
    @Override
    public Vente trouverParId(int id) throws SQLException {
        List<Vente> liste = lister(SELECT + "WHERE v.id_vente = ?", id);
        if (liste.isEmpty()) {
            return null;
        }
        Vente v = liste.get(0);
        v.setLignes(ligneVenteDAO.listerParVente(id));
        return v;
    }

    @Override
    public List<Vente> listerTous() throws SQLException {
        return lister(SELECT + "ORDER BY v.date_vente DESC");
    }

    @Override
    public boolean modifier(Vente v) throws SQLException {
        String sql = "UPDATE Vente SET montant_total = ?, id_acheteur = ? WHERE id_vente = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setBigDecimal(1, v.getMontantTotal());
            DaoUtil.setEntierOuNull(ps, 2, v.getAcheteur() == null ? null : v.getAcheteur().getId());
            ps.setInt(3, v.getId());
            return ps.executeUpdate() == 1;
        }
    }

    /** Supprime la vente ; ses lignes et sa facture sont supprimées en cascade. */
    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Vente WHERE id_vente = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** Ventes entre deux dates incluses ; null = pas de limite. */
    public List<Vente> listerParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        return lister(SELECT + "WHERE v.date_vente >= ? AND v.date_vente < ? ORDER BY v.date_vente DESC",
                borneDebut(debut), borneFin(fin));
    }

    /** Total HT des ventes de la période, hors factures annulées. */
    public BigDecimal totalParPeriode(LocalDate debut, LocalDate fin) throws SQLException {
        String sql = "SELECT ISNULL(SUM(v.montant_total), 0) FROM Vente v "
                   + "LEFT JOIN Facture f ON f.id_vente = v.id_vente "
                   + "WHERE (f.statut IS NULL OR f.statut <> 'ANNULEE') "
                   + "AND v.date_vente >= ? AND v.date_vente < ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setTimestamp(1, borneDebut(debut));
            ps.setTimestamp(2, borneFin(fin));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public List<Vente> listerParAcheteur(int idAcheteur) throws SQLException {
        return lister(SELECT + "WHERE v.id_acheteur = ? ORDER BY v.date_vente DESC", idAcheteur);
    }

    public int compterVentesDuJour() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vente WHERE CAST(date_vente AS DATE) = CAST(GETDATE() AS DATE)";
        try (PreparedStatement ps = DaoUtil.preparer(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Timestamp borneDebut(LocalDate debut) {
        return Timestamp.valueOf((debut == null ? LocalDate.of(1900, 1, 1) : debut).atStartOfDay());
    }

    private Timestamp borneFin(LocalDate fin) {
        return Timestamp.valueOf((fin == null ? LocalDate.of(9999, 12, 30) : fin.plusDays(1)).atStartOfDay());
    }

    private List<Vente> lister(String sql, Object... params) throws SQLException {
        List<Vente> liste = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(lire(rs));
                }
            }
        }
        return liste;
    }

    private Vente lire(ResultSet rs) throws SQLException {
        Vente v = new Vente();
        v.setId(rs.getInt("id_vente"));
        v.setDateVente(DaoUtil.dateHeure(rs, "date_vente"));
        v.setMontantTotal(rs.getBigDecimal("montant_total"));
        v.setNombreArticles(rs.getInt("nb_articles"));

        int idAcheteur = rs.getInt("id_acheteur");
        if (!rs.wasNull()) {
            Acheteur a = new Acheteur();
            a.setId(idAcheteur);
            a.setNom(rs.getString("a_nom"));
            a.setPrenom(rs.getString("a_prenom"));
            a.setTelephone(rs.getString("a_tel"));
            v.setAcheteur(a);
        }

        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("u_nom"));
        u.setPrenom(rs.getString("u_prenom"));
        v.setUtilisateur(u);
        return v;
    }
}
