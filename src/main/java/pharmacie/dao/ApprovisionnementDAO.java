package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Approvisionnement;
import pharmacie.model.Fournisseur;
import pharmacie.model.Medicament;
import pharmacie.model.Utilisateur;

public class ApprovisionnementDAO implements CrudDAO<Approvisionnement> {

    private static final String SELECT =
            "SELECT ap.*, m.nom AS m_nom, f.nom AS f_nom, u.nom AS u_nom, u.prenom AS u_prenom "
          + "FROM Approvisionnement ap "
          + "JOIN Medicament m ON m.id_medicament = ap.id_medicament "
          + "JOIN Fournisseur f ON f.id_fournisseur = ap.id_fournisseur "
          + "JOIN Utilisateur u ON u.id_utilisateur = ap.id_utilisateur ";

    @Override
    public boolean ajouter(Approvisionnement a) throws SQLException {
        String sql = "INSERT INTO Approvisionnement (quantite, prix_achat, id_medicament, id_fournisseur, id_utilisateur) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            ps.setInt(1, a.getQuantite());
            ps.setBigDecimal(2, a.getPrixAchat());
            ps.setInt(3, a.getMedicament().getId());
            ps.setInt(4, a.getFournisseur().getId());
            ps.setInt(5, a.getUtilisateur().getId());
            boolean ok = ps.executeUpdate() == 1;
            a.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Approvisionnement trouverParId(int id) throws SQLException {
        List<Approvisionnement> liste = lister(SELECT + "WHERE ap.id_appro = ?", id);
        return liste.isEmpty() ? null : liste.get(0);
    }

    @Override
    public List<Approvisionnement> listerTous() throws SQLException {
        return lister(SELECT + "ORDER BY ap.date_appro DESC");
    }

    @Override
    public boolean modifier(Approvisionnement a) throws SQLException {
        String sql = "UPDATE Approvisionnement SET prix_achat = ?, id_fournisseur = ? WHERE id_appro = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            ps.setBigDecimal(1, a.getPrixAchat());
            ps.setInt(2, a.getFournisseur().getId());
            ps.setInt(3, a.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Approvisionnement WHERE id_appro = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Approvisionnement> listerParMedicament(int idMedicament) throws SQLException {
        return lister(SELECT + "WHERE ap.id_medicament = ? ORDER BY ap.date_appro DESC", idMedicament);
    }

    private List<Approvisionnement> lister(String sql, Object... params) throws SQLException {
        List<Approvisionnement> liste = new ArrayList<>();
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

    private Approvisionnement lire(ResultSet rs) throws SQLException {
        Approvisionnement a = new Approvisionnement();
        a.setId(rs.getInt("id_appro"));
        a.setDateAppro(DaoUtil.dateHeure(rs, "date_appro"));
        a.setQuantite(rs.getInt("quantite"));
        a.setPrixAchat(rs.getBigDecimal("prix_achat"));

        Medicament m = new Medicament();
        m.setId(rs.getInt("id_medicament"));
        m.setNom(rs.getString("m_nom"));
        a.setMedicament(m);

        Fournisseur f = new Fournisseur();
        f.setId(rs.getInt("id_fournisseur"));
        f.setNom(rs.getString("f_nom"));
        a.setFournisseur(f);

        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id_utilisateur"));
        u.setNom(rs.getString("u_nom"));
        u.setPrenom(rs.getString("u_prenom"));
        a.setUtilisateur(u);
        return a;
    }
}
