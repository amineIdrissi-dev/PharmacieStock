package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Acheteur;
import pharmacie.model.Vente;

public class AcheteurDAO implements CrudDAO<Acheteur> {

    /** Le nombre d'achats est calculé pour l'affichage du tableau des acheteurs. */
    private static final String SELECT =
            "SELECT a.*, (SELECT COUNT(*) FROM Vente v WHERE v.id_acheteur = a.id_acheteur) AS nb_achats "
          + "FROM Acheteur a ";

    @Override
    public boolean ajouter(Acheteur a) throws SQLException {
        String sql = "INSERT INTO Acheteur (nom, prenom, telephone, email, adresse) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            remplir(ps, a);
            boolean ok = ps.executeUpdate() == 1;
            a.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Acheteur trouverParId(int id) throws SQLException {
        List<Acheteur> liste = lister(SELECT + "WHERE a.id_acheteur = ?", id);
        return liste.isEmpty() ? null : liste.get(0);
    }

    @Override
    public List<Acheteur> listerTous() throws SQLException {
        return lister(SELECT + "ORDER BY a.nom, a.prenom");
    }

    @Override
    public boolean modifier(Acheteur a) throws SQLException {
        String sql = "UPDATE Acheteur SET nom = ?, prenom = ?, telephone = ?, email = ?, adresse = ? "
                   + "WHERE id_acheteur = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            remplir(ps, a);
            ps.setInt(6, a.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Acheteur WHERE id_acheteur = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Acheteur> rechercherParNom(String texte) throws SQLException {
        String m = DaoUtil.motif(texte);
        return lister(SELECT + "WHERE a.nom LIKE ? OR a.prenom LIKE ? OR a.telephone LIKE ? "
                    + "ORDER BY a.nom, a.prenom", m, m, m);
    }

    public List<Vente> historiqueAchats(int idAcheteur) throws SQLException {
        return new VenteDAO().listerParAcheteur(idAcheteur);
    }

    /** Nombre d'achats de chaque acheteur, lu avec la dernière liste chargée. */
    public int nombreAchats(Acheteur a) {
        return a instanceof AcheteurAvecStats s ? s.nbAchats : 0;
    }

    private void remplir(PreparedStatement ps, Acheteur a) throws SQLException {
        ps.setString(1, a.getNom());
        ps.setString(2, a.getPrenom());
        ps.setString(3, a.getTelephone());
        ps.setString(4, a.getEmail());
        ps.setString(5, a.getAdresse());
    }

    private List<Acheteur> lister(String sql, Object... params) throws SQLException {
        List<Acheteur> liste = new ArrayList<>();
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

    private Acheteur lire(ResultSet rs) throws SQLException {
        AcheteurAvecStats a = new AcheteurAvecStats();
        a.setId(rs.getInt("id_acheteur"));
        a.setNom(rs.getString("nom"));
        a.setPrenom(rs.getString("prenom"));
        a.setTelephone(rs.getString("telephone"));
        a.setEmail(rs.getString("email"));
        a.setAdresse(rs.getString("adresse"));
        a.setDateInscription(DaoUtil.dateHeure(rs, "date_inscription"));
        a.nbAchats = rs.getInt("nb_achats");
        return a;
    }

    /** Acheteur enrichi du nombre d'achats (utilisé seulement pour l'affichage). */
    private static final class AcheteurAvecStats extends Acheteur {
        private int nbAchats;
    }
}
