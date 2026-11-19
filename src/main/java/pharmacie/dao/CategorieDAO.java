package pharmacie.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pharmacie.model.Categorie;

public class CategorieDAO implements CrudDAO<Categorie> {

    @Override
    public boolean ajouter(Categorie c) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparerAvecCle("INSERT INTO Categorie (libelle) VALUES (?)")) {
            ps.setString(1, c.getLibelle());
            boolean ok = ps.executeUpdate() == 1;
            c.setId(DaoUtil.cleGeneree(ps));
            return ok;
        }
    }

    @Override
    public Categorie trouverParId(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("SELECT * FROM Categorie WHERE id_categorie = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? lire(rs) : null;
            }
        }
    }

    @Override
    public List<Categorie> listerTous() throws SQLException {
        List<Categorie> liste = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.preparer("SELECT * FROM Categorie ORDER BY libelle");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(lire(rs));
            }
        }
        return liste;
    }

    @Override
    public boolean modifier(Categorie c) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("UPDATE Categorie SET libelle = ? WHERE id_categorie = ?")) {
            ps.setString(1, c.getLibelle());
            ps.setInt(2, c.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean supprimer(int id) throws SQLException {
        try (PreparedStatement ps = DaoUtil.preparer("DELETE FROM Categorie WHERE id_categorie = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Categorie lire(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id_categorie"), rs.getString("libelle"));
    }
}
