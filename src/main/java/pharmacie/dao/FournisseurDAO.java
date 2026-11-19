package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Fournisseur;

public class FournisseurDAO implements CrudDAO<Fournisseur> {

    @Override
    public boolean ajouter(Fournisseur f) throws SQLException {
        String sql = "INSERT INTO Fournisseur (nom, telephone, email, adresse) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.preparerAvecCle(sql)) {
            remplir(ps, f);
            boolean ok = ps.executeUpdate() == 1;
            f.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Fournisseur trouverParId(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("SELECT * FROM Fournisseur WHERE id_fournisseur = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lire(rs) : null;
            }
        }
    }

    @Override
    public List<Fournisseur> listerTous() throws SQLException {
        List<Fournisseur> liste = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.preparer("SELECT * FROM Fournisseur ORDER BY nom");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(lire(rs));
            }
        }
        return liste;
    }

    @Override
    public boolean modifier(Fournisseur f) throws SQLException {
        String sql = "UPDATE Fournisseur SET nom = ?, telephone = ?, email = ?, adresse = ? WHERE id_fournisseur = ?";
        try (PreparedStatement ps = DaoUtil.preparer(sql)) {
            remplir(ps, f);
            ps.setInt(5, f.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Fournisseur WHERE id_fournisseur = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private void remplir(PreparedStatement ps, Fournisseur f) throws SQLException {
        ps.setString(1, f.getNom());
        ps.setString(2, f.getTelephone());
        ps.setString(3, f.getEmail());
        ps.setString(4, f.getAdresse());
    }

    private Fournisseur lire(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setId(rs.getInt("id_fournisseur"));
        f.setNom(rs.getString("nom"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        return f;
    }
}
